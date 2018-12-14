package com.adenium.app.config

/**
  * Defines the path of the ZooKeeper node where the control messages and reference information of the Adenium framework are stored.
  * @param appName application name
  * @param versionRoot
  */
case class ConfZkPaths( appName: String, versionRoot: String= "/Adenium/2.0") {

  /**
    * Zookeeper Properties structure
    *
    * VersionRoot
    *  |-- app
    *  |   |-- watch
    *  |   |-- offsets
    *  |   |-- common               // added 10.17, g3nie
    *  |   |   |-- var_fields       // moved from ( normalizer -- parser_ref -- fields )
    *  |   |-- normalizer
    *  |   |   |-- parser_log
    *  |   |   |-- parser_ref
    *  |   |   |   |-- geo_ip_range
    *  |   |   |   |-- company_ip_range
    *  |   |   |   |-- signatures
    *  |   |   |   |-- agents
    *  |   |   |   |-- tokenize_rules
    *  |   |   |   |-- arrange_rules
    *  |   |   |   |-- fields       <----- may need to be located in other path...
    *  |   |   |   |-- replace_fields
    *  |   |   |   |-- company_ips
    */

  val app: String = versionRoot + "/" +  "app"

  val watch: String = app + "/" +  "watch" + "/" + appName
  val offsets: String = app + "/" +  "offsets" + "/" + appName
  val common: String = app + "/" +  "common"
  val varfields: String = app + "/" +  "var_fields"     // added 10.17

  val normalizer: String = app + "/" +  "normalizer"
  val parserlog: String = normalizer + "/" +  "parser_log"
  val parser: String = normalizer + "/" +  "parser_ref"
  val geoip: String = parser + "/" +  "geo_ip_range"
  val cipr: String = parser + "/" +  "company_ip_range"
  val sig: String = parser + "/" +  "signatures"
  val agn: String = parser + "/" +  "agents"
  val tokrul: String = parser + "/" +  "tokenize_rules"
  val arrrul: String = parser + "/" +  "arrange_rules"
  val fields: String = parser + "/" +  "fields"         // <-- deprecating 10.17, g3nie
  val repfld: String = parser + "/" +  "replace_fields"
  val cips: String = parser + "/" +  "company_ips"


}
