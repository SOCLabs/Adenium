package com.adenium.parser.structs

case class CompanyIp( companyId: Long,
                      publicIp: String,
                      privateIp: String="")

object CompanyIp {

  def isCompanyIp(company: Long, companyIps: Option[Map[Long, Array[CompanyIp]]], ips: String*)
  : Boolean = {

    val ret =
      companyIps.exists(
        _.get(company).exists { ar =>
          ips.exists { ip =>
            ar.exists(e =>
              e.publicIp == ip || e.privateIp == ip)
          }
        })

    ret
  }

}