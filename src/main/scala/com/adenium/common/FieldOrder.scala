package com.adenium.common

import com.adenium.common.Keys._

import scala.collection.immutable.HashMap

/**
  * Defines the order of the reserved fields.
  * This sequence is used to map the capture order of the regular expression.
  *
  * @param order
  * @param hashmap
  * @param size
  */
case class FieldOrder( order : Array[ Int],
                       hashmap: HashMap[Int, Int],
                       size: Int) {

  def contains( int: Int) : Boolean = hashmap.contains(int)
}

object FieldOrder {

  def apply( order: Array[Int]): FieldOrder = {

    val map = HashMap( order.zipWithIndex: _*)
    val size = order.length

    new FieldOrder( order, map, size)
  }

  def makeOrElse(maybe: Option[Array[Int]], default: FieldOrder): FieldOrder = {
    maybe.map( FieldOrder.apply ) getOrElse default
  }

  private val oldTSVOrder: Array[ Int] = Array (
    m_CATEGORY1,m_CATEGORY2,m_CATEGORY3,m_SIGNATURE ,m_SEVERITY,m_COUNT,m_REPEATCOUNT,m_SRCIP,m_SRCPORT,m_SRCMAC,
    m_SRCCOUNTRY,m_DESTIP,m_DESTPORT,m_DESTMAC,m_DESTCOUNTRY,m_SRCDIRECTION,m_DESTDIRECTION,m_URL,m_URI,m_URIPARAMS,
    m_HEADER,m_PROTOCOL,m_PAYLOAD,m_CODE,m_RCVDBYTES,m_SENTBYTES,m_MESSAGEID,m_SRCZONE,m_DESTZONE,m_SERVICE,m_DURATION,
    m_ACLNM,m_ACTION,m_RAWDATA,m_SENDER,m_ATTACHMENT,m_STARTATTACKTIME,m_ENDATTACKTIME,m_LOGTIME,m_SYSLOGTIME,
    m_SYSLOGHOST,m_AGENTID,m_AGENTIP,m_COMPANYID,m_COMPANYNM,m_COMPANYGROUPID,m_DEVICETYPE,m_DEVICEMODEL,m_VENDOR )

}




