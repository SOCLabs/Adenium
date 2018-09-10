package com.adenium.common


/** Normalization field
	*
	* A normalized event consists of a Field class representing the value of each item.
	  Normalization events are displayed as FieldGetter, Normalized, Parsed, etc.
		depending on the display format, the presence of auxiliary information, and the difference in functionality provided.
	*
	- FieldGetter : [[com.adenium.common.FieldGetter]]<br>
	- Normalized : [[com.adenium.common.Normalized]]<br>
	- Parsed : [[com.adenium.common.Parsed]]
	*
	* The field consists of a pair of field key and actual item value.
	*
  * @constructor
  * @param key Predefined Field Id [[com.adenium.common.FieldKey]]
  * @param value Field value */
case class Field ( key: FieldKey,
                   value: Option[String] ) {

  /**
	  * The value represented by a string that is a pair of the unique number (mid) and value of the field.
	  */
  def valueWithId( sep: String = Field.TAB) : Option[String] = value.map ( key.mid.toString + sep + _ )

  /**
	  * check if current field's key is equal to given field's.
	  *
	  */
  def isEqualKey( fld: Field ): Boolean = key.mid == fld.key.mid
}

object Field {

  /**
	  * default field key-value separate string
	  */
  val TAB = "\t"

  /**
    * @constructor
    */
  def apply( key: FieldKey, value: String )
  : Field = Field( key, Some(value))
}

