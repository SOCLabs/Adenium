package com.adenium.app.framework

import com.adenium.utils.Args.args2map
import com.adenium.utils.StringUtil

/**
  * Defines an AdeniumControl message.
  *
  * @param message
  * @param command
  */
case class AdeniumMessage( message: String, command: String) {

  private lazy val map: Option[Map[String, Array[String]]] = StringUtil.lift(message)
      .map( _.split(" "))
      .map( args2map ( _, command))

  /**
    *
    * @param cmd
    * @return
    */
  def getOpt( cmd: String): Option[Array[String]] = map.flatMap( _.get( cmd) )

}
