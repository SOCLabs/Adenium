package com.adenium.utils

/**
  * Created by SecudiumDev on 2017-10-18.
  */

object Logger extends Serializable {

  //ref => https://www.mail-archive.com/user@spark.apache.org/msg29010.html

  /**
    * error   : break conditions
    * warning : warning or important, main process's flow, one-time job.. stack tracing
    * info    : optional info, sub-routine's flow
    * debug   :
    * trace
    *
    */

  @transient private lazy val log = org.apache.log4j.Logger.getLogger( getClass.getName)

  def logWarning( str: String ): Unit = log.warn( str)
  def logWarning( str: String, e: Throwable): Unit = log.warn( str, e)
  def logInfo( str: String, e: Throwable): Unit = log.info( str, e)
  def logInfo( str: String): Unit = log.info( str)
  def logDebug( str: String ): Unit = log.debug( str)

}
