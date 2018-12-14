package com.adenium.parser.devices

import com.adenium.utils.WOption
import com.adenium.utils.WOption.{WOptionLogOn, WOptionMaker}

/** Syslog message handling
  *
  * '''RFC3164 : '''
  * The header conforms to RFC 3164 :
  * <PRI>TIMESTAMP HOSTNAME APP-NAME[PROCID]: sourcetype="SOURCETYPE" key1="val1" key2="val2" etc.
  * Header Element
  - PRI : The syslog priority defaults to 134. For information about how that number is determined, see Syslog Message Formats.
  - TIMESTAMP : Uses the format described in RFC 3164, which does not include the year. For example : Apr 11 17:55:56
  - HOSTNAME : The name of the Analytics server
  - APP-NAME : MobilityAnalytics
  - PROCID : The process ID (PID) of the Mobility Analytics process.
  - SOURCETYPE : The source type. For example, nm_app_exec or nm_session.
  - [[https://tools.ietf.org/html/rfc3164]]
  *
  * '''RFS5424 : '''
  * The header conforms to RFC 5424:
  * <PRI>VER TIMESTAMP HOSTNAME APP-NAME PROCID MSGID [SOURCETYPE@NM_IANA key1="val1" key2="val2" etc.]
  * Header Element
  - PRI : The syslog priority defaults to 134. For information about how that number is determined, see Syslog Message Formats.
  - VER : 1
  - TIMESTAMP : In UTC with standard format. For example: 2017-04-11T17:01:27.919Z
  - HOSTNAME : The name of the Analytics server
  - APP-NAME : MobilityAnalytics
  - PROCID : The process ID (PID) of the Mobility Analytics process.
  - MSGID : The type of event. For example: AppStart, SessionConnect, or UserLogin.
  - SOURCETYPE : The source type; for example, nm_app_exec or nm_session.
  - NM_IANA : 11912, which is the registered IANA (Internet Assigned Numbers Authority) number for NetMotion. For example,
    the client sessions source type would look like this in syslog output: nm_session@11912.
  - [[https://tools.ietf.org/html/rfc5424]]
  *
  */

object Syslog {

  private val RFC3164 = """^.{0,32}<(\d+)>\d? ?(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[ ]{1,2}(\d{1,2})[ ](\d{1,2}:\d{2}:\d{2}) (\S+) (.+)""".r.unanchored
  private val RFC5424 = """^.{0,32}<(\d+)>\d? ?(\d{4}[-]\d{2}[-]\d{2}[T]\S*)[ ](\S+) (.+)""".r.unanchored

  /** Additional supportable syslog encoding */
  case class SyslogEncodings( arr: Array[String])
  implicit val Encodings: SyslogEncodings = SyslogEncodings( Array( "CP949") )

  /** Header message struct trait */
  trait SyslogHeader {
    val priority: String
    val timestamp: String
    val hostname: String

  }

  /** Body message struct trait */
  trait SyslogMsg {
    val header: SyslogHeader
    val body: String
  }

  /**
    * RFC 3164 header
    *
    * 3164 Date format
    * "Mmm dd hh:mm:ss"
    * Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec
    * _1, _2, ... , 31
    * 00:00:00 .. 23:59:59
    * https://tools.ietf.org/html/rfc3164
    *
    * @param hostname hostname or IP address of original device.
    */
  case class Header3164 ( priority: String,
                          month: String,
                          day: String,
                          hour: String,
                          hostname: String) extends SyslogHeader     // hostname or IP address of original device.
  {
    val timestamp = s"${this.day} ${this.month} ${this.hour}"
  }

  /** RFC 3164 Body */
  case class RFC3164Msg ( header: Header3164, body: String ) extends SyslogMsg
  object RFC3164Msg {
    def apply ( priority: String, month: String, day: String, hour: String, hostname: String, body: String ): RFC3164Msg = {
      new RFC3164Msg( Header3164( priority, month, day, hour, hostname), body )
    }
  }

  /** RFC 5424 Header */
  case class Header5424 ( priority: String,
                          version: Option[String],
                          timestamp: String,
                          hostname: String,
                          appname: Option[String],
                          procid: Option[String],
                          msgid: Option[String]) extends SyslogHeader


  /** [[https://tools.ietf.org/html/rfc5424]] */
  case class RFC5424Msg ( header: Header5424, body: String ) extends SyslogMsg

  object RFC5424Msg {
    def apply ( priority: String, timestamp: String, hostname: String, body: String ): RFC5424Msg = {
      new RFC5424Msg(Header5424(priority, None, timestamp, hostname, None, None, None), body)
    }
  }

  ////////////////////////////////////////////

  def parse( msg: String)
           ( implicit wOptionLogOn: WOptionLogOn)
  : WOption[List[String], SyslogMsg ]
  = {

    parseSyslogMsg( msg).orElse {
      parseEncodeSyslogMsg( msg ).orElse {
        wOptionLogOn.log( s"[ Syslog ] fail : msg = ${msg.take( 50 )}" ) ~> None
      }
    }
  }

  /** Syslog Message parsing */
  private def parseSyslogMsg(msg: String)
                            ( implicit wOptionLogOn: WOptionLogOn)
  = {

    msg match {

      case RFC3164( pri, m, d, h, host, body ) =>

        val msg = RFC3164Msg( pri, m, d, h, host, body )
        wOptionLogOn.log( s"[ Syslog: RFC3164 ] ${msg.header}" ) ~> Option( msg )

      case RFC5424( pri, ts, host, body ) =>

        val msg = RFC5424Msg( pri, ts, host, body )
        wOptionLogOn.log( s"[ Syslog: RFC5424 ] ${msg.header}" ) ~> Option( msg )

      case _ =>
        wOptionLogOn.log( s"[ Syslog: parse ] fail ${msg.take(128)}" ) ~>
          None

    }
  }

  private def parseEncodeSyslogMsg( msg: String)
                                  ( implicit wOptionLogOn: WOptionLogOn)
  = {

    val Encodings = implicitly[SyslogEncodings].arr

    ( for {

      ec <- Encodings
      st = parseSyslogMsg( new String( msg.getBytes(ec),ec) )
      if st.isDefined

    } yield {

      wOptionLogOn.log( s"""[ parseEncodeSyslogMsg ] Success but, Check encoding charset= $ec""" ) +> st

    }).toStream.headOption getOrElse {
      wOptionLogOn.log(
        s"""[ parseEncodeRFC3164Msg ] tried but .. ${Encodings.mkString(":")}""" ) ~>
        None

    }
  }
}
