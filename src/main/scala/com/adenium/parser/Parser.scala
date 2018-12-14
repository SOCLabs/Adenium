package com.adenium.parser

import com.adenium.common.{Field, Parsed, VariableKeys}
import com.adenium.parser.ParserHelper._
import com.adenium.parser.devices.Syslog
import com.adenium.parser.reference.File2ParserRef.ParserRefFiles
import com.adenium.parser.reference.ParserRef
import com.adenium.parser.structs.Agent
import com.adenium.utils.May._
import com.adenium.utils.WOption
import com.adenium.utils.WOption.WOptionLogOn

import scala.collection.immutable.HashMap


/** Tokenize the sensor message and generate the normalization result. */
object Parser extends Serializable {

  /**
    * To normalize the contents of an event, you need to decide which rules apply.
    * The value in the Host Name field is extracted from the transport rule (Syslog) header to determine the rules to be applied.
    * The host name corresponds to the agentIp field in the reference information "[Agent]" and is used as a basis for selecting,
    * the normalization rule to apply to the event.
    * An agent is marked as a candidate agent because there are multiple agents that share a single host name.
    */
  def findCandidates(host: String, syslog_body: String, ref: ParserRef)
                    (implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[Agent]] = {

    for {

      socInfo <- SOCDeviceKindHint( host, syslog_body, ref)
      kind = socInfo._1
      hint = socInfo._2

      agents <- filterAgents(kind, hint, ref)

    } yield agents

  }

  /** Tokenize using regular expressions.
    *
      1. The regular expression is selected for all sensors connected to the candidate agent.
      1. Apply the regular expression to the event log.
      1. When matching multiple regular expressions, they first return a matching regular expression and the result.
      1. Generates the tokenized result as an array of normalized field kyeId, value pairs.
      1. Determines the owner of the agent and the normalization event.
    */
  def tryParsing(agents: Array[Agent], syslog_body: String, ref: ParserRef)
                (implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], (Agent, Array[(Int, String)])] = {

    for {

      tok_rules <- filterTokenizeRules(agents, ref)

      tokens <- tryTokenizeRules(syslog_body, tok_rules)
      matched_rule = tokens._1
      matched_result = tokens._2

      arr_rules <- findArrangeRule(matched_rule.id, matched_result.groupCount, ref)
      parsed <- arrangeResult(matched_result, arr_rules)
      agent <- decideAgent(matched_rule.sensorId, decideCompanyByIp(_, parsed, ref), agents)

    } yield (agent, parsed)

  }


  /** Generates a normalization field.
    *
    * fix : A field that does not change the token result.
    * rep : A field that changes the token result with a rule defined in "ReplaceField".
    * */
  private def makeFields(syslog_header: Syslog.SyslogHeader,
                         agent: Agent, parsed: Array[(Int, String)], ref: ParserRef)
                        (implicit wOptionLogOn: WOptionLogOn, vf: Option[VariableKeys])

  : WOption[List[String], Array[Array[Field]]]
  = {

    val m = HashMap( parsed:_*)

    val fix = makefixFields(agent, m, ref, syslog_header) //

    val rep = makeRepFields(parsed, agent, ref)

    WOption.sequence( Array(fix, rep))
  }

  def foldFields( extflds: Array[Array[Field]])
                ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[Field]] = {

    val ret =
      WOption.sequence {
        val ret =
          extflds.foldLeft(Array[WOption[List[String], Field]]())(overwrite)
        ret
      }

    if( wOptionLogOn.on)
      ret.appendLog( List[String]("=== foldFields ==="))

    ret
  }

  /** Parsing and normalization.
    *
      1. All candidate agents are selected.
      1. Returns the token result [agent, Array(normalization field key, value)]
      1. Generates a normalization field.
      1. The field is folded to produce the normalization result.
    * */
  def parse2Fields(syslog: Syslog.SyslogMsg, ref: ParserRef)
                  (implicit wOptionLogOn: WOptionLogOn, vf: Option[VariableKeys])
  : WOption[List[String], Array[Field]] = {

    for {
      agents <- findCandidates(syslog.header.hostname, syslog.body, ref)
      tuple <- tryParsing(agents, syslog.body, ref)
      extflds <- makeFields( syslog.header, agent = tuple._1, parsed = tuple._2, ref)
      flds <- foldFields(extflds)

    } yield {
      flds
    }
  }

  /** Data source of reference information
    *
    * You can change the ParserRef to various data sources. (db, zookeeper ..)
    * */
  implicit val referenceFiles: ParserRefFiles = ParserRefFiles(path = "")

  /** This function is the entry point for parsing and normalization.
    *
    * @param orig sensor message [ syslog body ]
    * @param ref reference data
    * @param verbose log on/off
    * @param wOptionLogOn trace log on/off
    * @return result of normalization
    */
  def execute( orig: String, ref: ParserRef, verbose: Boolean = false)
             ( implicit wOptionLogOn: WOptionLogOn)
  : Parsed = {

    implicit val v: Option[VariableKeys] = ParserRef.ref2VariableField(ref)

    /** Separate syslog messages into header and body. */
    val syslog = Syslog.parse( orig)

    /** Parsing and normalization. */
    val fields = syslog.flatMap( s => parse2Fields( s, ref) )

    /** If vervose is true, prints the log.*/
    def logString = if ( verbose ) fields.log.mkString("\n") else ""

    if ( wOptionLogOn.on ) {
      warn ( syslog.option )( "parser.execute : syslog"  + syslog.log.mkString("\n"))
      state ( s"parser.execute : success ? ${fields.option.isDefined}\n fields log" + fields.log.mkString("\n"))
    }

    /** Returns the result of normalization and additional information.
      * fields : Results of normalization
      * host : Hostname of syslog header
      * raw : Original syslog message
      * log : logString
      * */
    Parsed(
      fields = fields.option,
      host = syslog.option.map( _.header.hostname),
      raw = orig,
      log = logString)
  }

}

