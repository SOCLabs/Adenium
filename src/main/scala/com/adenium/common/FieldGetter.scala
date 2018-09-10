package com.adenium.common

/**
	* trait for Event that has getter interface
	*
*/
trait FieldGetter {

	/**
		* get specified field from current event
		*/
  def get( mid: Int): Option[String]

	/**
		* current event's company
		*/
	def company: Option[String]

}

