package com.adenium.utils

import com.adenium.utils.May.maybe

object StringUtil {
  def lift( str: String ): Option[String ] = {
    if ( str == null || str.isEmpty )
      None
    else
      Some( str)
  }

  def unescapeUnicode(str: String): String =

    """\\u+([0-9a-fA-F]{4})""".r.replaceAllIn(str,
      m => Integer.parseInt(m.group(1), 16).toChar match {
        case '\\' => """\\"""
        case '$' => """\$"""
        case c => c.toString
      })

  def unescapeString( s: String): String = {

    maybe {
      StringContext.treatEscapes(StringUtil.unescapeUnicode(s))
    } getOrElse s

  }

  /**
    * XML Escape / Unescape
    *
    * https://www.freeformatter.com/xml-escape.html
    * ' is replaced with &apos;
    * " is replaced with &quot;
    * & is replaced with &amp;
    * < is replaced with &lt;
    * > is replaced with &gt;*
    */
  def XMLEscape(str: String): String = {
    str
      .replace("&", "&amp;")
      .replace("'", "&apos;")
      .replace("\"", "&quot;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
  }

  /**
    * JSON String Escape / Unescape
    *
    * https://www.freeformatter.com/json-escape.html
      *Backspace is replaced with \b
      *Form feed is replaced with \f
      *Newline is replaced with \n
      *Carriage return is replaced with \r
      *Tab is replaced with \t
      *Double quote is replaced with \"
      *Backslash is replaced with \\
    */
  def JSONEscape( str: String): String ={

    str
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")

  }
}
