package com.adenium.app.config

import com.adenium.externals.kafka.KfConsumer
import com.adenium.externals.zookeeper.ZkClient
import com.adenium.utils.May.{maybeWarn, maybe}
import org.apache.curator.framework.CuratorFramework

/**
  * Contains configuration information for the behavior of the Adenium framework.
  * @param zkstr Zookeeper node
  * @param kfstr Kafka brokers
  * @param topic Kafka Topics
  * @param opts  Options
  */
case class Conf( zkstr: String,
                 kfstr: String,
                 topic: String,
                 opts: Opts )
{


  // kafka output broker : for migration
  lazy val kfostr: String = opts.kfostr getOrElse kfstr

  //////////////////////////////////////
  lazy val zkp: ConfZkPaths = ConfZkPaths ( opts.app)

  //////////////////////////////////////
  lazy val sp_master: String = opts.get( "sp:master")
  lazy val sp_app: String = opts.get( "sp:app")
  lazy val sp_batchDuration: String = opts.get( "sp:duration")
  lazy val sp_bulksize: Int = opts.getOption("sp:bulk").flatMap(v => maybe( v.toInt)).getOrElse( 512 )

  //////////////////////////////////////
  lazy val ctrl_topic: Option[String] = opts.getOption( "kf:ctrl")
  lazy val ctrl_zknode: String = zkp.watch

  //////////////////////////////////////
  lazy val kf_param: Map[ String, String ] =  Map[ String, String ]( "metadata.broker.list" -> kfstr )
  lazy val kf_needRestore: Option[Conf] = opts.getOption( "kf:restore").map(_ => this)
  lazy val kf_needSave: Option[Conf] = Some(this) //opts.getOption( "kf:save").map(_ => this)
  lazy val kf_topicout: String = opts.get("kf:out_topic")
  lazy val kf_topicerr: Option[String] = opts.getOption("kf:err_topic")

}

object Conf {

  def apply( opts: Opts): Conf = {
    opts.writeLog("====================== Conf log ======================")
    Conf( opts.zkstr, opts.kfstr, opts.topic, opts)
  }

  def apply( args: Array[String]): Conf = Conf.apply( Opts(args))

  /**
    * Monitors adenium control messages entered in the specified kafka topic.
    * @param conf Adenium config
    * @return
    */
  def ctrlReader( conf: Conf)
  : Option[ KfConsumer] =
    conf.ctrl_topic.flatMap { topic =>
      maybeWarn { KfConsumer( topic, conf.sp_app, conf.zkstr, readFromStartOfStream = false ) }
    }

  /**
    * Monitors the adenium control messages entered in the watche node.
    * @param conf
    * @return
    */
  def ctrlWatcher( conf: Conf)
  : Option[(CuratorFramework, String)]
  = ZkClient(conf.zkstr).curator.map ( _ -> conf.ctrl_zknode )

  /// don't change
  ////////////////////////////////////////////////////////////////////
  val zk_nodebyte = 524255    // 512 KByte
  val zk_sepLine ="\n"
  val zk_sepItem ="\t"
  val zk_sepLiveDetectItem = "\t:\t"
//  val zk_sepLiveDetectItem = ","
}
