package com.adenium.utils

/**
  * Argument handling class
  */
object Args {

  /**
    *
    * args: Array[String], keywords
    - make Map[String, Array[String ] ]
    *
    * keyword = "option1|option2|option3|..."
	  - args = Array( "-option1", "opt11", "opt12", "-option2", "opt21", "-option3", ....)
    *
    * Map
    - option1 --> Array("opt11", "opt12")
    - option2 --> Array("opt21")
    - option3 --> Array()
    *
    * @param args : arguments
    * @param keywords
    * @return
    */

  def args2map (args: Array[String], keywords : String )
  : Map[String, Array[String ] ] = {

    def toArr( args: Array[ String ]) = args.foldLeft( Array[ Array[ String ] ]() ) {

      case (b, arg) =>
        if ( arg.startsWith( "-" ) ) {
          Array( arg ) +: b
        } else {
          b.headOption foreach { _ => b.update( 0, b.head :+ arg ) }
          b
        }
    }

    val opts = keywords.split( "\\|" )

    toArr( args ).flatMap { arg =>
      opts
        .find( opt => arg.headOption.exists( ar => ar.dropWhile( _ == '-' ).equals( opt ) ) )
        .map( _ -> arg.drop( 1 ) )
    }.toMap

  }
}
