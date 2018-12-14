package com.adenium.common

import com.adenium.common.FieldType._
import com.adenium.common.KeyID._
import com.adenium.utils.May._

import scala.collection.immutable.HashMap

object Keys {

	/**
		* Merges reserved and custom fields to form a key set.
		*
		* @param fields
		* @return
		*/
	def mergedKeys(fields: Option[Array[FieldKey]]): HashMap[Int, FieldKey] = {

		def alreadyDefined(fk: FieldKey): Boolean = {

			// todo : if already defined 'name'?
			val already = ReservedKeys.find {
				case (fid, key) => fid == fk.fid || key.name == fk.name
			}

			already.foreach {k => state(s"!!! ALREADY DEFINED FIELDKEY !!! : ${fk -> k}")}
			already.isDefined
		}

		val filtered = fields.
			map(_.filterNot(alreadyDefined).map(_.tuple)).
			getOrElse(Array.empty)

		HashMap(ReservedKeys.toArray ++ filtered: _*)
	}

	///////////////////////////////////////////////////
	// ( mid, fname, type)
	val ReservedKeys: HashMap[Int, FieldKey]
	= HashMap(
		FieldKey(1, "category1", Some(CATEGORY1)).tuple,
		FieldKey(2, "category2", Some(CATEGORY2)).tuple,
		FieldKey(3, "category3", Some(CATEGORY3)).tuple,
		FieldKey(4, "signature", Some(SIGNATURE)).tuple,
		FieldKey(5, "severity", Some(SEVERITY)).tuple,
		FieldKey(6, "count", Some(COUNT), TNumeric).tuple,
		FieldKey(7, "RepeatCount", Some(REPEATCOUNT)).tuple,
		FieldKey(8, "srcIp", Some(SRCIP)).tuple,
		FieldKey(9, "srcPort", Some(SRCPORT), TNumeric).tuple,
		FieldKey(10, "srcMac", Some(SRCMAC)).tuple,
		FieldKey(11, "srcCountry", Some(SRCCOUNTRY)).tuple,
		FieldKey(12, "destIp", Some(DESTIP)).tuple,
		FieldKey(13, "destPort", Some(DESTPORT), TNumeric).tuple,
		FieldKey(14, "destMac", Some(DESTMAC)).tuple,
		FieldKey(15, "destCountry", Some(DESTCOUNTRY)).tuple,
		FieldKey(16, "srcDirection", Some(SRCDIRECTION)).tuple,
		FieldKey(17, "destDirection", Some(DESTDIRECTION)).tuple,
		FieldKey(18, "url", Some(URL)).tuple,
		FieldKey(19, "uri", Some(URI)).tuple,
		FieldKey(20, "uriParams", Some(URIPARAMS)).tuple,
		FieldKey(21, "header", Some(HEADER)).tuple,
		FieldKey(22, "protocol", Some(PROTOCOL)).tuple,
		FieldKey(23, "payload", Some(PAYLOAD)).tuple,
		FieldKey(24, "code", Some(CODE)).tuple,
		FieldKey(25, "rcvdBytes", Some(RCVDBYTES), TNumeric).tuple,
		FieldKey(26, "sentBytes", Some(SENTBYTES), TNumeric).tuple,
		FieldKey(27, "messageId", Some(MESSAGEID)).tuple,
		FieldKey(28, "srcZone", Some(SRCZONE)).tuple,
		FieldKey(29, "destZone", Some(DESTZONE)).tuple,
		FieldKey(30, "service", Some(SERVICE)).tuple,
		FieldKey(31, "duration", Some(DURATION), TNumeric).tuple,
		FieldKey(32, "aclNm", Some(ACLNM)).tuple,
		FieldKey(33, "action", Some(ACTION)).tuple,
		FieldKey(34, "rawData", Some(RAWDATA)).tuple,
		FieldKey(35, "sender", Some(SENDER)).tuple,
		FieldKey(36, "attachment", Some(ATTACHMENT)).tuple,
		FieldKey(37, "startAttackTime", Some(STARTATTACKTIME), TDateString).tuple,
		FieldKey(38, "endAttackTime", Some(ENDATTACKTIME), TDateString).tuple,
		FieldKey(39, "logTime", Some(LOGTIME), TDateMillis).tuple,
		FieldKey(40, "SyslogTime", Some(SYSLOGTIME)).tuple,
		FieldKey(41, "SyslogHost", Some(SYSLOGHOST)).tuple,
		FieldKey(42, "AgentId", Some(AGENTID), TNumeric).tuple,
		FieldKey(43, "agentIp", Some(AGENTIP)).tuple,
		FieldKey(44, "companyId", Some(COMPANYID), TNumeric).tuple,
		FieldKey(45, "companyNm", Some(COMPANYNM)).tuple,
		FieldKey(46, "companyGroupId", Some(COMPANYGROUPID), TNumeric).tuple,
		FieldKey(47, "deviceType", Some(DEVICETYPE)).tuple,
		FieldKey(48, "deviceModel", Some(DEVICEMODEL)).tuple,
		FieldKey(49, "vendor", Some(VENDOR)).tuple
	)

