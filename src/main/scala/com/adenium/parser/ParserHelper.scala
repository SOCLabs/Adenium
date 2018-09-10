package com.adenium.parser

import com.adenium.common.Keys._
import com.adenium.common.{Field, FieldKey}
import com.adenium.parser.devices._
import com.adenium.parser.reference.ParserRef
import com.adenium.parser.structs._
import com.adenium.utils.May.{lift, maybe}
import com.adenium.utils.WOption
import com.adenium.utils.WOption.{WOptionLogOn, WOptionMaker}

import scala.language.reflectiveCalls
import scala.util.matching.Regex

/** Parsing and normalization methods  */
object ParserHelper {

  /** Apply the overwrite rule to create the field.
    *
    * The default field is the overwrite field. The default field is changed. If it is not an overwrite field, it is added without change.
    *
    * @param fields : target fields
    * @param over : overwrite field
    * @param wOptionLogOn
    * @return
    */
  def overwrite( fields: Array[WOption[List[String], Field]],
                 over: Array[Field])
               ( implicit wOptionLogOn: WOptionLogOn)
  : Array[WOption[List[String], Field]] = {

    def append( base: Array[WOption[List[String], Field]], over: Field)
    : Array[WOption[List[String], Field]] = {

      val idx = base.indexWhere(_.option.exists(_.isEqualKey(over)))
      if (idx == -1)
        wOptionLogOn.log(s"added: ${(over.key.name, over.value)}") ~> Some(over) +: base
      else {
        val fld = base(idx)
        base.update(idx, (
          wOptionLogOn.log(s"changed: ${(over.key.name, over.value)}") ~> Some(over)).appendLog(fld.log))
        base
      }
    }

    over.foldLeft(fields)(append)
  }

  def getString(mid: Int, parsed: Array[(Int, String)])
  : Option[String] = {
    parsed.toMap.get(mid)
  }

  def getInteger(mid: Int, parsed: Map[Int, String] )
  : Option[Int] = {
    parsed.get(mid).flatMap(str => maybe(str.toInt))
  }

  def getInteger(mid: Int, parsed: Array[(Int, String)])
  : Option[Int] = {
      parsed.toMap.get(mid).flatMap(str => maybe(str.toInt))
  }

