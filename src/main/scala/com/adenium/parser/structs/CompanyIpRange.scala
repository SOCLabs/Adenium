package com.adenium.parser.structs

import com.adenium.utils.Logger
import com.adenium.utils.IpUtil.ip2Long

/** Agent's owner IP band.
  *
  * In case of shared equipment, shared zone, it is used as information for determining owner after normalization
  *
  * When a plurality of pieces of sensor information are registered in the same agent ID,
  * The owner is determined by comparing whether the "Source IP" or "Dest IP" of the normalization field is included in the IP range.
  *
  * @constructor
  * @param companyId
  * @param sip
  * @param eip
  */
case class CompanyIpRange( companyId: Long,
                           sip:Long,
                           eip:Long )

object CompanyIpRange {

  def apply( cid: Long, startIP: String, endIP: String)
  : CompanyIpRange = {

    val sip = ip2Long(startIP).getOrElse {
      Logger.logWarning( s"""CompanyIpRange : company's sip is not ip format = ${(cid, startIP)}""")
      0L
    }

    val eip = ip2Long(endIP).getOrElse{
      Logger.logWarning( s"""CompanyIpRange : company's eip is not ip format = ${(cid, endIP)}""")
      0L
    }

    CompanyIpRange( cid, sip, eip)
  }

  def isCompanyIp( ip: String, arr : Array[CompanyIpRange]): Boolean =
  {
    ip2Long(ip).exists { i =>
      arr.exists( p => p.sip <= i && i <= p.eip )
    }
  }


  def isCompanyIp(company: Long, ranges: Option[ Map[Long, Array[CompanyIpRange]]], ips: String*)
  : Boolean = {

    val check =
    ranges.exists {
      _.get(company).exists { ar =>
        ips.exists { ipStr =>
          isCompanyIp( ipStr, ar)
          }
        }
      }
    check
  }

  /** If Ip is in the corporate ip range, the "Direction" in the normalization field is determined as IN. */
  def locationString ( isCompanyIp: Boolean): String = if ( isCompanyIp) "IN" else "OUT"

}