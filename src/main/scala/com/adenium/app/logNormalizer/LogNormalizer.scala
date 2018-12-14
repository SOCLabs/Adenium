package com.adenium.app.logNormalizer

import com.adenium.app.config.Conf
import com.adenium.app.framework.AdeniumBroadcast.getBroadcast
import com.adenium.app.framework.{AdeniumContext, AdeniumControl}
import com.adenium.common.FieldFormatter.DatePath
import com.adenium.common.Keys._
import com.adenium.common.{Normalized, Parsed}
import com.adenium.externals.kafka.KfQueue
import com.adenium.parser.Parser
import com.adenium.parser.reference.{ParserRef, Zk2ParserRef}
import com.adenium.utils.Logger
import com.adenium.utils.May._
import com.adenium.utils.Timer.TimeLog
import com.adenium.utils.WOption.WOptionLogOn
import org.apache.spark.TaskContext
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.HasOffsetRanges


/**
  * LogNormalizer
  * Create the Adenium context and prepare the resources for running the engine.
  * The Adenium context includes "spark context", "spark direct stream", "engine configuration", and "resource".
  * Resources include information about the zookeeper connection and Kafka connection information.
  */
object LogNormalizer {

  def main(args: Array[String]) {

    /** Engine configuration */
    val conf = Conf(args)

    /** Create the Adenium context
      * The Adenium context includes "spark context", "spark direct stream", "engine configuration", and "resource".
      * @param conf : Engine configuration
      * @param register : Function to register class to be serialized
      */

    /** Create Input data stream*/
    val context = AdeniumContext( conf, LNBroadcast.registerClasses)

    /** Create Input data stream*/
    val dstream = AdeniumContext.createDirectStream( context, conf)

    /** Run main process*/
    setProcessor( context, dstream, conf)

    context.streamContext.start()
    context.streamContext.awaitTermination()
  }

  def setProcessor(context: AdeniumContext,
                   dstream: InputDStream[ (String, String) ],
                   conf: Conf ): StreamingContext = {

    /** Reference data for Normalization */
    implicit val parserRefMaker: Zk2ParserRef = Zk2ParserRef( context.zkClient.curator, conf)

    state( "Initialize ...")

    /**
      * Create an Adenium control. The control handles spark broadcast values.
      * The broadcast value processing method is defined in "LNBroadcast" class.
      */
    val ctrl = AdeniumControl[LNBroadcast]( LNBroadcastHelper(conf)).start( context.sparkContext, conf)
    val acc = context.createAccumulators( Seq[String] ( "[ # Raw  ]", "[ # Norm ]", "[ # Skip ] ") )

    dstream.foreachRDD { rdd =>

      state( "foreachRDD ...")

      /**
        * Save offsets read from Kafka.
        * You can use the stored offsets when you restart the system.
        */
      context.saveOffsets( rdd.asInstanceOf[ HasOffsetRanges ].offsetRanges)

      /** Get broadcast values from blocking message queue */
      val sb = TimeLog( ctrl.queue2Broadcast( context.sparkContext))("ctrl.updateBroadcast")
      val tp = conf.kf_topicout
      val tp2 = conf.kf_topicerr
      val logOn = context.getConfiguration( conf.zkp.parserlog).contains("true")

      implicit val wOptionLogOn: WOptionLogOn = WOptionLogOn( logOn)

      rdd.foreachPartition { lines =>

        state( "foreachPartition ...")
        val parser = warn( getBroadcast( sb)( _.parserRef))("parserRef is not ready")

        val pid = maybeInfo { TaskContext.get.partitionId() }("current partitionId not found").getOrElse(0)
        /* shuffle */
        val suffix = pid % 5    // todo : need to be controlled...
        val que = warn { getBroadcast(sb)( _.kfQueue)}("broadcasted kfqueue")
        val normalizer = LogNormalizerHelper.normalize( parser)_
        val forward2Kf = LogNormalizerHelper.sendKf( que, suffix )( tp)_
        val forward2Kf2 = LogNormalizerHelper.sendKf2( que)( tp2)_

        /* Normalization */
        TimeLog {
          lines.grouped( conf.sp_bulksize)
            .flatMap { bulk =>

              normalizer( bulk).map { case (fails, norms) => acc.add2sum( Seq( norms.length, fails.length) )
                ( fails, norms )
              }
            }
            .foreach {
              case( fails, norms) =>
                forward2Kf ( norms)
                forward2Kf2 ( fails)

            }

        }( s"mapPartitionsWithIndex : normalize - pid = $pid)")
      }

      acc.logStr foreach Logger.logInfo
    }
    context.streamContext
  }
}

/** Utility class for Normalization */
object LogNormalizerHelper {

  /**
    * Send the results of the normalization to kafka
    * @param que kafka queue
    * @param suffix shuffle values
    * @param topic Kafka topic name to save results
    * @param norms normalized result
    * @return
    */

  def sendKf ( que: Option[KfQueue], suffix: Int = 0 )
             ( topic: String )
             ( norms: Array[Parsed]): Int = {

    que.map { q =>
      norms.flatMap { evt =>

        val key = evt.get( m_AGENTID).orElse( evt.company).map( _ + suffix ).getOrElse( evt.logTime.getOrElse(""))   // added to shuffle by companyId
        q.send2kf( key, Normalized.parsed2TSV(evt), topic)
      }.length
    } getOrElse 0
  }

  /**
    * Send a normalization failure log to kafka
    * @param que kafka queue
    * @param topic kafka topic name to save results
    * @param fails raw log
    * @return
    */
  def sendKf2 ( que: Option[KfQueue] )
              ( topic: Option[String] )
              ( fails: Array[Parsed]): Int = {

    que.flatMap { q =>
        topic.map { tp =>
          fails.flatMap { evt =>
            q.send2kf( evt.logTime.getOrElse(""), evt.raw, tp)
          }.length
        }
    } getOrElse 0
  }

  // local helper function


  ///////////////////////////////////////////////////////
  // (fails, norms)
  def normalize( parserRef: Option[ ParserRef] )
               ( bulk: Seq[(String, String)])
               ( implicit logOn: WOptionLogOn)
  : Option[(Array[Parsed], Array[Parsed])] = {

    warn ( parserRef.flatMap{ ref =>
      maybeWarn {
        bulk
          .toParArray
          .map { v => Parser.execute( v._2, ref ) }.toArray
          .partition( _.fields.isEmpty )
      }
    } ) ("normalize - parserRef" )
  }
}
