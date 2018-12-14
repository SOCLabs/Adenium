package com.adenium.parser.structs

case class ReplaceField( mid: Int,
                         instr: String,
                         outstr: String,
                         vendor: Long,
                         ignoreVendor: Boolean = true)

object ReplaceField {

  def find( parsed: Array[(Int, String)], agn: Agent, replaces: Option[Map[Int, Array[ReplaceField]]])
  : Array[(Int, String)] = {

    def replaceString( mid: Int, value: String): Option[(Int, String)] = {
      for {
        rfs <- replaces
        arr <- rfs.get( mid )
        rep = arr.filter ( _.instr == value)      // todo: may need some performance improvement...
        ret <- rep.find( _.vendor == agn.vendorId) orElse rep.find ( _.ignoreVendor )

      } yield {
        mid -> ret.outstr
      }
    }

    val ret = parsed.flatMap { case( mid, value) => replaceString ( mid, value.trim.toUpperCase) }
    ret

  }
}