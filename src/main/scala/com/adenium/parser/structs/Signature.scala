package com.adenium.parser.structs

/** Categories of message signatures defined by sensor
  *
  * @constructor
  * @param signature
  * @param vendor
  * @param category1
  * @param category2
  * @param category3
  * @param category4
  */
case class Signature(vendor: Long = 0,
                     signature: String,
                     category1: String,
                     category2: String,
                     category3: String,
                     category4: String)

object Signature {

  def find( signature: String, vendor: Long = 0)(omap: Option[Map[(String, Long), Signature]] )
  : Option[ Signature ] = {

    val ret =
      omap.flatMap( map =>
          map.get(signature, vendor)
      )
    ret
  }

}