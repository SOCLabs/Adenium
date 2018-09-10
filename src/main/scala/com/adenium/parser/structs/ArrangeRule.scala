package com.adenium.parser.structs

/** The rule that links the tokenization result to the normalization field.
  *
  * Associate the regular expression capture sequence with the normalization field ID
  *
  * @constructor
  * @param tokenizeRuleId Regex ID
  * @param captureOrder Regex Match group Sequence
  * @param fieldId Predefined field id
  */
case class ArrangeRule( tokenizeRuleId: Int,
                        captureOrder: Int,
                        fieldId:Int
                        )

object ArrangeRule {

  def findInvalidRules(rules: Option[ Map[Int, Array[ArrangeRule]]] )
  : Option[ Map[Int, Array[ArrangeRule]]] = {

    val invalids =
      rules.map { tbl =>
        tbl.filterNot { case (_, mr) =>
          mr.map(_.captureOrder).sorted.deep == (1 to mr.length).toArray.deep && // captureOrder is valid?
            mr.map(_.fieldId).toSet.size == mr.length // unique?
        }
      }
    invalids
  }
}