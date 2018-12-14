package com.adenium.externals.kafka

import java.util.Properties
import java.util.concurrent.Future

import com.adenium.utils.May._
import org.apache.kafka.clients.producer._

/**
  * A client that consumes records from a Kafka cluster.
  * This client transparently handles the failure of Kafka brokers, and transparently adapts as topic partitions it fetches migrate within the cluster. This client also interacts with the broker to allow groups of consumers to load balance consumption using consumer groups.
  *
  * The consumer maintains TCP connections to the necessary brokers to fetch data. Failure to close the consumer after use will leak these connections. The consumer is not thread-safe. See Multi-threaded Processing for more details.
  *
  *
  * @param brokers
  * @param producer
  */
class KfProducer( brokers: String, producer: KafkaProducer[String, String]) extends Serializable {

  def send( record: ProducerRecord[String, String], callBack: Callback = null)
  : Option[Future[RecordMetadata]] = {

    maybeWarn { producer.send( record, callBack) }
  }

  def close(): Unit = Option( producer).foreach ( _.close())
}

/**
  * A Kafka client that publishes records to the Kafka cluster.
  * The producer is thread safe and sharing a single producer instance across threads will generally be faster than having multiple instances.
  *
  * Here is a simple example of using the producer to send records with strings containing sequential numbers as the key/value pairs.
  * [[https://kafka.apache.org/10/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html]]
  */
object KfProducer {

  def apply( broker: String, props: Option[Properties] = None): KfProducer = {

    val prop = props.getOrElse {
      val  p = new Properties()
      p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
      p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
      p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
      p.put(ProducerConfig.ACKS_CONFIG, "0")
      p.put(ProducerConfig.RETRIES_CONFIG, "3")
      p.put(ProducerConfig.LINGER_MS_CONFIG, "0")
      //props.put(ProducerConfig.BATCH_SIZE_CONFIG, "1")
      p
    }

    val prod = new KafkaProducer[String, String](prop)
    new KfProducer( broker, prod )
  }

}