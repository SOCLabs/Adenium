package com.adenium.utils

import May.maybe

/**
  * Ip address handling class
  */
object IpUtil {

  /**
    * ip --> Long
    * @param ip
    * @return
    */
  def ip2Long(ip: String): Option[Long] = {

    maybe {

      val arr = ip split "\\."
      if (arr.length == 4) {

        val ret =
          arr(0).trim.toLong * math.pow(2, 24).toLong +
          arr(1).trim.toLong * math.pow(2, 16).toLong +
          arr(2).trim.toLong * math.pow(2, 8).toLong +
          arr(3).trim.toLong

        Some(ret)
      }
      else None

    }.flatten
  }

  /**
    * RFC 1918 private IPv4 address space
    *
    {{{
        RFC1918 name      IP address range            number of addresses
        24-bit block      10.0.0.0–10.255.255.255     16777216
        20-bit block      172.16.0.0–172.31.255.255   1048576
        16-bit block      192.168.0.0–192.168.255.255 65536
    }}}

    [[https://en.wikipedia.org/wiki/Private_network]]
    */
  val privateIPs: Array[(Long, Long)] = Array[ (Long, Long)] (
    ip2Long( "10.0.0.0").get -> ip2Long( "10.255.255.255").get,
    ip2Long( "172.16.0.0").get -> ip2Long( "172.31.255.255").get,
    ip2Long( "192.168.0.0").get -> ip2Long( "192.168.255.255").get,
    ip2Long( "127.0.0.1").get -> ip2Long( "127.255.255.254").get
  )

  val invalidIps: Array[Long] = Array[Long](
    ip2Long("0.0.0.0").get,
    ip2Long("255.255.255.255").get
  )

  def isSpecialIp( ip: Long): Boolean = privateIPs.exists(r => r._1 <= ip && ip <= r._2 )
  def isInvalidIp( ip: Long): Boolean = invalidIps.contains( ip )

  def ip2PublicLong( ip: String): Option[Long] = ip2Long(ip).find { ip => !isSpecialIp(ip) && !isInvalidIp(ip) }
}

