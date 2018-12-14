package com.adenium.common

import scala.collection.immutable.HashMap

/**
  * Defines the extension key.
  * @param keys
  */

case class VariableKeys( keys: HashMap[Int, FieldKey] ) {

  lazy val length: Int = keys.size
  lazy val size: Int = keys.size
  def getKey( fid: Int): Option[ FieldKey] = keys.get(fid) orElse Keys.ReservedKeys.get(fid)

  def defaultName( id: Int): String = "fid_" + id

  /**
    * @param ids
    * @return
    */
  def getName( id : Int): Option[String] = keys.get(id).map( _.name)

  /**
    * @param ids
    * @return
    */
  def getNameOrDefault( id : Int): String = getName(id).getOrElse( defaultName(id))

  /**
    * get all listed Names : name may be omitted if not exists.
    * @param ids
    * @return
    */
  def getNames( ids : Array[Int]): Array[String] = ids.flatMap( getName)

  /**
    * get all listed Names : name may be None if not exists.
    * @param ids
    * @return
    */
  def getAllNames( ids : Array[Int]): Array[Option[String]] = ids.map( getName)

}

object VariableKeys {

  val defaultVariableKeys = VariableKeys(None)

  def apply( fields: Option[Array[FieldKey]] = None ) : VariableKeys = {
    new VariableKeys( Keys.mergedKeys ( fields) )
  }
}
