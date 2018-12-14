package com.adenium.externals.spark

import com.adenium.utils.Logger
import com.adenium.utils.May.maybe
import com.adenium.utils.May.maybeInfo
import org.apache.spark.util.LongAccumulator

/**
  * Spark util wrapped class
  */
object SparkUtil {

  case class Accumulate ( acs : Seq[ LongAccumulator ] ) {

    def logStr: Seq[String ] = {
      acs map { ac => s"${ac.name.getOrElse( ac.id.toString )} = ${ac.value}" }
    }

    def log (): Unit = {
      acs foreach { ac => Logger.logInfo( ac.name.getOrElse( ac.id.toString) + " = " + ac.value )}
    }

    def add ( is: Seq[ Int] ): Unit = {
      acs.zip( is).foreach{ case (a, i) => maybe { a add i } } // swallow exception
    }

    def add( nls: Iterator[ Seq[ Int]]): Unit = {
      //ex: Iterator [Seq( ls, ncnt, bcnt, fcnt )]
      nls foreach { is => this.add( is) }
    }

    def add2sum(is: Seq[ Int], f: Seq[Int] => Int = _.sum ): Unit = {
      acs.zip( f(is) +: is).foreach { case (a, i) =>
        maybeInfo( a add i )("add2sum failed")
      }
    }
  }

}
