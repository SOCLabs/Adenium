package com.adenium.parser.reference

import com.adenium.common.Field
import com.adenium.parser.structs._
import com.adenium.utils.Logger

import scala.language.implicitConversions

/** Reference data for normalization
  *
  - mapHostAgent: Map of host ( ip or name )
  - mapAgent: Map of id
  - tokenizeRules: Regular expressions [[com.adenium.parser.structs.TokenizeRule]]
  - arrangeRules: Rules that link token fields to normalization fields. [[com.adenium.parser.structs.ArrangeRule]]
  - companyIpRange: Agent owner ip ranges [[com.adenium.parser.structs.CompanyIpRange]]
  - companyIps: Owner server IP of the installed agent [[com.adenium.parser.structs.CompanyIp]]
  - geoIpRange: Ip by country [[com.adenium.parser.structs.GeoIpRange]]
  - signatures: Signatures of sensor [[com.adenium.parser.structs.Signature]]
  - replaceFields: Changeable field [[com.adenium.parser.structs.ReplaceField]]
  - fields: Normalization fields [[com.adenium.common.Field]]
  */
case class ParserRef (mapHostAgent: Option[Map[Long, Array[ Agent]]] = None,
                      mapAgent: Option[Map[Long, Array[Agent]] ] = None,
                      tokenizeRules: Option[Map[Long, Array[TokenizeRule]]] = None,
                      arrangeRules: Option[Map[Int, Array[ArrangeRule]]] = None,
                      companyIpRange: Option[Map[Long, Array[CompanyIpRange]]] = None,
                      companyIps: Option[Map[Long, Array[CompanyIp]]] = None,
                      geoIpRange: Option[Map[Long, Array[GeoIpRange]]] = None,
                      signatures: Option[Map[(String, Long), Signature]] = None,
                      replaceFields: Option[Map[Int, Array[ ReplaceField]]] = None,
                      fields: Option[Array[Field]] = None )
{

  /** Add reference data */
  def <+(update : ParserRef ): ParserRef = {

    ParserRef (
      update.mapHostAgent orElse this.mapHostAgent,
      update.mapAgent orElse this.mapAgent,
      update.tokenizeRules orElse this.tokenizeRules,
      update.arrangeRules orElse this.arrangeRules,
      update.companyIpRange orElse this.companyIpRange,
      update.companyIps orElse this.companyIps,
      update.geoIpRange orElse this.geoIpRange,
      update.signatures orElse this.signatures,
      update.replaceFields orElse this.replaceFields,
      update.fields orElse this.fields
    )
  }
}

object ParserRef {

  def logStatus ( ref: ParserRef ): String = {

    val logStr =
      s"""[ParserRef] geoIpRange: ${ref.geoIpRange.map(_.keys.size)}""" +
        s"""[ParserRef] companyIpRange: ${ref.companyIpRange.map(_.values.size)}""" +
        s"""[ParserRef] signatures: ${ref.signatures.map(_.values.size)}"""  +
        s"""[ParserRef] mapHostnameAgent: ${ref.mapHostAgent.map(_.values.size)}"""   +
        s"""[ParserRef] mapAgent: ${ref.mapAgent.map(_.values.size)}"""   +
        s"""[ParserRef] ruleSetInfo  : ${ref.tokenizeRules.map(_.values.size)}"""  +
        s"""[ParserRef] matchingRuleFieldInfo  : ${ref.arrangeRules.map(_.values.size)}"""  +
        s"""[ParserRef] FieldInfo  : ${ref.fields.map(_.length)}"""  +
        s"""[ParserRef] FieldReplace : ${ref.replaceFields.map(_.values.size)}"""  +
        s"""[ParserRef] companyServerInfo  : ${ref.companyIps.map(_.valuesIterator.length)}"""

    Logger.logInfo( logStr)

    logStr
  }

  def initialize(maker: ParserRefMaker): ParserRef = {
    maker.initialize()
  }

  /////////////////////////////////////////////////////////////////////
  implicit def agentRef( r: Option[Array[ Agent]] )
  : ParserRef = {
    r.map { arr =>

      val mapIp = arr.groupBy( _.ipLong )
      val mapId = arr.groupBy( _.agentId )

      ParserRef( mapHostAgent = Some(mapIp), mapAgent = Some( mapId))

    }.getOrElse( ParserRef() )
  }

  implicit def rules2Tokenize2Ref  ( r: Option[ Array[TokenizeRule]] )
  : ParserRef = ParserRef( tokenizeRules = r.map(_.groupBy( _.sensorId)) )

  implicit def rules2Arrange2Ref   ( r: Option[ Array[ArrangeRule]])
  : ParserRef = ParserRef( arrangeRules = r.map(_.groupBy(_.tokenizeRuleId)))

  implicit def companyIpRange2Ref  ( r: Option[ Array[CompanyIpRange]])
  : ParserRef = ParserRef( companyIpRange = r.map(_.groupBy( _.companyId)) )

  implicit def companyIps2Ref      ( r: Option[Array[CompanyIp]])
  : ParserRef = ParserRef( companyIps = r.map(_.groupBy(_.companyId)) )


  implicit def geoIpRange2Ref      ( r: Option[Map[Long, Array[GeoIpRange]]])
  : ParserRef = ParserRef( geoIpRange = r)

  implicit def signatures2Ref      ( r: Option[Array[Signature]])
  : ParserRef = ParserRef( signatures = r.map{ e =>
    e.groupBy( ds => (ds.signature, ds.vendor))
      .map( e => ( e._1, e._2.head ) )
  } )

  implicit def replaceFields2Ref   ( r: Option[Array[ ReplaceField]])
  : ParserRef = ParserRef( replaceFields = r.map(_.groupBy(_.mid)))

  implicit def Fields2Ref          ( r: Option[Array[Field]])
  : ParserRef = ParserRef(fields = r)


}