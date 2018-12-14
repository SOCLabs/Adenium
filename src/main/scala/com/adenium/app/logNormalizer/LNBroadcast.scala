package com.adenium.app.logNormalizer

import com.adenium.app.config.Conf
import com.adenium.app.framework.AdeniumBroadcast.{Caster, Ref, broadcaster}
import com.adenium.app.framework.{AdeniumBroadcastHelper, AdeniumMessage}
import com.adenium.externals.kafka.KfQueue
import com.adenium.externals.zookeeper.ZkClient
import com.adenium.parser.reference.{ParserRef, ParserRefMaker}
import com.adenium.utils.Logger
import com.adenium.utils.May.memo
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Defines broadcast variables used for normalization.
  * The zkcurator, kafka queue, and normalization reference data is broadcast.
  * Broadcast variables are wrapped in the Ref class.[[com.adenium.app.framework.AdeniumBroadcast.Ref]]
  *
  * @param zkcurator   : zookeeper client
  * @param kfQueue     : kafka producer
  * @param parserRef   : Normalization reference data
  */

case class LNBroadcast( zkCurator: Ref[ZkClient] = Ref.empty,
                        kfQueue: Ref[KfQueue] = Ref.empty,
                        parserRef: Ref[ParserRef] = Ref.empty) {
  def status: String = s"LNBroadcast( zk, kf, pr) = ${(zkCurator.status, kfQueue.status, parserRef.status)}"
}

object LNBroadcast {

  /**
    * A dummy broadcast object that represents the initial creation state.
    * The broadcast target variable was not initialized on this object.
    */
  val empty = new LNBroadcast()


  /**
    * This method serializes the class.
    *
    * @param sparkConf : sparkConf
    * @return
    */
  def registerClasses(sparkConf: SparkConf): SparkConf = sparkConf
      .set( "spark.serializer", "org.apache.spark.serializer.KryoSerializer" )
      .registerKryoClasses( Array (
        classOf[ParserRef],
        classOf[ZkClient],
        classOf[KfQueue] )
      )


  /**
    * Creates a new (neo) LNBroadcast object.
    * @param zkCurator
    * @param kfQueue
    * @param parserRef
    * @return
    */
  def from( zkCurator: Option[Ref[ZkClient]] = None,
            kfQueue: Option[Ref[KfQueue]] = None,
            parserRef: Option[Ref[ParserRef]] = None): Option[LNBroadcast] = {

    if ( zkCurator.isDefined || kfQueue.isDefined || parserRef.isDefined )
      Some ( LNBroadcast(
        zkCurator.getOrElse( Ref.empty),
        kfQueue.getOrElse( Ref.empty),
        parserRef.getOrElse( Ref.empty)) )
    else
      None
  }

  /**
    * Update the "spark broadcast variable" using the caster function "f".
    *
    * @param old : Variable that is currently broadcast to the sparkContext.
    * @param neo : The variable to be newly broadcast to the sparkContext.
    * @param f : Methods for broadcasting LNBroadcasts to sparkContext.
    * @return : Returns an LNBroadcast object containing the newly broadcasted variable.
    */
  def broadcast( old: LNBroadcast, neo: LNBroadcast, f: Caster): LNBroadcast = {

    LNBroadcast(
      f( neo.zkCurator, old.zkCurator),
      f( neo.kfQueue, old.kfQueue),
      f( neo.parserRef, old.parserRef)
    )
  }
}

//////////////////////////////////////////////////////////////////////////////////////
case class LNBroadcastHelper( conf: Conf)( implicit parserRefMaker: ParserRefMaker)
  extends AdeniumBroadcastHelper[LNBroadcast] {

  /**
    * curator variables are lazy variables.
    * Because of The actual connection is created when you call "zkClient.curator".
    * This action is to manage the connection like a singleton.
    */
  lazy val curator: Option[CuratorFramework] = ZkClient( conf.zkstr).curator

  //////// private ref loader
  private def pa = Ref( ParserRef.initialize( parserRefMaker))
  private def zk ( str: String) = Ref( ZkClient( str))
  private def kf ( str: String) = Ref( KfQueue( str))

  override def initializeBroadcast(sc: SparkContext): LNBroadcast = {

    val neo = LNBroadcast( zk(conf.zkstr), kf(conf.kfostr), pa)

    Logger.logWarning( s" LNBroadcastHelper : initializeBroadcast : ${neo.status}")

    reference2Broadcast( LNBroadcast.empty, neo, blocking = true)( sc)
  }

  override def command2Reference( message: String): Option[LNBroadcast] = {

    val m = AdeniumMessage(message, LNBroadcastHelper.commands)

    LNBroadcast.from(
      m.getOpt( "update:zkstr").map( _.head).map(zk),
      m.getOpt( "update:kfstr").map( _.head).map(kf),
      m.getOpt( "update:parser").map(_ => pa)
    )
  }

  override def reference2Broadcast( old: LNBroadcast,
                                    neo: LNBroadcast, blocking: Boolean)(sparkContext: SparkContext)
  : LNBroadcast =
  {
    val ret = LNBroadcast.broadcast( old, neo, broadcaster( blocking, sparkContext))
    memo( s"LNBroadcastHelper : reference2Broadcast: ${ret.status}")
    ret
  }
}


object LNBroadcastHelper {

  val commands : String = "update:zkstr|update:kfstr|update:parser|save:parser"

}
