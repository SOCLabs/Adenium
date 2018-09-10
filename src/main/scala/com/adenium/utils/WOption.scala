package com.adenium.utils

/**
  * Monoid
	* append : some binary operation S X S -> S, which holds associativity rule.
  * empty : identity element
  */
trait Monoid[T] {

  def append(a1: T, a2: T): T
  def empty: T

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

  /** Apply the given procedure f to the option's value
    *
    *  if it is nonempty. Otherwise, do nothing.
    *
    *  @param  f   the procedure to apply.
    *  @see map
    *  @see flatMap
    */
  def foreach (f: A => Unit ): Unit = option foreach f

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
  def map[B](f: A => B): WOption[L, B] = WOption( log, option map f )

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

  /** Returns this option if it is nonempty '''and''' applying the predicate p to
    * this option's value returns false. Otherwise, return none.
    *
    *  @param  p   the predicate used for testing.
    */
  def filterNot(p: A => Boolean): WOption[L, A] =
    if (isEmpty || !p(this.get)) this else WOption( log, None)

  /** Returns this option if it is nonempty '''and''' applying the predicate p to
    * this option's value returns true. Otherwise, return none.
    * ++++++++++++++++++++++++++++++
    *
    *  @param  p   the predicate used for testing.
    */
  def filter(p: A => Boolean): WOption[L, A] =
    if (isEmpty || p(this.get)) this else WOption( log, None)

  /** Returns the result of applying f to this option's
    *  value if the option is nonempty.  Otherwise, evaluates
    *  expression `ifEmpty`.
    *
    *  @note This is equivalent to `option map f getOrElse ifEmpty`.
    *
    *  @param  ifEmpty the expression to evaluate if empty.
    *  @param  f       the function to apply if nonempty.
    */
  def fold[B](ifEmpty: => B)(f: A => B): B = if (isEmpty) ifEmpty else f(this.get)

  def appendLog(a: L) (implicit m: Monoid[L]) : WOption[ L, A ] = WOption( m.append(log, a), option)
  def prependLog(a: L) (implicit m: Monoid[L]) : WOption[ L, A ] = WOption( m.append( a, log), option)

	// option's comments
  /** Returns true if the option is none, false otherwise.
    */
  def isEmpty: Boolean = option.isEmpty

  /** Returns false if the option is none, true otherwise.
    *  @note   Implemented here to avoid the implicit conversion to Iterable.
    */
  def nonEmpty: Boolean = option.nonEmpty

  /** Returns true if the option is an instance of some, false otherwise.
    */
  def isDefined: Boolean = option.isDefined

  /** Returns the option's value.
    *  @note The option must be nonEmpty.
    *  @throws 'java.util.NoSuchElementException' if the option is empty.
    */
  def get: A = option.get

  /** Returns this option if it is nonempty,
    *  otherwise return the result of evaluating `alternative`.
    *  @param alternative the alternative expression.
    */
  def orElse [B >: A]( alternative : => WOption[L, B])(implicit m: Monoid[L])
  : WOption[ L, B ] = if ( option.nonEmpty) this else alternative.prependLog( log)

  /** Returns the option's value if the option is nonempty, otherwise
    * return the result of evaluating `default`.
	  * +++++++++++++++++++++ log messages are dropped..
    *
    *  @param default  the default expression.
    */
  def getOrElse [B >: A] ( default : => B)(implicit m: Monoid[L])
  : B = if ( option.isEmpty) default else this.option.get

}

object WOption {
  import scala.language.{implicitConversions, reflectiveCalls}
  import scala.reflect.ClassTag

  /** An Option factory which creates Some(x) if the argument is not null,
    *  and None if it is null.
    *
    *  @param  x the value
    *  @return   Some(value) if value != null, None if value == null
    */
  def apply[L, A]( value : A) (implicit m: Monoid[L])
  : WOption[ L, A ] = if( value == null) WOption( m.empty, None) else WOption( m.empty, Option(value))

  def apply[L, A]( option : Option[A]) (implicit m: Monoid[L])
  : WOption[ L, A ] = WOption( m.empty, option)


  // Write a generic function map2 that combines two Option values using a binary
  // function. If either Option value is None, then the return value is too.
  def map2[L, A,B,C](a: WOption[L, A], b: WOption[L, B])(f: (A, B) => C)(implicit m: Monoid[L])
  : WOption[L, C] =
    a.flatMap( va => b.map( vb => f(va, vb)))

  // Write a function sequence that combines a list of Option s into one Option
  // containing a list of all the Some values in the original list. If the
  // original list contains None even once, the result of the function should be
  // None; otherwise the result should be Some with a list of all the values.
  def sequence[L, A: ClassTag](a: Array[WOption[L, A]])( implicit m : Monoid[L], wOptionLogOn: WOptionLogOn)
  : WOption[L, Array[A]] = {

    if( wOptionLogOn.on ) {
      a.foldRight( WOption( Array[A]())) { (x, y) =>
        map2( x, y)( _ +: _ )
//        WOption(
//          m.append(x.log, y.log),
//          y.option.map{ vy => x.option.map( vx => vx +: vy ).getOrElse( vy) } )
      }

    }
    else {
      WOption ( a.flatMap(_.option))
    }
  }

  // implicit case class to decide whether log or not.
  case class WOptionLogOn( on: Boolean ) {
    def log(str: => String): String = if (on) str else ""
  }


  implicit class WOptionMaker[L]( a: L) {

	  // implicit constructor
    // create WOption with given log string, and Option value
    // "" ~> Option("abc")  ==> WOption (List(""), Option("abc"))
    def ~>[B]( b: Option[B])( implicit log: WOptionLogOn): WOption[List[L], B]
    = if( log.on) WOption(List(a), b) else WOption(Nil, b)


    // implicit operator : append log to current WOption
    // "additional log" +> WOption
	  //
    def +>[B](b: WOption[List[L], B])(implicit m: Monoid[List[L]], log: WOptionLogOn): WOption[ List[L], B ]
    = if( log.on) b.appendLog(List(a)) else b
  }
}

