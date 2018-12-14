package com.adenium.common

/** Generic types in JSON
	- '''Number:''' a signed decimal number that may contain a fractional part and may use exponential E notation,
		but cannot include non-numbers such as NaN. The format makes no distinction between integer and floating-point.
		JavaScript uses a double-precision floating-point format for all its numeric values, but other languages implementing JSON may encode numbers differently.
	- '''String:''' a sequence of zero or more Unicode characters. Strings are delimited with double-quotation marks and support a backslash escaping syntax.
	- '''Boolean:''' either of the values true or false

  * Basic
	1. '''Most types are converted to String'''
		- String, Date, DateTime... ==> "String", Right DateString format ?,
		- JSON itself does not specify how dates should be represented, but JavaScript does.
	1. '''Numerics are converted to Long or Double'''
		- Long, Integer, ==> Long, 12345, Double, Float  ==> Double,
		- Double or Float : not used in current adenium normalized result.
	1. '''Booleans are converted  to Boolean'''
		- Boolean ==> (Boolean or true),
		- false Boolean : not used in current adenium normalized result.
	1. '''Collections'''
		- Map, Tuple ==> { "key" : "value", "key2" : "value2", .... }
		- Array, ... ==> [ "key" : "value", "key2" : "value2", .... }
  *
  *
  * Data Type of Normalization field
  *
  * further thinking...
	-   typeStringEmail,
	-   typeStringAddress,
	-   typeStringIP,
	-   typeStringPort,
	-   typeStringURL
  */

object FieldType extends Enumeration {

  type FieldType = Value

  val TString: FieldType.Value = Value
  val TNumeric: FieldType.Value = Value
  val TDateString: FieldType.Value = Value
  val TDateMillis: FieldType.Value = Value
  val TBoolean: FieldType.Value = Value
  val TDouble: FieldType.Value = Value

}

