package com.adenium.common

import com.adenium.common.FieldType._
import com.adenium.common.KeyID._

import scala.collection.immutable.HashMap

object Keys {

  // ( mid, fname, type)
  val ReservedKeys: HashMap[Int, FieldKey]
  = HashMap (
    FieldKey( Some(CATEGORY1 ), 1 , "category1").tuple,
    FieldKey( Some(CATEGORY2 ), 2 , "category2").tuple,
    FieldKey( Some(CATEGORY3 ), 3 , "category3").tuple,
    FieldKey( Some(SIGNATURE ), 4 , "signature").tuple,
    FieldKey( Some(SEVERITY ), 5 , "severity").tuple,
    FieldKey( Some(COUNT ), 6 , "count", TypeNumeric).tuple,
    FieldKey( Some(REPEATCOUNT ), 7 , "RepeatCount").tuple,
    FieldKey( Some(SRCIP ), 8 , "srcIp").tuple,
    FieldKey( Some(SRCPORT ), 9 , "srcPort").tuple,
    FieldKey( Some(SRCMAC ), 10 , "srcMac").tuple,
    FieldKey( Some(SRCCOUNTRY ), 11 , "srcCountry").tuple,
    FieldKey( Some(DESTIP ), 12 , "destIp").tuple,
    FieldKey( Some(DESTPORT ), 13 , "destPort").tuple,
    FieldKey( Some(DESTMAC ), 14 , "destMac").tuple,
    FieldKey( Some(DESTCOUNTRY ), 15 , "destCountry").tuple,
    FieldKey( Some(SRCDIRECTION ), 16 , "srcDirection").tuple,
    FieldKey( Some(DESTDIRECTION ), 17 , "destDirection").tuple,
    FieldKey( Some(URL ), 18 , "url").tuple,
    FieldKey( Some(URI ), 19 , "uri").tuple,
    FieldKey( Some(URIPARAMS ), 20 , "uriParams").tuple,
    FieldKey( Some(HEADER ), 21 , "header").tuple,
    FieldKey( Some(PROTOCOL ), 22 , "protocol").tuple,
    FieldKey( Some(PAYLOAD ), 23 , "payload").tuple,
    FieldKey( Some(CODE ), 24 , "code").tuple,
    FieldKey( Some(RCVDBYTES ), 25 , "rcvdBytes", TypeNumeric).tuple,
    FieldKey( Some(SENTBYTES ), 26 , "sentBytes", TypeNumeric).tuple,
    FieldKey( Some(MESSAGEID ), 27 , "messageId").tuple,
    FieldKey( Some(SRCZONE ), 28 , "srcZone").tuple,
    FieldKey( Some(DESTZONE ), 29 , "destZone").tuple,
    FieldKey( Some(SERVICE ), 30 , "service").tuple,
    FieldKey( Some(DURATION ), 31 , "duration", TypeNumeric).tuple,
    FieldKey( Some(ACLNM ), 32 , "aclNm").tuple,
    FieldKey( Some(ACTION ), 33 , "action").tuple,
    FieldKey( Some(RAWDATA ), 34 , "rawData").tuple,
    FieldKey( Some(SENDER ), 35 , "sender").tuple,
    FieldKey( Some(ATTACHMENT ), 36 , "attachment").tuple,
    FieldKey( Some(STARTATTACKTIME ), 37 , "startAttackTime", TypeDateString).tuple,
    FieldKey( Some(ENDATTACKTIME ), 38 , "endAttackTime", TypeDateString).tuple,
    FieldKey( Some(LOGTIME ), 39, "logTime", TypeDateMillis).tuple,
    FieldKey( Some(SYSLOGTIME ), 40 , "SyslogTime").tuple,
    FieldKey( Some(SYSLOGHOST ), 41 , "SyslogHost").tuple,
    FieldKey( Some(AGENTID ), 42 , "AgentId",TypeNumeric).tuple,
    FieldKey( Some(AGENTIP ), 43 , "agentIp").tuple,
    FieldKey( Some(COMPANYID ), 44 , "companyId", TypeNumeric).tuple,
    FieldKey( Some(COMPANYNM ), 45 , "companyNm").tuple,
    FieldKey( Some(COMPANYGROUPID ), 46 , "companyGroupId", TypeNumeric).tuple,
    FieldKey( Some(DEVICETYPE ), 47 , "deviceType").tuple,
    FieldKey( Some(DEVICEMODEL ), 48 , "deviceModel").tuple,
    FieldKey( Some(VENDOR ), 49 , "vendor").tuple
    //////////////////////

  )

  ///////////////////////////////////////////////////

