package com.adenium.common

import com.adenium.utils.May._

import scala.collection.immutable.HashMap

/**
  * Normalization result
  *
  * @param arrayWithID
  */
case class Normalized( arrayWithID : Array[ (Int, String)]) extends FieldGetter {

  private lazy val hashmap: HashMap[Int, String] = HashMap( arrayWithID: _* )
  override def get( mid: Int): Option[String] = hashmap.get( mid)

  /**
    * get all listed values according to listed seq. :  value may be None if not exists.
    * @param ids
    * @return
    */
  def getAll( ids: Array[Int]): Array[(Int, Option[String])] = ids.map(i => i -> get(i))

  /**
    * get all listed values according to listed seq.: value may be omitted if not exists.
    * @param ids
    * @return
    */
  def gets( ids: Array[Int]): Array[(Int, String)] = ids.flatMap(i => get(i).map( i -> _))

  /**
    * get all listed values according to listed seq. :  value is set to default if not exists.
    * @param ids
    * @return
    */
  def getAllwithDefault( ids: Array[Int], default: String)
  : Array[(Int, String)] = ids.map(i => i -> get(i).getOrElse(default))
}

object Normalized {

  def apply( TSV : String): Normalized = Normalized( TSV2Normalized( TSV))

  def filtered( ids: Array[Int])
              ( norm: Normalized): Normalized = new Normalized( norm.gets(ids))

  def ordered( ids: Array[Int], default: String= "")
             ( norm: Normalized): Normalized = new Normalized( norm.getAllwithDefault(ids, default))

  ///////////////////////////////////////////////////////
  // interim String format
  ///////////////////////////////////////////////////////
  val versionHeader : String = "adenium"
  val TAB: String = "\t"

  /**
    * The result of the normalization of TAB delimiters is made into an array of (key, value) pairs.
    * @param TSV
    * @return
    */

  def TSV2Normalized( TSV: String ): Array[ (Int, String)] = {

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

  def parsed2TSV( parsed: Parsed): String = {
    val TSV: String = warn {
      parsed.fields.map ( _.flatMap { _.valueWithId }.mkString( TAB) )
    }("[ Normalized ] : toTSV = Fields is empty.").getOrElse( "")

    versionHeader + TAB + TSV
  }
}
