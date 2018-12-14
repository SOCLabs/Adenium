package com.adenium.common

case class Parsed ( fields: Option[ Array[Field]],
                    host: Option[String],
                    raw: String,
                    log: String) extends FieldGetter {

  override def get( id: Int): Option[String] = {
    fields.flatMap( _.find( _.key.fid == id)).flatMap( _.value)
  }
}

