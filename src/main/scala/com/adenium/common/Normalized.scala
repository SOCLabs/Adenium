package com.adenium.common

import com.adenium.common.Keys._
import com.adenium.utils.May._

import scala.collection.immutable.HashMap

/**
	* Normalization result
	*
  * @param arrayWithMid
  */
case class Normalized( arrayWithMid : Array[(Int, String)]) extends FieldGetter {

  lazy val arrayWithKey: Array[( FieldKey, String)] =  arrayWithMid.flatMap{ itm => getKey(itm._1).map( _ -> itm._2) }

  lazy val keyMap : Map[ FieldKey, String] = arrayWithKey.toMap

  lazy val midMap : Map[ Int, String] = arrayWithMid.toMap

  lazy val midMap2: HashMap[Int, String] = HashMap( arrayWithMid: _* )

  //  override def get( keyid: KeyID.Value): Option[String] = getKey(keyid).flatMap( keyMap.get )
  override def get( mid: Int): Option[String] = midMap2.get( mid)
  override def company: Option[String] = this.get( m_COMPANYID )

}

object Normalized {

	/** Normalization Format version Header*/
  val versionHeader : String = "adenium"
	/** Normalization Result Delimiter*/
  val TAB: String = "\t"

	/** Converts normalization result to TAB delimiter string*/
  def makeTSPString(parsed: Parsed): String = {
    val TSP: String = warn {
      parsed.fields.map ( _.flatMap { _.valueWithId() }.mkString( TAB) )
    }("[ Normalized ] : toTSV = Fields is empty.").getOrElse( "")

    versionHeader + TAB + TSP
  }


  /**
    * The result of the normalization of TAB delimiters is made into an array of (key, value) pairs.
    * @param TSV
    * @return
    */
  def arrayWithMid( TSV: String ): Array[(Int, String)] = {

    val arr = TSV.split( TAB)
    val ret =
      if ( arr.headOption.contains( versionHeader) ) {
        arr
          .drop(1)
          .grouped(2)
          .flatMap ( ar => maybeWarn {
            val idx = ar(0).toInt
            val value = ar(1)
            (idx, value)
          })
          .toArray
      }
      else
        arr.zipWithIndex.map { case (v, i) => ( i, v) }
    ret
  }

  def apply( TSV : String): Normalized = {
    Normalized( arrayWithMid( TSV))
  }
}