  def makeField(mid: Int, value: String)
               ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Field] = {

    makeField( getKey(mid), value)
  }

  def makeField ( key: Option[FieldKey], value: String )
                ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Field] = {
    wOptionLogOn.log( s"+ makeField: ${(key.map( _.name), value)}") ~>  key.map( Field(_, value))
  }

  def makeField (target: Int,
                 source: Int,
                 parsed: Map[Int, String],
                 f: String => Option[String] = Some(_))
                ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Field] =
  {
    val ret =
      for {
        to <- getKey( target)   // todo : naming check... what if mid doesn't exists...
        str <- parsed.get(source)
        value <- f( str)

      } yield Field( to, value )

    //todo
    wOptionLogOn.log( s"makeField: ${ret.map( _.key.name)}") ~> ret
  }

  def makeRefFields ( ref: ParserRef )
                    ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[Field]] = {

    val arr = ref.fields.map {
        _.map { f =>
          wOptionLogOn.log(s"from Ref ${(f.key.name, f.value.getOrElse(""))}: ") ~> Some(f)
        }
      }
    arr
      .map ( WOption.sequence( _) )
      .getOrElse( wOptionLogOn.log("* makeRefFields: empty") ~> Some(Array[Field]()))
  }

  def makeRawFields ( parsed: Array[(Int, String)])
                    ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[Field]] = {


    val flds = parsed.map { itm => makeField( itm._1, itm._2 ) }

    WOption.sequence( flds)
      .orElse( wOptionLogOn.log("* makeRawFields : empty") ~> Some(Array[Field]()))
  }

  def makeCatFields( agent: Agent, parsed: Array[(Int, String)], ref: ParserRef)
                   ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[Field]] = {

    // todo : check
    val ret =
      for {
        str <- getString( m_SIGNATURE, parsed)
        signature <- Signature.find( str, agent.vendorId)(ref.signatures)
      } yield {
        WOption.sequence {
          Array(
            makeField(m_CATEGORY1, signature.category1),
            makeField(m_CATEGORY2, signature.category2),
            makeField(m_CATEGORY3, signature.category3)
          )
        }
      }
    ret.getOrElse( wOptionLogOn.log("* makeCatFields : cat not found") ~> Some( Array[Field]()) )

  }

  def makeGeoFields(agent: Agent, parsed: Map[Int, String], ref: ParserRef)
                   ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[Field]] = {

    val ret =
      WOption.sequence{
        val arr = Array(
          makeField( m_SRCCOUNTRY, m_SRCIP, parsed, GeoIpRange.getCountry( _, ref.geoIpRange)),
          makeField( m_DESTCOUNTRY, m_DESTIP, parsed, GeoIpRange.getCountry( _, ref.geoIpRange))
        )
        arr
      }.orElse( wOptionLogOn.log("* makeGeoFields : empty") ~> Some(Array[Field]()))
    ret
  }

  def makefixFields(agent: Agent,
                    parsed: Map[Int, String],
                    ref: ParserRef,
                    syslog: Syslog.SyslogHeader)
                   ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[Field]] = {

    val orig = getInteger( m_COUNT, parsed).getOrElse( 1)
    val repeat = getInteger( m_REPEATCOUNT, parsed).getOrElse( 1)
    val count = ( orig * repeat ).toString

    // todo
    val f  = (str: String) => Option( CompanyIpRange.locationString( CompanyIpRange.isCompanyIp( agent.companyId, ref.companyIpRange, str) ))
    val z = parsed.getOrElse(m_AGENTIP, agent.host)

    val ret = parsed.map { itm => makeField( itm._1, itm._2 ) } ++
      Array (
        makeField( m_COMPANYNM, agent.companyName),
        makeField( m_COMPANYID, agent.companyId.toString),
        makeField( m_COMPANYGROUPID, agent.companyGroupId.toString),

        makeField( m_AGENTID, agent.agentId.toString),    // added for ES
        makeField( m_VENDOR, agent.vendorName),
        makeField( m_DEVICEMODEL, agent.sensor),
        makeField( m_DEVICETYPE, agent.sensorType.toString),
        makeField( m_REPEATCOUNT, repeat.toString),
        makeField( m_COUNT, count),

        makeField( m_SYSLOGHOST, syslog.hostname),
        makeField( m_SYSLOGTIME, syslog.timestamp),
        makeField( m_SRCCOUNTRY, m_SRCIP, parsed, GeoIpRange.getCountry( _, ref.geoIpRange)),
        makeField( m_DESTCOUNTRY, m_DESTIP, parsed, GeoIpRange.getCountry( _, ref.geoIpRange)),

        makeField( m_SRCDIRECTION, m_SRCIP, parsed, f),
        makeField( m_DESTDIRECTION, m_DESTIP, parsed, f)
      )

    WOption.sequence( ret.toArray)
  }


  def makeAgnFields(agent: Agent, parsed: Array[(Int, String)], ref: ParserRef)
                   ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[Field]] = {

    val ret =
      WOption.sequence(
        Array (
          makeField( m_COMPANYNM, agent.companyName),
          makeField( m_COMPANYID, agent.companyId.toString),
          makeField( m_COMPANYGROUPID, agent.companyGroupId.toString),
          makeField( m_AGENTID, agent.agentId.toString),    // added for ES
          makeField( m_VENDOR, agent.vendorName),
          makeField( m_DEVICEMODEL, agent.sensor),
          makeField( m_DEVICETYPE, agent.sensorType.toString)
        )
      ).orElse( wOptionLogOn.log("* makeAgnFields : empty") ~> Some(Array[Field]()))
    ret
  }

  def makeComFields(agent: Agent, parsed: Map[Int, String], ref: ParserRef)
                   ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[Field]] = {

    // todo
    val f  = (str: String) => Option( CompanyIpRange.locationString( CompanyIpRange.isCompanyIp( agent.companyId, ref.companyIpRange, str) ))

    val ret =
      WOption.sequence(
        Array (
          makeField( m_SRCDIRECTION, m_SRCIP, parsed, f),
          makeField( m_DESTDIRECTION, m_DESTIP, parsed, f)
        )
      ).orElse( wOptionLogOn.log("* makeComFields : empty") ~> Some(Array[Field]()))
    ret
  }

  def makeCntFields( parsed: Array[(Int, String)])
                   ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[Field]] = {

    val orig = getInteger( m_COUNT, parsed).getOrElse( 1)
    val repeat = getInteger( m_REPEATCOUNT, parsed).getOrElse( 1)
    val count = ( orig * repeat ).toString

    val ret =
      WOption.sequence(
        Array (
          makeField( m_REPEATCOUNT, repeat.toString),
          makeField( m_COUNT, count)
        )
      ).orElse( wOptionLogOn.log("* makeCntFields : empty") ~> Some(Array[Field]()))
    ret
  }

  def makeHdrFields( syslog: Syslog.SyslogHeader,
                     parsed: Array[(Int, String)])
                   ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[Field]] = {

    val ret =
      WOption.sequence(
        Array (
          makeField( m_SYSLOGTIME, syslog.timestamp)
        )
      ).orElse( wOptionLogOn.log("* makeHdrFields : empty") ~> Some(Array[Field]()))
    ret
  }


  def makeRepFields(parsed: Array[(Int, String)], agn: Agent, ref: ParserRef)
                   ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[Field]] = {

    val replaces: Array[(Int, String)] = ReplaceField.find(parsed, agn, ref.replaceFields)

    WOption.sequence{
      val arr =
        replaces.map( itm => makeField(itm._1, itm._2))
      arr
    }.orElse( wOptionLogOn.log("* makeRepFields : cat not found") ~> Some( Array[Field]()) )
  }

  /** Determines the company of the normalization result.
    *
    * Search by IP information registered in CompanyIp
    * In the absence of matching company ip,
    * The owner is determined by comparing the "Source IP" or "Dest IP" in the normalization field to the IP range.
    * */
  def decideCompanyByIp( candidates: Array[Agent], parsed: Array[(Int, String)], ref: ParserRef)
                       ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String ], Agent ] = {

    def findByCompanyIp = {

      for {
        sip <- wOptionLogOn.log(s"[ decideCompanyByIp ] start ") ~> getString( m_SRCIP, parsed)
        dip <- wOptionLogOn.log(s"[ decideCompanyByIp ] sip = $sip") ~> getString( m_DESTIP, parsed)
        agn <- wOptionLogOn.log(s"[ decideCompanyByIp ] dip = $dip") ~> candidates.find { a =>
          CompanyIp.isCompanyIp ( a.companyId, ref.companyIps, sip, dip ) ||
            CompanyIpRange.isCompanyIp ( a.companyId, ref.companyIpRange, sip, dip)
        }

        out <- wOptionLogOn.log(s"[ decideCompanyByIp ] ($sip, $dip) is found in cid = ${agn.agentId}") ~> Some( agn)

      } yield out
    }

    /** Method to handle cases where there are multiple agents or no results registered in the IP range
      *
      * Determined as the most recently registered device among the active agents
      *
      * 1. If the company's IP range is duplicated and the owner can not be determined
      * 2. There are duplicate agents with multiple sensors that can not determine the device
      * 3. If you can not determine the owner because there is no ip registered in company ip, company ip range
      *
      * */
    def findActiveOrNew(as : Array[Agent]): WOption[List[String], Agent] = {

      val ar = as.filter( _.active )
      ar.length match {
        case 1 =>
          val head = ar.head
          wOptionLogOn.log(s"[ decideCompanyByIp ] found 1 active agent = ${head.agentId}") ~> Some(head)
        case n =>
          val head = ar.sortWith( _.agentId > _.agentId ).head
          wOptionLogOn.log(s"[ decideCompanyByIp ] n-active, latest = ${head.agentId}") ~> Some(head)
      }
    }

    findByCompanyIp orElse findActiveOrNew( candidates)

  }

  /** Determine the Agent that corresponds to the normalization result.
    *
    * In a shared device or shared zone, multiple Agents or multiple company information are registered in a single Agent.
    * For multiple sensors, determine the agent as the type of matched regular expression
    * For multiple companies, agents and owners are determined based on company IP information.
    * */
  def decideAgent( matched_deviceType: Long, decideCompany: Array[Agent] => WOption[List[String ], Agent ], agents: Array[Agent])
                 ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String ], Agent ] = {

    val candidates = agents.filter( _.sensorId == matched_deviceType)

    candidates.length match {
      case 0 =>
        wOptionLogOn.log(s"[ decideAgent ] this case can't happen.. matched_deviceType = $matched_deviceType ") ~>
          None

      case 1 =>
        wOptionLogOn.log(s"[ decideAgent ] agent = ${candidates.map(c => (c.companyId, c.companyName, c.vendorName)).head} ") ~>
          Some( candidates.head)

      case n =>
        wOptionLogOn.log(s"""[ decideAgent ] too many agents ($n) = ${candidates.map(c => (c.companyId, c.companyName, c.vendorName)).mkString(":")} """) +>
          decideCompany( candidates)
    }
  }

  /** Retrieves the rule that assigns the matched result to the normalized Field. */
  def findArrangeRule( tokenizeRuleId: Int, matchedFieldCount: Int, ref: ParserRef)
                     ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[ArrangeRule]] = {

    val ret =

      ref.arrangeRules
        .flatMap( _.get( tokenizeRuleId))
        .map { mr =>
          if ( mr.length == matchedFieldCount )
            wOptionLogOn.log(s"[ findArrangeRule ] $tokenizeRuleId") ~> Some(mr)
          else
            wOptionLogOn.log(s"[ findArrangeRule ] invalid ( arrangeRule_count != matched_count ) * ${(tokenizeRuleId, matchedFieldCount)}") ~> None

        }.getOrElse (wOptionLogOn.log(s"[ findArrangeRule ] not found * $tokenizeRuleId") ~> None)

    ret
  }


  /** Assigns the Tokenized result to the normalization field. */
  def arrangeResult( matched : Regex.Match, rule: Array[ArrangeRule])
                   ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], Array[(Int, String)]] = {

    WOption.sequence{
      val arr =
        rule.map { mr =>

          val from = mr.captureOrder
          val to = mr.fieldId
          val itm = lift( matched.group ( from))

          wOptionLogOn.log(s"[ arrangeResult ] : ${(from, to, itm)}") ~> itm.map ( to -> _ )
        }
      arr
    }
  }

  /** Attempts to match regular expressions.
    *
    * Returns the first matched regular expression result among a number of regular expressions.
    * */
  def tryTokenizeRules( body: String, rules: Array [TokenizeRule] )
                      ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String ], (TokenizeRule, Regex.Match) ] = {

    ( for {

      ri <- rules.toIterator
      mi = ri.regEx.findFirstMatchIn( body)
      if mi.nonEmpty

    } yield {
      wOptionLogOn.log(
        s"""[ tryTokenizeRules ] ${(ri.sensorType, ri.sensorId)} in ${rules.map(r=> (r.sensorType, r.sensorId)).mkString(":")}""") ~>
        Some ( ri, mi.get )

    } ).toStream.headOption getOrElse(
      wOptionLogOn.log(
        s"""[ tryTokenizeRules ] fail : ${rules.map( r=> (r.sensorType, r.sensorId)).mkString(":")}""".stripMargin) ~>
        None
      )
  }

  /** Gets the regular expression to apply.
    *
    * Gets the regular expression of the sensor connected to the selected agent as a candidate.
    * [Agent].sensorId = [TokenizeRule].sensorId
    * */
  def filterTokenizeRules( agents: Array[Agent ], ref: ParserRef)
                         ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String ], Array[TokenizeRule ] ] = {

    val rules = ref.tokenizeRules
    val ret = TokenizeRule.filter( agents, rules)


    ret.length match {
      case 0 =>
        wOptionLogOn.log(
          s"""[ lookupDeviceRules ] not found * agent: ${agents.map(h => (h.sensorId, h.sensor)).mkString(":")}""") ~>
          None

      case 1 =>
        wOptionLogOn.log(
          s"""[ lookupDeviceRules ] ( type, name) = ${ret.map(r => (r.sensorId, r.sensorType)).mkString(":")}""") ~>
          Some(ret)

      case n =>
        wOptionLogOn.log(
          s"""[ lookupDeviceRules ] too many (# $n) * ${agents.map(h => (h.sensorId, h.sensor)).mkString(":")}""") ~>
            Some(ret)
    }
  }

  /** Search for agents in the Agent category.
    * Agent discovery uses hostname.
    */
  def filterAgents( kind: SOCDeviceKind.Value, hint: Option[Array[String]], ref: ParserRef)
                  ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String ], Array[Agent ] ] = {

    hint.flatMap{ ar =>

      val ret = kind match {
        case SOCDeviceKind.normal => Agent.filter(ar(0), ref.mapHostAgent)
      }

      ret.map { r =>

        r.length match {
          case 0 =>
            wOptionLogOn.log(
              s"""[ filterAgents ] not found * hints: $kind : ${ar.mkString( ":" )}""" )~>
              None

          case 1 =>
            wOptionLogOn.log(
              s"""[ filterAgents ] host ( $kind : ${ar.mkString( ":" )} )""" )~>
              Some( r )

          case n =>
            wOptionLogOn.log(
              s"""[ filterAgents ] too many found (# $n ) * $kind : ${ar.mkString( ":" )}""" )~>
              Some( r )
        }
      }

    } getOrElse {
      wOptionLogOn.log( s"""[ filterAgents ] not defied hint""") ~> None
    }
  }


  def SOCDeviceKindHint( host: String, body: String, ref: ParserRef)
                       ( implicit wOptionLogOn: WOptionLogOn)

  : WOption[List[String ], ( SOCDeviceKind.Value, Option[Array[String ] ]) ] = {

      wOptionLogOn.log( s"[ SOCDeviceTypeHint ] $host") ~> Some( SOCDeviceKind.normal -> Some( Array(host) ) )
  }
}

