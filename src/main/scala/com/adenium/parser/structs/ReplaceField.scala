package com.adenium.parser.structs

/** Field for changing the normalization result by condition
  *
  * If the normalization character at the specified mid matches a specific "vendor" and corresponds to "in string", change it to "out string".
  *
  * @constructor
  * @param mid
  * @param instr
  * @param outstr
  * @param vendor
  */
case class ReplaceField( mid: Int,
                         instr: String,
                         outstr: String,
                         vendor: Long)

object ReplaceField {

  def find( parsed: Array[(Int, String)], agn: Agent, replaces: Option[Map[Int, Array[ReplaceField]]])
  : Array[(Int, String)] = {

    def replaceString( mid: Int, value: String): Option[(Int, String)] = {
      for {
        rfs <- replaces
        arr <- rfs.get( mid )
        rep = arr.filter ( _.instr == value)      // todo: may need some performance improvement...
        ret <- rep.find( _.vendor == agn.vendorId)

      } yield {
        mid -> ret.outstr
      }
    }

    val ret = parsed.flatMap { case( mid, value) => replaceString ( mid, value.trim.toUpperCase) }
    ret

  }
}