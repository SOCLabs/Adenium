package com.adenium.utils

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.adenium.utils.May._

object Timer {

  def currentMillis: String = Instant.now().toEpochMilli.toString

  //////////////////////////////////////////////////

  def Timer[A] ( f : => A) : ( A, Long) = {
    val s = System.currentTimeMillis
    val r = f
    val e = System.currentTimeMillis
    ( r, e-s )
  }

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
