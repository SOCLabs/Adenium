package com.adenium.parser.structs

/** Agent's owner IP ( public, private).
  *
  * It is used as information to determine the owner IP information of the installed agent and the finalized agent of the normalized result.
  * If the host information of the result of normalization matches the company ip, it is determined to be the equipment of the matching company.
  *
  * @constructor
  * @param companyId
  * @param publicIp
  * @param privateIp
  */
case class CompanyIp( companyId: Long,
                      publicIp: String,
                      privateIp: String)

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