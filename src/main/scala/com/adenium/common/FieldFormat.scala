package com.adenium.common

/**
  * Fields Formats cover all JSON format.
  * maybe later, FString will be split to sub-classes, ie. FIPString, FDNSString ...
  * Please refer to the FieldType description.
  * [[com.adenium.common.FieldType]]
  */
sealed trait FieldFormat[T] {
  def get : T
  def string : String = get match {
    case str : String => str
    case etc          => etc.toString
  }
}

object FNull extends FieldFormat[Null] { override def get: Null = null }

/** Adenium field type [[com.adenium.common.FieldType]]  */
final case class FString( get: String) extends FieldFormat[String]
/** Adenium field type [[com.adenium.common.FieldType]]  */
final case class FDateString( get: String) extends FieldFormat[String]
/** Adenium field type [[com.adenium.common.FieldType]]  */
final case class FDateMillis( get: String) extends FieldFormat[String]
/** Adenium field type [[com.adenium.common.FieldType]]  */
final case class FNumeric( get: Long) extends FieldFormat[Long]
/** Adenium field type [[com.adenium.common.FieldType]]  */
final case class FBoolean( get: Boolean) extends FieldFormat[Boolean]
/** Adenium field type [[com.adenium.common.FieldType]]  */
final case class FDouble( get: Double) extends FieldFormat[Double]


object FieldFormat {

  type Formats = FieldFormat[_ >: String with Long with Boolean with Double with Null]
}
