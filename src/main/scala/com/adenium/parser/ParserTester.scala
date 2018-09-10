package com.adenium.parser

import com.adenium.common.Parsed
import com.adenium.parser.reference.File2ParserRef.ParserRefFiles
import com.adenium.parser.reference.{File2ParserRef, ParserRef}
import com.adenium.utils.Args
import com.adenium.utils.May.{maybe, maybeWarn2}
import com.adenium.utils.WOption.WOptionLogOn

object ParserTester extends App {

  import scala.io.StdIn.readLine

  /* arguments => opts*/
  val opts = Args.args2map( args, "path|logon")

  /* reference data path*/
  val refPath = opts.get("path") map (_.head) getOrElse {
    println("Please check reference data path : -path [resource path] " + "\n " +
            "Loaded from default path: resources\\")
    "resources"
  }

  val logOn = opts.get("logon").exists(_ => true)

  /* Init reference data from file*/
  val refFiles: ParserRefFiles = ParserRefFiles.apply( refPath + "\\")
  val parserRef: ParserRef = File2ParserRef(refFiles).initialize()

  val versionHeader : String = "adenium"
  val TAB: String = "\t"
  private def makeTSPString(parsed: Parsed): String = {
    val TSP: String =
      maybe {
        parsed.fields
          .map ( _.flatMap { _.valueWithId() }.mkString( TAB) )
          .getOrElse(": maybe error : toTSV = Fields is empty.")
      }.getOrElse(": exception occurred")

    versionHeader + TAB + TSP
  }


  private def tryParse( str: String ): Unit = {

    implicit val wOptionLogOn: WOptionLogOn = WOptionLogOn(logOn)

    val ret: Parsed = Parser.execute( str, parserRef, verbose = true)
    val flds = makeTSPString( ret)

    println( "\n" + flds)

  }

  private def run(): Unit = {

    System.setProperty("line.separator", "\n")
    val s = System.currentTimeMillis

    var repeat = true
    do {
      repeat = readLine match {
        case "" | null =>
          println( "i got blank line. end-of-stream. Bye~!")
          false
        case "q" | "quit" | "bye" | "exit"  => println("Bye !")
          false
        case str =>
          maybe( tryParse(str))
          true
      }
    } while (repeat)

    val e = System.currentTimeMillis
    println(s"List(=== running time : ${e - s}===============================================")
  }

  run()

}
















