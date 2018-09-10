package com.adenium.parser.structs

import com.adenium.utils.May.maybeWarn
import com.adenium.utils.IpUtil.ip2Long

/** Agent
  *
  * The Agent has "specific Sensor + Owner Information"
  *
  * @constructor
  * @param host System host information to identify the agent [ from syslog header: host]
  * @param ipLong Host to long type : string = 0L, ip = long
  * @param agentId Agent's Unique Identifiable ID
  * @param sensorType Sensor Type [ FW, WAF, DDOS, IPS, IDS ... ]
  * @param companyId ID of Agent owner
  * @param companyName Name of Agent owner
  * @param companyGroupId Group ID of Agent owner
  * @param sensorId Unique ID to identify the Sensor
  * @param sensor Model name of Sensor
  * @param vendorId Vendor ID of Sensor
  * @param vendorName Name of vendor
  * @param active use flag [ 'Y' or 'N' ]
  */
case class Agent(host: String,
                 ipLong:Long,
                 agentId: Long,
                 companyId: Long,
                 companyName: String,
                 companyGroupId: Long,
                 sensorId: Long,
                 sensor: String,
                 sensorType: String,
                 vendorId: Long,
                 vendorName: String,
                 active: Boolean)


object Agent {
  def apply(agentIp: String,
            agentId: Long,
            companyId: Long,
            companyName: String,
            companyGroupId: Long,
            sensorId: Long,
            sensor: String,
            sensorType: String,
            vendorId: Long,
            vendorName: String,
            active: Boolean): Agent =  {

    val ipLong = ip2Long( agentIp).getOrElse(0L)
    Agent(
      host= agentIp,
      ipLong= ipLong,

      agentId= agentId,
      sensorType= sensorType,

      active= active,
      companyId= companyId,
      companyName= companyName,
      companyGroupId= companyGroupId,
      sensorId= sensorId,
      sensor= sensor,
      vendorId= vendorId,
      vendorName= vendorName
    )
  }

  def filter( host: String, agent: Option[Map[Long, Array[Agent] ] ])
  : Option[Array[Agent]] = {

    val ret =
      for {
        m <- agent
        ipLong = ip2Long(host) getOrElse 0L
        ar <- m.get( ipLong)

      } yield {
        ar.filter ( _.host == host )
      }

    ret

  }

  //////////////////////////////////////////////////
  def filter( companyId: String, agentId: String, agent: Option[ Map[Long, Array[Agent]]])
  : Option[Array[Agent]] = {

    val ret =
      for {
        m <- agent
        cid <- maybeWarn { companyId.toLong }
        id <- maybeWarn { agentId.toInt }

        ar <- m.get( id)

      } yield {

        ar.filter( p => p.agentId == id && p.companyId == cid)
      }

    ret
  }

}