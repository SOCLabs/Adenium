package com.adenium.app.framework

import com.adenium.utils.May.maybeWarn
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast

import scala.reflect.ClassTag

/**
  * Defines and handles "spark broadcast variables".
  */
object AdeniumBroadcast {

  case class Ref[A]( value: Option[A], bc: Option[Broadcast[A]]) {
    def status: (Boolean, Boolean) = ( value.isDefined, bc.isDefined )
    def isDefined: Boolean=  value.isDefined
  }

  object Ref {
    def empty[A]: Ref[A] = Ref[A]( None, None)
    def apply[A]( a: => A): Ref[A] = Ref( maybeWarn(a), None)
  }

  trait Caster {
    def apply[A:ClassTag]( neo: Ref[A], old: Ref[A]): Ref[A]
  }

  /**
    * Update "Spark broadcast Variables".
    * You can set blocking options that control the placement of workers during the update of broadcast variables.
    * true: The batch operation is aborted while the broadcast variable is being updated.
    * false: The batch job is asynchronous while the broadcast variable is being updated.
    *
    * @param blocking true/false
    * @param sparkContext
    * @return Caster
    */
  def broadcaster( blocking: Boolean, sparkContext: SparkContext)
  : Caster = new Caster {

    override def apply[A:ClassTag](n: Ref[A], o: Ref[A])
    : Ref[A] = update[A](blocking, sparkContext)(n, o)

  }

  private def update[A:ClassTag]( blocking: Boolean, sparkContext: SparkContext)
                        ( neo: Ref[A], old: Ref[A]) : Ref[A] = {

    neo.value.map { up =>

      old.bc.foreach(_.unpersist(blocking))
      val b = maybeWarn( sparkContext.broadcast(up))
      val r = Ref( neo.value, b)
      r

    } getOrElse old
  }


  private def getBroadcastedRef[A](broadcasted: Ref[A] )
  : Option[A] = maybeWarn( broadcasted.bc.map(_.value) ).flatten

  /**
    * Get the value from the broadcasted variable.
    *
    * @param current Spark Broadcast value
    * @param f Casting function
    * @tparam A
    * @tparam B
    * @return
    */
  def getBroadcast[A,B](current: Option[A])(f: A => Ref[B])
  :Option[B] = current.flatMap( itm => AdeniumBroadcast.getBroadcastedRef( f(itm)))

}
