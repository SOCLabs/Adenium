package com.adenium.common

import com.adenium.common.FieldType.{ FieldType, TString}

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
  * @param fid Field ID
  * @param name Field Name (presentation String )
  * @param keyID Reserved Id
  * @param fieldType Field type
  */
case class FieldKey( fid: Int,
                     name: String,
                     keyID: Option[KeyID.Value]= None,
                     fieldType: FieldType = TString) {

  def tuple: (Int, FieldKey) = fid -> this

}
