package com.adenium.parser.structs

import com.adenium.utils.IpUtil.ip2PublicLong

import scala.annotation.tailrec

case class GeoIpRange ( key: Long,
                        sid: Int,
                        eid: Int,
                        nation: String,
                        sip: Long,
                        eip: Long)

/////////////////////////////////

object GeoIpRange {

  private val factor = math.pow(2, 23).toLong   // 8,388,608

  def apply(name: String, sip: Long, eip: Long)
  : GeoIpRange = {

    val s = sip min eip
    val b = sip max eip

    val sid = math.floor((s / factor).toDouble).toInt
    val eid = math.floor((b / factor).toDouble).toInt
    val key = s >> 24

    GeoIpRange( key, sid, eid, name, sip, eip )

  }

  def search(arr: Array[GeoIpRange], ip: Long): Option[Int] = {

    @tailrec
    def go( lo: Int, hi: Int): Option[Int] = {
      if (lo > hi)
        None
      else {
        val mid: Int = lo + (hi - lo) / 2
        arr(mid) match {
          case range if range.sip <= ip && ip <= range.eip => Some(mid)
          case range if range.eip < ip => go(mid + 1, hi)
          case _ => go(lo, mid - 1)
        }
      }
    }

    go(0, arr.length - 1)
  }

  def getCountry(ip: Long, geoips: Map[Long, Array[GeoIpRange]] )
  : Option[String] = {

    val fac = math.floor((ip / factor).toDouble).toInt
    val key = ip >> 24

    for {
      arr <- geoips.get( key)
      idx <- search( arr, ip)
    } yield {
      arr( idx).nation
    }
  }

  def getCountry( ip: String, geoips: Option[Map[Long, Array[GeoIpRange]]] )
  : Option[String] = {

    val ret =
      for {
        ranges <- geoips
        ipLong <- ip2PublicLong( ip)
        country <- getCountry( ipLong, ranges)
      } yield {
        country
      }
    ret
  }
}

