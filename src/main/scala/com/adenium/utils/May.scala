package com.adenium.utils

/**
  * Exception handling class
  */

object May {

  def memo( memoString: => String): Unit = { Logger.logWarning(s"[ Memo ] $memoString ") }
  def state( stateString: => String): Unit = { Logger.logWarning(s"[ State ] ========== $stateString ==========") }

  /**
    * f : function returning value t of type T, which can throw an exception.
	  * log : exception log string
    *
    * @param f
    * @param log
    * @tparam A
    * @return
    */
  def warn[A]( f : => Option[A])(log : => String)
  : Option[A] = {
    f
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

  def maybeWarn[A](a : => A, str: String = "[ maybeWarn ]")
  : Option[A] = {
    try Some(a) catch { case e: Throwable => Logger.logWarning( str, e ); None }
  }

  //////////////////////////////////////////////////////////////////
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
  def lift( str: String ): Option[String ] = {
    if ( str == null || str.isEmpty )
      None
    else
      Some( str)
  }

  /**
    * f : function which may throw an exception.
    *
    * @param f
    * @tparam A
    * @tparam B
    * @return
    */
  def lift[A, B]( f: A => B): A => Option[B] = (a: A) => maybe( f( a))


  def unescapeUnicode(str: String): String =

    """\\u+([0-9a-fA-F]{4})""".r.replaceAllIn(str,
      m => Integer.parseInt(m.group(1), 16).toChar match {
        case '\\' => """\\"""
        case '$' => """\$"""
        case c => c.toString
      })

  def unescapeString( s: String): String = {

    maybe {
      StringContext.treatEscapes(unescapeUnicode(s))
    } getOrElse s

  }
}

