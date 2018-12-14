package com.adenium.common

import com.adenium.common.FieldFormatter.DatePath
import Keys.m_LOGTIME
import Keys.m_COMPANYID
import Keys.m_AGENTID
import com.adenium.utils.May.warn


trait FieldGetter {
  def get( mid: Int): Option[String]

  lazy val logTime: Option[String] = get( m_LOGTIME )
  lazy val company: Option[String] = get( m_COMPANYID )
  lazy val agentId: Option[String] = get( m_AGENTID )
  lazy val companyId: Int = warn( company.map(_.toInt) )("Invalid CompanyId") getOrElse -1
}

