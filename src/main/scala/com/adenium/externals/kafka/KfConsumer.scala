package com.adenium.externals.kafka

import java.util.Properties

import com.adenium.utils.May._
import com.adenium.utils.Timer._
import kafka.consumer._
import kafka.serializer._

import scala.collection.JavaConversions._

/**
  * KafkaConsumer [[https://kafka.apache.org/quickstart]]
  *
  * @param stream
  * @param connector
  */
class KfConsumer ( stream: KafkaStream[ Array[Byte], Array[Byte]],
                   connector: ConsumerConnector ) {

  def read( handler: Array[Byte] => Unit): Unit = {

    for (messageAndTopic <- stream) {
      maybeWarn2 {

        handler( messageAndTopic.message() )
      }( "[ Kafka Message handler ]")
    }
  }

  def readTuple( handler: (Array[Byte], Array[Byte]) => Unit): Unit = {

    for ( messageAndTopic <- stream) {
      maybeWarn2 {

        handler( messageAndTopic.key(), messageAndTopic.message() )
      }( "[ Kafka Message handler ]")
    }
  }

  def close(): Unit = connector.shutdown()
}

/**
  * [[https://kafka.apache.org/0100/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html]]
  */
object KfConsumer {

  def apply( topic: String,
             groupId: String,
             zkstr: String,
             readFromStartOfStream: Boolean = true): KfConsumer = {

    val props = new Properties()
    props.put("group.id", groupId)
    props.put("zookeeper.connect", zkstr)
    props.put("auto.offset.reset", if (readFromStartOfStream) "smallest" else "largest")

    val config = new ConsumerConfig(props)
    val connector = Consumer.create(config)
    val filter= Whitelist(topic)

    state( s"[ KafkaConsumer ] kafka consumer create = $topic")
    val stream = TimeLog {

        connector.createMessageStreamsByFilter( filter, 1, new DefaultDecoder(), new DefaultDecoder()).get(0)

      }(s"[ KafkaConsumer ] (topic, zkstr) = ${(topic, zkstr)}" )

    new KfConsumer( stream, connector)
  }

}