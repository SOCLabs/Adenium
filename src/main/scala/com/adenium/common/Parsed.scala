package com.adenium.common

import com.adenium.common.Keys._
import com.adenium.utils.May._

/** Parsed field with side information. */
case class Parsed ( fields: Option[ Array[Field]],
                    host: Option[String],
                    raw: String,
                    log: String) extends FieldGetter {

  override def get(mid: Int): Option[String] =
    Parsed.get( _.key.mid == mid, this.fields)

  override def company: Option[String] = this.get( m_COMPANYID )
}

object Parsed {

  /** get first field's value that satisfying predicate. */
  def get( predicate: Field => Boolean, fields: Option[Array[Field]] ): Option[String] = {
    warn (
      fields
        .flatMap { flds => flds.find( predicate).flatMap(_.value) }
    )(s"Parsed: check predicate.. get(${(fields.isDefined, predicate)}")
  }

  /** get first field that satisfying predicate. */
  def find( predicate: Field => Boolean, fields: Option[Array[Field]] ): Option[Field] = {
    warn (
      fields
        .flatMap { flds => flds.find( predicate) }
    )(s"Parsed: check predicate.. find(${(fields.isDefined, predicate)}")
  }

  /** filter fields that satisfying predicate. */
  def filter( predicate: Field => Boolean, fields: Option[Array[Field]] ): Option[Array[Field]] = {
    warn (
      fields
        .map { flds => flds.filter( predicate) }.filter(_.nonEmpty)
    )(s"Parsed: check predicate.. filter(${(fields.isDefined, predicate)}")
  }
}


