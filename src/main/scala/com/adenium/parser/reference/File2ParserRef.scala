package com.adenium.parser.reference

import com.adenium.common.Field
import com.adenium.common.Keys.getKey
import com.adenium.parser.reference.File2ParserRef.ParserRefFiles
import com.adenium.parser.structs._
import com.adenium.utils.IpUtil.ip2Long
import com.adenium.utils.May.{lift, maybe, maybeWarn}

import scala.reflect.ClassTag

/** Initialize reference data from file
  *
  * Generates reference information for parsing from a file.
  * This text uses the default tab delimiters and creates an array one line at a time.
  * You can change the DataSource by inheriting ParserRefMaker. (DB, zookeeper, stream, memory ..)
  *
  * */
object File2ParserRef {

  /** reference data
    *
    * @constructor
    * @param agents_fn Agent.
    * @param companyIpRange_fn Agent's owner IP band.
    * @param companyIps_fn Agent's owner IP ( public, private).
    * @param geoIPRange_fn Country-specific IP range.
    * @param signatures_fn Message signatures of sensor.
    * @param replaceFields_fn Replace fields.
    * @param tokenizeRules_fn Regular expression for tokenization.
    * @param arrangeRules_fn The rule that links the tokenization result to the normalization field.
    * @param Fields_fn Normalization fields.
    */
  case class ParserRefFiles(agents_fn : String = "agentInfo.ref",
                            companyIpRange_fn : String = "companyIpRange.ref",
                            companyIps_fn : String = "companyServerIp.ref",
                            geoIPRange_fn : String = "geoIpRange.ref",
                            signatures_fn : String = "signatures.ref",
                            replaceFields_fn : String = "replaceFields.ref",
                            tokenizeRules_fn : String = "tokenizeRules.ref",
                            arrangeRules_fn : String = "arrangeRules.ref",
                            Fields_fn : String = "fields.ref")

  object ParserRefFiles {
    def apply( path: String): ParserRefFiles = {

      ParserRefFiles ( agents_fn = path + "agentInfo.ref",
        companyIpRange_fn = path + "companyIpRange.ref",
        companyIps_fn = path + "companyServerIp.ref",
        geoIPRange_fn = path + "geoIpRange.ref",
        signatures_fn = path + "signatures.ref",
        replaceFields_fn = path + "replaceFields.ref",
        tokenizeRules_fn = path + "tokenizeRules.ref",
        arrangeRules_fn = path + "arrangeRules.ref",
        Fields_fn = path + "fields.ref"
      )
    }
  }
}

/** Generates reference information from ParserRefFiles */
case class File2ParserRef(  ref : ParserRefFiles) extends ParserRefMaker {

  /** Reads the specified file and creates a DATA array.
    *
    * You can use the drop Header option to delete header text.
    *
    * @param filename reference data file.
    * @param dropHeader true : Exclude the first line. : Include the first line.
    * @param separator text separator
    * @param f Handling Function
    * @tparam T
    * @return
    */
  def file2Array[T:ClassTag]( filename: String,
                              dropHeader: Boolean = true,
                              separator: String= "\t")
                            ( f: Array[String] => Option[T])
  : Option[Array[T]] = {

    maybeWarn {
      val sr = scala.io.Source.fromFile(filename, "UTF-8")
      val itr = if( dropHeader ) sr.getLines.drop(1) else sr.getLines
      itr.flatMap{ ln => f( ln.split(separator)) }.toArray
    }
  }

  /** Make referece data from file */
  def makeRefs[T:ClassTag] ( filename: String)( f: Array[String] => T)
  : Option[Array[T]] = {
    file2Array(filename)(lift(f))
  }

  /** Generate country-specific IP range reference information. */
  override def getGeoIPRange()
  : ParserRef = {

    val fn = ref.geoIPRange_fn

    makeRefs( fn) {
      case Array(a1, a2, a3) =>
        GeoIpRange( a1,
          a2.replaceAll(",", "").toLong,
          a3.replaceAll(",", "").toLong)

    }.map( _.groupBy(_.key))

  }

  /** Generate owner IP (public, private) information for the agent */
  override def getCompanyIps(): ParserRef = {

    val fn = ref.companyIps_fn

    makeRefs( fn) {
      case Array(a1, a2) =>
        CompanyIp(
          a1.toInt,
          a2,
          "")
      case Array(a1, a2, a3) =>
        CompanyIp(
          a1.toInt,
          a2,
          a3)
    }
  }

  /** Generate Agent's owner IP bands information */
  override def getCompanyIpRange(): ParserRef = {

    val fn = ref.companyIpRange_fn

    makeRefs( fn) {
      case Array(a1, a2, a3) =>
        CompanyIpRange(
          a1.toInt,
          ip2Long(a2).getOrElse(0L),
          ip2Long(a3).getOrElse(0L))
    }
  }

  /** Generates message signature information */
  override def getSignatures(): ParserRef = {

    val fn = ref.signatures_fn

    makeRefs( fn) {
      case Array(a1, a2, a3, a4, a5, a6) =>
        Signature( a1.toLong,
          a2,
          a3,
          a4,
          a5,
          a6)

    }
  }

  /** Generates agent information */
  override def getAgents(): ParserRef = {

    val fn = ref.agents_fn

    makeRefs( fn) {
      case Array( a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11) =>
        Agent(a1,
          a2.toInt,
          a3.toLong,
          a4,
          a5.toLong,
          a6.toLong,
          a7,
          a8,
          a9.toLong,
          a10,
          a11 == "Y"
        )
    }
  }

  /** Generates regex information */
  override def getTokenizeRules(): ParserRef = {

    val fn = ref.tokenizeRules_fn

    // agent_type_id	ruleId, regEx, agentTypeName

    makeRefs( fn) {
      case Array( a1, a2, a3, a4 ) =>
        TokenizeRule(
          maybe(a1.toInt)getOrElse 0,
          maybe(a2.toLong)getOrElse 0L,
          a3,
          a4)
    }
  }

  /** The rule that links the tokenization result to the normalization field. */
  override def getArrangeRules(): ParserRef = {

    val fn = ref.arrangeRules_fn

    makeRefs( fn) {
      case Array(a1, a2, a3) =>
        ArrangeRule(
          a1.toInt,
          a2.toInt,
          a3.toInt)
    }
  }

  /** Generates rule information that links the tokenization result to the normalization field. */
  /**
    * todo :
    * check : getKey ==> get reserved Key, or create user-defined one..
    *
    * @return
    */
  override def getFields(): ParserRef = {

    val fn = ref.Fields_fn

    makeRefs( fn) {
      case Array( mid, fname) =>
        Field( getKey( mid.toInt, fname), None)

    }

  }

  /** Replaceable fields*/
  override def getReplaceFields(): ParserRef =  {

    val fn = ref.replaceFields_fn

    makeRefs( fn) {
      case Array(a1, a2, a3, a4) =>
        ReplaceField(
          a1.toInt,
          a2,
          a3,
          a4.toLong
          )
    }
  }


}


