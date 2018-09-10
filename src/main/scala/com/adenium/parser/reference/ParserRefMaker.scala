package com.adenium.parser.reference

/**
  * RefMaker trait
  *
  * You can change the DataSource by inheriting ParserRefMaker. (DB, zookeeper, stream, memory ..)
  *
  */

trait ParserRefMaker {

  def getGeoIPRange(): ParserRef
  def getCompanyIpRange(): ParserRef
  def getSignatures(): ParserRef
  def getAgents(): ParserRef
  def getTokenizeRules(): ParserRef
  def getArrangeRules(): ParserRef
  def getFields(): ParserRef
  def getReplaceFields(): ParserRef
  def getCompanyIps(): ParserRef

  def initialize(): ParserRef = {
    getCompanyIpRange() <+
      getGeoIPRange() <+
      getSignatures() <+
      getAgents() <+
      getTokenizeRules() <+
      getArrangeRules() <+
      getCompanyIps() <+
      getFields() <+
      getReplaceFields()
  }
}
