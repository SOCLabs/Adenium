package com.adenium.common

import com.adenium.common.FieldType._


/** Field Key
	*
	* Field item descriptor
  *
  * FieldKey is a value that indicates which item a particular field corresponds to
	- A unique number (mid)
	- Name (name)
	- Unique ID : predefined items ( reserved ID : ex- SourceIP )
	- See the description of the Formatted field value format. [[com.adenium.common.FieldType]]
	*
  * @param mid Field ID
  * @param name Field Name (presentation String )
  * @param keyID Reserved Id
  * @param fieldType Field type
  */
case class FieldKey(mid: Int, name: String, keyID: Option[KeyID.Value], fieldType: FieldType) {

  /** Returns the key entry associated with the unique number (mid). */
	/**
		* pair (mid , FieldKey)
		*/
	def tuple: (Int, FieldKey) = mid -> this
}

/** Factory for [[com.adenium.common.FieldKey]] instances */
object FieldKey {
  /** Create FieldKey
    *
    * @constructor
    * @param kid predefined key id
    * @param mid mid to be associated with kid
    * @param name key name
    * @param format key type
    * @return fieldKey
    */
  def apply( kid: Option[KeyID.Value], mid: Int, name: String, format: FieldType = TypeString) : FieldKey = {

    FieldKey( mid, name, kid, format)
  }
}