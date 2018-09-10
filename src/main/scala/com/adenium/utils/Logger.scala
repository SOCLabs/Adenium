package com.adenium.utils

/**
  * Log handling class
  */

object Logger extends Serializable {

  /**
    - error   : break conditions
    - warning : warning or important, main process's flow, one-time job.. stack tracing
    - info    : optional info, sub-routine's flow
    - debug   :
    - trace
    */

  @transient private lazy val log = org.apache.log4j.Logger.getLogger( getClass.getName)

  def logWarning( str: String ): Unit = log.warn( str)
  def logWarning( str: String, e: Throwable): Unit = log.warn( str, e)
  def logInfo( str: String, e: Throwable): Unit = log.info( str, e)
  def logInfo( str: String): Unit = log.info( str)
  def logDebug( str: String ): Unit = log.debug( str)

}