  /**
    * reserved KeyID --> Key
    * @param keyid
    * @return
    */
  def getKey( keyid: KeyID.Value): Option[ FieldKey] = ReservedKeys.find( _._2.keyID.exists( _ == keyid) ).map(_._2)

  /**
    * MID --> KEY
    *
    * TODO: CHECK
    * MID MAY NOT EXISTS IN RESERVED KEY....
	  *
    * @param MID
    * @return
    */
  def getKey( mid: Int): Option[ FieldKey] = ReservedKeys.get( mid)

  /**
    *
	  * note: if reserved key doesn't exist... create user-define-field-key
    */
  def getKey( mid: Int, name: String):  FieldKey = ReservedKeys.getOrElse(mid, FieldKey(None, mid, name))

  val	m_CATEGORY1       : Int = getKey( CATEGORY1).get.mid
  val	m_CATEGORY2       : Int = getKey( CATEGORY1).get.mid
  val	m_CATEGORY3       : Int = getKey( CATEGORY3).get.mid
  val	m_SIGNATURE	      : Int = getKey( SIGNATURE).get.mid
  val m_SEVERITY	      : Int = getKey( SEVERITY).get.mid
  val m_COUNT	          : Int = getKey( COUNT).get.mid
  val m_REPEATCOUNT	    : Int = getKey( REPEATCOUNT).get.mid
  val m_SRCIP	          : Int = getKey( SRCIP).get.mid
  val m_SRCPORT	        : Int = getKey( SRCPORT).get.mid
  val m_SRCMAC	        : Int = getKey( SRCMAC).get.mid
  val m_SRCCOUNTRY	    : Int = getKey( SRCCOUNTRY).get.mid
  val m_DESTIP	        : Int = getKey( DESTIP).get.mid
  val m_DESTPORT	      : Int = getKey( DESTPORT).get.mid
  val m_DESTMAC	        : Int = getKey( DESTMAC).get.mid
  val m_DESTCOUNTRY	    : Int = getKey( DESTCOUNTRY).get.mid
  val m_SRCDIRECTION	  : Int = getKey( SRCDIRECTION).get.mid
  val m_DESTDIRECTION	  : Int = getKey( DESTDIRECTION).get.mid
  val m_URL	            : Int = getKey( URL).get.mid
  val m_URI	            : Int = getKey( URI).get.mid
  val m_URIPARAMS	      : Int = getKey( URIPARAMS).get.mid
  val m_HEADER	        : Int = getKey( HEADER).get.mid
  val m_PROTOCOL	      : Int = getKey( PROTOCOL).get.mid
  val m_PAYLOAD	        : Int = getKey( PAYLOAD).get.mid
  val m_CODE	          : Int = getKey( CODE).get.mid
  val m_RCVDBYTES	      : Int = getKey( RCVDBYTES).get.mid
  val m_SENTBYTES	      : Int = getKey( SENTBYTES).get.mid
  val m_MESSAGEID	      : Int = getKey( MESSAGEID ).get.mid
  val m_SRCZONE	        : Int = getKey( SRCZONE).get.mid
  val m_DESTZONE	      : Int = getKey( DESTZONE).get.mid
  val m_SERVICE	        : Int = getKey( SERVICE).get.mid
  val m_DURATION	      : Int = getKey( DURATION).get.mid
  val m_ACLNM	          : Int = getKey( ACLNM).get.mid
  val m_ACTION	        : Int = getKey( ACTION).get.mid
  val m_RAWDATA	        : Int = getKey( RAWDATA).get.mid
  val m_SENDER	        : Int = getKey( SENDER).get.mid
  val m_ATTACHMENT	    : Int = getKey( ATTACHMENT).get.mid
  val m_STARTATTACKTIME : Int = getKey( STARTATTACKTIME).get.mid
  val m_ENDATTACKTIME	  : Int = getKey( ENDATTACKTIME).get.mid
  val m_LOGTIME	        : Int = getKey( LOGTIME).get.mid
  val m_SYSLOGTIME	    : Int = getKey( SYSLOGTIME).get.mid
  val m_SYSLOGHOST	    : Int = getKey( SYSLOGHOST).get.mid
  val m_AGENTID	        : Int = getKey( AGENTID).get.mid
  val m_AGENTIP	        : Int = getKey( AGENTIP).get.mid
  val m_COMPANYID	      : Int = getKey( COMPANYID).get.mid
  val m_COMPANYNM	      : Int = getKey( COMPANYNM).get.mid
  val m_COMPANYGROUPID	: Int = getKey( COMPANYGROUPID).get.mid
  val m_DEVICETYPE	    : Int = getKey( DEVICETYPE).get.mid
  val m_DEVICEMODEL	    : Int = getKey( DEVICEMODEL).get.mid
  val m_VENDOR	        : Int = getKey( VENDOR ).get.mid
}





