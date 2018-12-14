package com.adenium.app.framework

import _root_.kafka.message.MessageAndMetadata
import _root_.kafka.serializer.StringDecoder
import com.adenium.app.config.Conf
import com.adenium.externals.spark.SparkUtil.Accumulate
import com.adenium.externals.zookeeper.ZkClient
import com.adenium.utils.Logger
import com.adenium.utils.May._
import kafka.common.TopicAndPartition
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.{KafkaUtils, OffsetRange}
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  *  The Adenium context includes "spark context", "spark direct stream", "engine configuration".
  * AdenuimContext creates spark context, spark direct stream, and zookeeper client.
  * spark direct stream can be created from a specific offset of kafka topic and provides a method for it.
  *
  */

case class AdeniumContext( sparkContext: SparkContext,
                           streamContext: StreamingContext,
                           zkClient: ZkClient,
                           conf: Conf ) {

  /**
    *
    * @param acs
    * @return
    */
  def createAccumulators( acs: Seq[String])
  : Accumulate = {
    Logger.logWarning( s"[ AdeniumContext ] createAccumulators : $acs")
    Accumulate( acs map sparkContext.longAccumulator)
  }

  /**
    * Save Kafka's current offset to the zookeeper.
    * @param offsets
    * @return
    */
  def saveOffsets( offsets: => Array[OffsetRange]): Option[String]
  = {

    conf.kf_needSave
      .flatMap { conf =>

        lazy val off = offsets

        Logger.logWarning( s"[ AdeniumContext ] saveOffsets to ${conf.zkp.offsets}\t" +
          s"""${off.map( _.toString() ).mkString("\n")}""" )

        this.zkClient.setOffsets( conf.zkp.offsets, conf.topic, off)
      }
  }

  /**
    *
    * @param path
    * @return
    */
  def getConfiguration( path: String): Option[String] = {
    zkClient.getZkString( path)
  }
}

object AdeniumContext {

  /**
    * AdeniumContext constructor
    *
    * @param conf : engine configuration [[com.adenium.app.config.Conf]]
    * @param registerClasses : Function to register class to be serialized
    * @return
    */
  def apply(conf: Conf, registerClasses: SparkConf => SparkConf)
  : AdeniumContext = {

    Logger.logInfo( "[ AdeniumContext ] spark (master, app, batchDuration) =" +
                   s" ${(conf.sp_master, conf.sp_app, conf.sp_batchDuration)}" )

    val sparkConf = new SparkConf()
      .setMaster(conf.sp_master)
      .setAppName(conf.sp_app)

    registerClasses( sparkConf)

    val context = new SparkContext( sparkConf)

    val streamcontext = new StreamingContext( context, Milliseconds( conf.sp_batchDuration.toLong))

    val zkCurator= ZkClient( conf.zkstr)

    AdeniumContext(context, streamcontext, zkCurator, conf)
  }


  /**
    * create an InputDStream using a streamingContext defined in AdeniumContext
    *
    * @param context : AdeniumContext [[com.adenium.app.framework.AdeniumContext]]
    * @param conf : configurations [[com.adenium.app.config.Conf]]
    * @return
    */
  def createDirectStream(context: AdeniumContext, conf: Conf )
  : InputDStream[(String, String)] = {

    val sc = context.streamContext
    val zk = context.zkClient

    /**
      * If the first parameter of readOffsetsIfNeeded is true, reads the last offset stored in the zookeeper.
      * This offset is used to create an InputDStream. If there is no offset,
      * InputDStream is created using the current offsets.
      */
    val offsets = AdeniumContext.readOffsetsIfNeeded(conf, zk)

    offsets.flatMap{ from =>

      maybeInfo {
        val handler = (msg: MessageAndMetadata[String, String]) => (msg.key(), msg.message())

        Logger.logInfo("[ createDStream ] ( broker, restore) : " + (conf.kfstr, from))
        KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, (String, String)](
          sc, conf.kf_param, from, handler)

      }("[ createDStream ] can't restore from zk's offsets.. so, i'll try from current")

    } getOrElse {

      Logger.logInfo("[ createDStream ] (broker, current) : " + (conf.kfstr, conf.topic))
      KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
        sc, conf.kf_param, conf.topic.split(",").toSet)
    }
  }

  /**
    * Read the last offset stored in the zookeeper.
    * @param conf : configuration
    * @param zkCurator : zookeeper client
    * @return
    */
  private def readOffsetsIfNeeded( conf: Conf, zkCurator: ZkClient)
  : Option[Map[TopicAndPartition, Long ] ] = {

    conf.kf_needRestore
      .flatMap { conf =>
        zkCurator.getOffsets( conf.zkp.offsets, conf.topic)
      }
  }
}

