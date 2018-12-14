package com.adenium.common

import java.time.Instant

import com.adenium.common.FieldFormat.Formats
import com.adenium.common.FieldType._
import com.adenium.utils.May._
import com.adenium.utils.StringUtil

case class FieldFormatter( fString: Option[  String => Formats],
                           fNumeric: Option[ String => Formats],
                           fDateString: Option[ String => Formats],
                           fDateMillis: Option[ String => Formats],
                           fBoolean: Option[ String => Formats] = None,   // not used yet.
                           fDouble: Option[ String => Formats] = None     // not used yet.
                         )
{

  def converter(k: FieldKey): String => Formats = {

    val f = k.fieldType match {
      case TString => fString
      case TNumeric => fNumeric
      case TDateString => fDateString
      case TDateMillis => fDateMillis
      case TBoolean => fBoolean
      case TDouble => fDouble
      case _ => None
    }

    // note: i'll use FString as formatter, if formatter is not defined...
    f.getOrElse(FString)
  }

  def format( vf: VariableKeys)
            ( kv: (Int, String)): Option[(String, Formats)] = {

    /////////////////////////////
    val fid = kv._1
    val str = kv._2

    vf.getKey(fid).map { k =>

      /**
        * assert
        * 1. k must contain 'fieldType' and 'fieldName'
        * 2. this(FieldFormattter) must provide fieldFormatting Function (converter)
        * 3. converter may cause an exception,
        *
        */
      k.name -> {
        maybeWarn( converter(k)(str), "[ format ] Major : covnert throw an exception")
          .getOrElse( FString(str) )
      }
    }.orElse {
      /**
        * key not found in vf.. maybe conf. has some problems.
        * hmmm... should i
        */
      // todo
//      warn( Some( vf.defaultName( fid) -> FString(str)) )("[ format ] Major : fieldKey not found..")
      None
    }
  }
}

object FieldFormatter {

  //////////////////////////////////////////////////////////////////
  // formatter
  //////////////////////////////////////////////////////////////////
  def strDefault(str: String) = FString(str)
  def strJson(str: String) = FString( StringUtil.JSONEscape(str) )
  def strXml(str: String) = FString( StringUtil.XMLEscape(str))

  def numeric( str: String) = FNumeric( maybe(str.toLong).getOrElse( 0L) )  // todo: toInt or toLong ??
  def dateCEP ( str: String) = FDateString( DateFieldFormatter.cepDt( str))
  def millHD( str: String) = FDateMillis( DateFieldFormatter.mil2DateHd( str))

  object DateFieldFormatter {

    import java.time.format.DateTimeFormatter
    import java.time.{Instant, ZoneId}
    import java.util.Locale

    def dateTimeFormatter(str: String): DateTimeFormatter = {
      DateTimeFormatter.ofPattern( str)
        .withZone( ZoneId.systemDefault() )
        .withLocale( Locale.ENGLISH )
    }

    def millis2DateString(millis: Long, dateTimeFormatter: DateTimeFormatter): String = {
      maybeInfo (
        dateTimeFormatter.format( Instant.ofEpochMilli( millis))
      )("fail so i'll use current.. ").getOrElse(
        dateTimeFormatter.format( Instant.now())
      )
    }

    private val es_dateformat  = dateTimeFormatter( "yyyyMMdd'T'HHmmssZ" )
    private val dr_dateformat  = dateTimeFormatter( "yyyy-MM-dd HH:mm:ss z")
    private val dr_dateformat2 = dateTimeFormatter( "yyyy-MM-dd HH:mm:ss")
    private val hd_dateformat  = dateTimeFormatter( "yyyy-MM-dd HH:mm:ss z")

    def mil2DateHd( millis: String): String = millis2DateString( millis.toLong, hd_dateformat)

    // added to temp. cure
    def cepDt ( str: String) : String = {
      maybeInfo {
        val dt = str.take(19)
        dr_dateformat2.parse( dt )
        dt + " KST"
      }("fail so i'll use current.. ").getOrElse(
        dr_dateformat2.format( Instant.now()) + " KST"
      )
    }
  }

  object DatePath {

    import DateFieldFormatter.{dateTimeFormatter, millis2DateString}

    private val date2minBolck = dateTimeFormatter("yyyy/MM/dd/HH/mm")
    private val date2dateHour = dateTimeFormatter("yyyyMMddHH")
    private val date2datePath = dateTimeFormatter("yyyyMMdd")

    def minBlockPath( millis: Long): String =  millis2DateString(  millis, date2minBolck ).take(15) + "0"
    def minBlockPath( millis: String): String = DatePath.minBlockPath( millis.toLong)

    def dateHour( millis: Long): String =   millis2DateString( millis, date2dateHour )
    def dateHour( millis: String): String = DatePath.dateHour( millis.toLong )

    // todo : may cause integrity problem
    def dateString( instant: Option[Instant] = None): String = {
      val date = instant getOrElse Instant.now()
      date2datePath.format( date)
    }
  }
}