	// todo : maybe deleted.. later... ( maybe better to remove implicit )
	def getKey(mid: Int)(implicit variableKeys: Option[VariableKeys])
	: Option[FieldKey] = variableKeys.flatMap(_.getKey(mid))

	///////////////////////////////////////////////////
	// todo ... somewhat ugly..
	private def getFid(keyid: KeyID.Value): Int =
		ReservedKeys.
			find(_._2.keyID.exists(_ == keyid)).
			map(_._2.fid).
			getOrElse {
				throw new Exception("check ReservedKeys.")
				9999
			}

	val	m_CATEGORY1       : Int = getFid( CATEGORY1)
	val	m_CATEGORY2       : Int = getFid( CATEGORY1)
	val	m_CATEGORY3       : Int = getFid( CATEGORY3)
	val	m_SIGNATURE	      : Int = getFid( SIGNATURE)
	val m_SEVERITY	      : Int = getFid( SEVERITY)
	val m_COUNT	          : Int = getFid( COUNT)
	val m_REPEATCOUNT	    : Int = getFid( REPEATCOUNT)
	val m_SRCIP	          : Int = getFid( SRCIP)
	val m_SRCPORT	        : Int = getFid( SRCPORT)
	val m_SRCMAC	        : Int = getFid( SRCMAC)
	val m_SRCCOUNTRY	    : Int = getFid( SRCCOUNTRY)
	val m_DESTIP	        : Int = getFid( DESTIP)
	val m_DESTPORT	      : Int = getFid( DESTPORT)
	val m_DESTMAC	        : Int = getFid( DESTMAC)
	val m_DESTCOUNTRY	    : Int = getFid( DESTCOUNTRY)
	val m_SRCDIRECTION	  : Int = getFid( SRCDIRECTION)
	val m_DESTDIRECTION	  : Int = getFid( DESTDIRECTION)
	val m_URL	            : Int = getFid( URL)
	val m_URI	            : Int = getFid( URI)
	val m_URIPARAMS	      : Int = getFid( URIPARAMS)
	val m_HEADER	        : Int = getFid( HEADER)
	val m_PROTOCOL	      : Int = getFid( PROTOCOL)
	val m_PAYLOAD	        : Int = getFid( PAYLOAD)
	val m_CODE	          : Int = getFid( CODE)
	val m_RCVDBYTES	      : Int = getFid( RCVDBYTES)
	val m_SENTBYTES	      : Int = getFid( SENTBYTES)
	val m_MESSAGEID	      : Int = getFid( MESSAGEID )
	val m_SRCZONE	        : Int = getFid( SRCZONE)
	val m_DESTZONE	      : Int = getFid( DESTZONE)
	val m_SERVICE	        : Int = getFid( SERVICE)
	val m_DURATION	      : Int = getFid( DURATION)
	val m_ACLNM	          : Int = getFid( ACLNM)
	val m_ACTION	        : Int = getFid( ACTION)
	val m_RAWDATA	        : Int = getFid( RAWDATA)
	val m_SENDER	        : Int = getFid( SENDER)
	val m_ATTACHMENT	    : Int = getFid( ATTACHMENT)
	val m_STARTATTACKTIME : Int = getFid( STARTATTACKTIME)
	val m_ENDATTACKTIME	  : Int = getFid( ENDATTACKTIME)
	val m_LOGTIME	        : Int = getFid( LOGTIME)
	val m_SYSLOGTIME	    : Int = getFid( SYSLOGTIME)
	val m_SYSLOGHOST	    : Int = getFid( SYSLOGHOST)
	val m_AGENTID	        : Int = getFid( AGENTID)
	val m_AGENTIP	        : Int = getFid( AGENTIP)
	val m_COMPANYID	      : Int = getFid( COMPANYID)
	val m_COMPANYNM	      : Int = getFid( COMPANYNM)
	val m_COMPANYGROUPID	: Int = getFid( COMPANYGROUPID)
	val m_DEVICETYPE	    : Int = getFid( DEVICETYPE)
	val m_DEVICEMODEL	    : Int = getFid( DEVICEMODEL)
	val m_VENDOR	        : Int = getFid( VENDOR )
}
