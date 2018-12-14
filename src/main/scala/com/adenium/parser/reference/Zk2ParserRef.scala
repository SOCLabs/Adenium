package com.adenium.parser.reference

import com.adenium.app.config.Conf
import com.adenium.common.{FieldKey, VariableKeys}
import com.adenium.externals.zookeeper.ZkUtil.readZKArray
import com.adenium.parser.structs._
import com.adenium.utils.IpUtil.ip2Long
import com.adenium.utils.Logger
import com.adenium.utils.May.{maybe, maybeWarn}
import org.apache.curator.framework.CuratorFramework

import scala.reflect.ClassTag

/** Initialize reference data from Zookeeper node.
  *
  * Generates reference information for parsing from zookeeper nodes.
  *
  * */
case class Zk2ParserRef( ref : ( Option[CuratorFramework], Conf ) ) extends ParserRefMaker {

  val curator: Option[CuratorFramework] = ref._1
  val conf: Conf = ref._2

  def makeRefs[T:ClassTag] ( path: String)( f: Array[String] => T)
  : Option[Array[T]] = {

    maybeWarn { readZKArray( curator)( path)( f ) }
  }

  /** Generate country-specific IP range reference information. */
  override def getGeoIPRange(): ParserRef = {

    val fn = conf.zkp.geoip

    makeRefs( fn ) {
      case Array(a1, a2, a3) =>
        GeoIpRange( a1,
          a2.replaceAll(",", "").toLong,
          a3.replaceAll(",", "").toLong )
    }.map( _.groupBy(_.key))
  }

  /** Generate owner IP (public, private) information for the agent */
  override def getCompanyIps(): ParserRef = {

    val fn = conf.zkp.cips

    // CustomerServerInfo
    // company_seq	public_ip	private_ip	service_active\n

    makeRefs( fn) {
      case Array(a1, a2, a3) =>
        CompanyIp(
          a1.toLong,
          a2,
          a3)
    }
  }

  /** Generate Agent's owner IP bands information */
  override def getCompanyIpRange(): ParserRef = {

    val fn = conf.zkp.cips

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

    val fn = conf.zkp.sig

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

    val fn = conf.zkp.agn

    //agent_seq	agent_ip	agent_type_id	active_flag	agent_type	company_seq	company_group_seq	agent_name	agent_vendor_name	company_name	service_id	agent_vendor_seq

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

    val fn = conf.zkp.tokrul
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

    val fn = conf.zkp.arrrul

    // idx	set_type	set_priority	column_idx	data_format	capture_order	order_index	column_name	column_data_type	is_trans
    makeRefs( fn) {
      case Array(a1, a2, a3) =>
        ArrangeRule(
          a1.toInt,
          a2.toInt,
          a3.toInt)
    }
  }

  /** Generate varfields information */
  override def getFields(): ParserRef = {

    val fn = conf.zkp.varfields

    /**
      *  fid    : field Id, (Integer)
      *  fname  : field name used in presenting a field by name, ( Alpha_Numeric_String)
      *  ftype  : field type, currently NOT USED.. YET. (default = FieldType.TString)
      *
      */
    val keys = makeRefs( fn) {
      case Array( fid, fname ) => FieldKey( fid.toInt, fname)
    }

    Some( VariableKeys(keys))
  }

  /** Replaceable fields*/
  override def getReplaceFields(): ParserRef =  {

    val fn = conf.zkp.repfld

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