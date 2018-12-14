package com.adenium.app.framework

import org.apache.spark.SparkContext

trait AdeniumBroadcastHelper[T] {

  /**
    * Initializes the broadcast variable.
    * @param sparkContext
    * @return
    */
  def initializeBroadcast( sparkContext: SparkContext): T

  /**
    * Update the broadcast variable.
    *
    * @param current
    * @param update
    * @param blocking
    * @param sparkContext
    * @return
    */
  def reference2Broadcast( current:T, update: T, blocking: Boolean)(sparkContext: SparkContext): T

  /**
    * Handles Adenium control commands.
    * Updates the broadcast variable according to the defined message.
    * @param message
    * @return
    */
  def command2Reference( message: String ) : Option[T]

}
