package com.adenium.app.config

import com.adenium.utils.Args.args2map
import com.adenium.utils.Logger

/**
  * Control the execution parameters and options of the Adenium framework.
  * @param args arguments
  * @param keywords Predefined Option key
  */
case class Opts( args: Array[String], keywords: String = Opts.keywords ) {

  private lazy val optMap: Map[String, Array[String ] ] = args2map( args, keywords )

  private lazy val log: String = optMap
    .map( v => v._1 -> v._2.mkString("\t"))
    .mkString( "\n=== Opts ( option -> value ) ===\n", "\n", "\n=============================" )

  def getOption( opt: String): Option[String] = optMap.get( opt).map(_.mkString(",") )

  //////////////////////////////////////////////////
  lazy val zkstr: String = get( "zk:conn" )
  lazy val kfstr: String = get( "kf:broker")
  lazy val kfostr: Option[String] = getOption( "kf:outbroker")
  lazy val topic: String = get( "kf:topic")
  lazy val app: String= get( "sp:app")

  //////////////////////////////////////////////////

  /**
    * Outputs the received options.
    * @param head
    */
  def writeLog(head: String): Unit = Logger.logInfo( head + log)

  /** Gets the value of the option.
    *
    * @param opt option key
    * @param default default value
    * @return option value
    */
  def get( opt: String, default: Option[String] = None ): String =
    optMap.get( opt).map( _.mkString(",") )
      .getOrElse {
        default
          .getOrElse  {
            throw new Exception(s"""\n$opt in not set. (${args.mkString("\t")})\n($optMap)\n""")
            "check_options"
          }
      }
}

/**
  * Predefined Option key
  */
object Opts {

  val keywords: String =
      "sp:master|sp:app|sp:duration|sp:bulk|" +
      "zk:conn|zk:ctrl|" +
      "kf:broker|kf:outbroker|kf:topic|kf:ctrl|kf:restore|kf:save|kf:out_topic|kf:err_topic|"
}