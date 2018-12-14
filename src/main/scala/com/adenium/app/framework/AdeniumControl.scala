package com.adenium.app.framework

import java.util.concurrent.ConcurrentLinkedQueue

import com.adenium.app.config.Conf
import com.adenium.externals.kafka.KfConsumer
import com.adenium.externals.zookeeper.ZkWatcher
import com.adenium.utils.Logger
import com.adenium.utils.May._
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.SparkContext

import scala.concurrent.Future

/**
  * It monitors the control commands of Adenium Framework and performs control actions according to messages.
  */

case class AdeniumControl[A](queue: ConcurrentLinkedQueue[A], helper: AdeniumBroadcastHelper[A] ) {

  private var currentValue : A = _

  private def push(msg: A): Boolean = queue.offer(msg)
  private def pop(): Option[A] = {
    val ret = Option( queue.poll())
    ret.foreach ( r => state( s" AdeniumControl == pop message: $r") )
    ret
  }

  /**
    * Execute AdeniumControl.
    * Call the initializeBroadcast method to initialize the broadcast variable and execute the Control Message loop.
    *
    * @param sparkContext
    * @param conf
    * @return
    */
  def start( sparkContext: SparkContext, conf: Conf): AdeniumControl[A] = {

    state("[ AdeniumControl ] == start ......... ")

    maybeWarn2 {

      initializeBroadcast( sparkContext)

      Conf.ctrlReader( conf)
        .foreach ( AdeniumControl.startKfMsgLoop( this) )

      Conf.ctrlWatcher( conf)
        .foreach { case (curator, path) => AdeniumControl.startZkMsgLoop(this)(curator, path) }

    }("[ AdeniumControl ] == is not successfully started....  ")

    this
  }

  /**
    * initialize the broadcast variable
    * @param sparkContext
    * @return
    */
  def initializeBroadcast(sparkContext: SparkContext): A = {

    state("[ AdeniumControl - == initializeBroadcast ] ......... ")

    currentValue = helper.initializeBroadcast( sparkContext)
    state(s"[ AdeniumControl -  == initializeBroadcast ] status : $currentValue ")

    currentValue
  }

  /**
    * Broadcasts the updated data to the Queue.
    *
    * @param sparkContext
    * @param blocking
    * @return
    */
  def queue2Broadcast(sparkContext: => SparkContext, blocking: Boolean = true)
  : Option[A] = {

    pop().map { ref =>

      state(s"[ AdeniumControl - message2Broadcast ] == message poped1 = $ref ......... ")

      val ret = helper.reference2Broadcast(currentValue, ref, blocking )(sparkContext)
      state(s"[ AdeniumControl - message2Broadcast ] == message poped2 = $ret ......... ")

      ret

    }.foreach ( currentValue = _ )

    state(s"[ AdeniumControl - message2Broadcast ] == message poped3 ......... $currentValue")
    Some( currentValue )

  }

}

object AdeniumControl {

  import scala.concurrent.ExecutionContext.Implicits.global

  /**
    * AdeniumControl implements a supervisor that monitors the Adenium control messages and broadcasts according to the message.
    * The type parameter A of the constructor is an object that defines a broadcast variable wrapped by Ref [[com.adenium.app.framework.AdeniumBroadcast.Ref]].
    * AdeniumControl consists of a Queue that stores this object and a Helper that processes
    * the Queue according to the methods defined in Helper and updates and broadcasts the Queue.
    *
    * @param helper
    * @tparam A
    * @return
    */
  def apply[A]( helper : AdeniumBroadcastHelper[A]): AdeniumControl[A]
  = {

    state( "[ AdeniumControl ] create queue for receiving admin commands..")
    new AdeniumControl[A]( new ConcurrentLinkedQueue[A](), helper )

  }

  private def command2queue[A]( msg: String, queue: AdeniumControl[A])
  : Boolean = {

    val ret = queue
      .helper
      .command2Reference( msg)
      .map( queue.push )

    ret getOrElse false
  }

  /**
    * Executes the Kafka message loop.
    * The message loop is a Watcher thread that monitors the AdeniumControl message.
    * Performs the defined action according to the entered message.
    *
    * @param reader
    * @param queue
    * @tparam A
    */
  def startKfMsgLoop[A]( queue: AdeniumControl[A])(reader: KfConsumer)
  : Unit = Future {

    state(s" [ AdeniumControl -  startKfMsgLoop] == startKfMsgLoop started : $reader")

    reader.readTuple { case ( _, value) =>

      val msg = new String(value)
      state ( s"[ AdeniumControl ] kafka message : $msg  ]" )

      val ret = command2queue( msg, queue)
      state ( s"[ AdeniumControl ] kafka message pushed: ${(msg, ret)}" )
    }

  } onComplete {
    _ => Logger.logWarning( "[ AdeniumControl ] startKfMsgLoop : Stopped ]" )
  }

  /**
    * Executes the Zookeeper message loop.
    * The message loop is a Watcher thread that monitors the AdeniumControl message.
    * Performs the defined action according to the entered message.
    *
    * @param queue
    * @param cur
    * @param path
    * @tparam A
    */
  def startZkMsgLoop[A](queue: AdeniumControl[A])(cur: CuratorFramework, path: String)
  : Unit = Future {

    state(s" [ AdeniumControl -  startZkMsgLoop] == startKfMsgLoop started : ${(cur, path)}")

    ZkWatcher.onZkChange(cur, path) { case ( msg, _) =>
        state ( "[ AdeniumControl ] == Zookeeper message : " + msg )

        val ret = command2queue( msg, queue)
        state ( "[ AdeniumControl ] == Zookeeper message pushed: " + (msg, ret))
    }
  } onComplete {
    _ => Logger.logWarning( "[ AdeniumControl ] startZkMsgLoop : Stopped ]" )
  }

}


