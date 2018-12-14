package com.adenium.utils

/**
  * Monoid
  * append : some binary operation S X S -> S, which holds associativity rule.
  * empty : identity element
  */
trait Monoid[A] {

  def append(a1: A, a2: A): A
  def empty: A

}

object Monoid {

  /**
    * refer to implicit conversion in scala
    * @tparam A
    * @return
    */
  implicit def ListMonoid[ A ]: Monoid[ List[ A ] ] = new Monoid[ List[ A ] ] {
    def append(a1: List[ A ], a2: List[ A ]): List[ A ] = a1 ::: a2
    def empty: List[ A ] = Nil
  }
}

/** writer Option monad */
case class WOption[L, +A](log: L, option: Option[A]) {

  /** Returns a some containing the result of applying f to this option's
    * value if this option is nonempty.
    * Otherwise return none.
    *
    *  @note This is similar to `flatMap` except here,
    *  f does not need to wrap its result in an option.
    *
    *  @param  f   the function to apply
    *  @see flatMap
    *  @see foreach
    */
  def map[B](f: A => B)
  : WOption[L, B] = WOption( log, option map f )

  /** Returns the result of applying f to this option's value if
    * this option is nonempty.
    * Returns none if this option is empty.
    * Slightly different from `map` in that f is expected to
    * return an option (which could be none).
    *
    * ++++++++++++++++++++++++++++++++++++++++++++++++++++
    *  @param  f   the function to apply
    *  @see map
    *  @see foreach
    */
  def flatMap[B](f: A => WOption[L, B])(implicit m: Monoid[L])
  : WOption[ L, B ] = {

    option.map{ f(_).prependLog( log) }.getOrElse( WOption(log, None: Option[B]))
  }

  def appendLog(a: L) (implicit m: Monoid[L]) : WOption[ L, A ] = WOption( m.append(log, a), option)
  def prependLog(a: L) (implicit m: Monoid[L]) : WOption[ L, A ] = WOption( m.append( a, log), option)

  def isEmpty: Boolean = option.isEmpty
  def nonEmpty: Boolean = option.nonEmpty
  def isDefined: Boolean = option.isDefined
  def get: A = option.get

  def orElse [B >: A]( alternative : => WOption[L, B])(implicit m: Monoid[L])
  : WOption[ L, B ] = if ( option.nonEmpty) this else alternative.prependLog( log)

  // log messages are dropped..
  def getOrElse [B >: A] ( default : => B)(implicit m: Monoid[L])
  : B = if ( option.isEmpty) default else this.option.get

}

object WOption {
  import scala.language.{implicitConversions, reflectiveCalls}
  import scala.reflect.ClassTag

  def unit[L, A]( option : Option[A]) (implicit m: Monoid[L])
  : WOption[ L, A ] = WOption( m.empty, option)

  def noLog[L, A](a: Option[A])(implicit m: Monoid[L])
  : WOption[L, A ] = WOption.unit[L, A](a)

  /**
    * Write a function sequence that combines a list of Option s into one Option
    * containing a list of all the Some values in the original list. If the
    * original list contains None even once, the result of the function should be
    * None; otherwise the result should be Some with a list of all the values.
    *
    * @param a
    * @param m
    * @param wOptionLogOn
    * @tparam L
    * @tparam A
    * @return
    */

  def sequence[L, A: ClassTag](a: Array[WOption[L, A]])( implicit m : Monoid[L], wOptionLogOn: WOptionLogOn)
  : WOption[L, Array[A]] = {

    if( wOptionLogOn.on ) {
      a.foldRight( WOption.unit( Some(Array[A]()) )){ (x, y) =>
        WOption(
          m.append(x.log, y.log),
          y.option.flatMap( vy => x.option.map( vx => vx +: vy )).orElse( y.option) )
      }
    }
    else {
      WOption ( m.empty, Some(a.flatMap( _.option)) )
    }
  }

  /** implicit case class to decide whether log or not. */
  case class WOptionLogOn(on: Boolean ) {
    def log(str: => String): String = if (on) str else ""
  }

  implicit class WOptionMaker[L]( a: L) {
    /** implicit constructor
      *
      * create WOption with given log string, and Option value
      * "" ~> Option("abc")  ==> WOption (List(""), Option("abc"))
      */
    def ~>[B]( b: Option[B])( implicit log: WOptionLogOn): WOption[List[L], B]
    = if( log.on) WOption(List(a), b) else WOption(Nil, b)

    /** implicit operator : append log to current WOption
      * "additional log" +> WOption
      */
    def +>[B](b: WOption[List[L], B])(implicit m: Monoid[List[L]], log: WOptionLogOn): WOption[ List[L], B ]
    = if( log.on) b.appendLog(List(a)) else b
  }

  //    implicit def ListWOption[L](a: L)(log: Boolean = implicitly[WOptionLogOn].on) = new {
  //
  //      def ~>[B]( b: Option[B])
  //      = WOption(List(a), b)
  //
  //      def +>[B](b: WOption[List[L], B])(implicit m: Monoid[List[L]]): WOption[ List[L], B ]
  //      = b.appendLog(List(a))
  //    }


}

