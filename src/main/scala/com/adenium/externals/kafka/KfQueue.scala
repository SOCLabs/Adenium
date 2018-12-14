package com.adenium.externals.kafka

import java.util.concurrent.Future

import kafka.common.TopicAndPartition
import org.apache.kafka.clients.producer._

object KfQueue {

  def apply(broker: String): KfQueue = {
    val f = () => {
      val wrapper = KfProducer(broker)
      sys.addShutdownHook {
        wrapper.close()
      }
      wrapper
    }
    new KfQueue(f)
  }
}

/**
  * kafka util wrapped class
  * @param createWrapper
  */
class KfQueue( createWrapper: () => KfProducer) extends Serializable {

  lazy val producer: KfProducer = createWrapper()

  def close(): Unit = producer.close()

  def partition(pn: Option[Int], n: Int): Int = {

    pn match {
      case Some(p) => if (p < n) p else scala.util.Random.nextInt(n)
      case _ => scala.util.Random.nextInt(n)
    }

  }

  def send2kf( key: String, value: String, topic: String)
  : Option[ Future[RecordMetadata]] = {

    producer.send(new ProducerRecord[String, String]( topic, key, value))

  }
}




