package com.adenium.parser.structs

case class CompanyHost( companyId: Long,
                        name: String,
                        ip: String)

object CompanyHost {

  def hostname2Ip( company: Long, hostname: String)( companyHost2Ip: Option[Map[Long, Array[CompanyHost]]])
  : Option[String]
  = {
    val ret =
      companyHost2Ip.flatMap (
        _.get(company).flatMap (
          _.find( _.name == hostname) )).map( _.ip)

    ret
  }
}