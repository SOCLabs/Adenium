package com.adenium.parser.structs

import com.adenium.utils.Logger
import com.adenium.utils.IpUtil.ip2Long

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

  def locationString ( isCompanyIp: Boolean): String = if ( isCompanyIp) "IN" else "OUT"

}