package com.adenium.utils

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.adenium.utils.May._

/** Time Util */
object Timer {

  /** get current millis*/
  def currentMillis: String = Instant.now().toEpochMilli.toString

  /** Date with Timezone*/
  def dateformatter( str: String): DateTimeFormatter = {
    DateTimeFormatter.ofPattern( str)
      .withZone( ZoneId.systemDefault() )
      .withLocale( Locale.ENGLISH )
  }

  /**
    * exception --> now
    * @param millis
    * @param dateTimeFormatter
    * @return
    */
  def formatMillis( millis: Long, dateTimeFormatter: DateTimeFormatter): String = {
    maybeInfo (
      dateTimeFormatter.format( Instant.ofEpochMilli( millis))
    )("fail so i'll use current.. ").getOrElse(
      dateTimeFormatter.format( Instant.now())
    )
  }
  //////////////////////////////////////////////////

  /** get Elapsed time*/
  def Timer[A] ( f : => A) : ( A, Long) = {
    val s = System.currentTimeMillis
    val r = f
    val e = System.currentTimeMillis
    ( r, e-s )
  }

  //** Logging with time*/
  def TimeLog[A]( f : => A)(msg: String): A = {
    val ( r, t) = Timer( f)
    Logger.logWarning( s"[ Time spent ] $t in $msg")
    r
  }

  def UnitTimer( f : => Unit) : Long = {
    val ( _, t) = Timer( f)
    t
  }
}
