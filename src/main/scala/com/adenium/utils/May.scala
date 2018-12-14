package com.adenium.utils
/**
  * Exception handling class
  */

object May {

  def memo( str: => String): Unit = { Logger.logWarning(s"[ Memo ] $str ") }

  def state( str: => String): Unit = { Logger.logWarning(s"[ State ] ========== $str ==========") }

  /**
    * f : function returning value t of type T, which can throw an exception.
    * log : exception log string
    *
    * @param f
    * @param log
    * @tparam A
    * @return
    */
  def warn[A]( a : => Option[A])(str : => String)
  : Option[A] = {
    if ( a.isEmpty)
      Logger.logWarning(s"[ Warn ] : None = $str")

    a
  }

  /**
    *
    * @param a
    * @tparam A
    * @return
    */
  def maybe[A](a : => A)
  : Option[A] = {
    try Some(a) catch { case _: Throwable => None }
  }

  def maybeInfo[A](a : => A)(str: => String)
  : Option[A] = {
    try Some(a) catch { case e: Throwable => Logger.logWarning( "[ maybeInfo ]" + str ); None }
  }

  def maybeWarn2[A](a : => A)( str: String = "[ maybeWarn ]")
  : Option[A] = {
    try Some(a) catch { case e: Throwable => Logger.logWarning( str, e ); None }
  }

  def maybeWarn[A](a : => A, str: => String = "[ maybeWarn ]")
  : Option[A] = {
    try Some(a) catch { case e: Throwable => Logger.logWarning( str, e ); None }
  }

  /**
    *
    * loan pattern
    * r : Closable Resource which may throw an exception.
    * f : function using closable resource r
    * needClose : close after f.
    *
    * @param r
    * @param needClose
    * @param f
    * @tparam A
    * @return
    */
  def using[A](r : => AutoCloseable, needClose: Boolean = true)(f : AutoCloseable => A)
  : Option[A] = {

    maybeWarn( r ).flatMap { rc =>
      try {
        Some( f( rc ) )
      } catch {

        case e: Throwable =>
          Logger.logInfo( "[ using ]", e )
          None

      } finally {

        if ( needClose )
          maybeWarn( rc.close() )

      }
    }
  }

  /**
    *
    * @param str
    * @return
    */
  def lift[A, B]( f: A => B)
  : A => Option[B] = (a: A) => maybe( f( a))

}

