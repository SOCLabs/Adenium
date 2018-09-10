package com.adenium.parser.structs

import scala.util.matching.Regex

/**
  * Regular expression for tokenization.
  *
  * @param id ID to identify the regular expression
  * @param sensorId Id of the sensor to which the regular expression is applied
  * @param sensorType Sensor Type
  * @param regEx Regular expression
  */
case class TokenizeRule(id: Int,
                        sensorId: Long,
                        sensorType: String,
                        regEx: Regex
                       )

object TokenizeRule {

  def apply(id: Int, sensorId: Long, sensorType: String, regex: String )
  : TokenizeRule = {
    TokenizeRule( id, sensorId, sensorType, regex.r )
  }

  def filter( agents: Array[Agent], rules: Option[Map[Long, Array[TokenizeRule]]])
  : Array[ TokenizeRule] = {

    // agentTypeID de-dupulication : to be deleted later.. after DB refining
    val uniques = agents.groupBy( _.sensorId).values.map( _.head).toArray

    val ret =
      uniques.flatMap { agn =>
        rules.flatMap{ rls =>
          rls.get( agn.sensorId )
        }
      }.flatten

    ret
  }
}