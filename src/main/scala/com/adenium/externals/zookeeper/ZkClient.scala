package com.adenium.externals.zookeeper

import com.adenium.externals.kafka.KfPaths.topicPartitions
import com.adenium.externals.zookeeper.ZkUtil._
import com.adenium.utils.May._
import kafka.common.TopicAndPartition
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.spark.streaming.kafka.OffsetRange

object ZkClient {

  /**
    * zookeeper retry-policy
    * @param baseSleepTimeMs
    * @param maxRetries
    */
  case class Retry( baseSleepTimeMs: Int, maxRetries: Int) {
    def policy = new ExponentialBackoffRetry( baseSleepTimeMs, maxRetries)
  }

  object Retry {
    val default = Retry(1000, 3)
  }

  /**
	  * note:  zkclient contains only conntection-maker ( not connection itself )
    * @param connectionString
    * @param retry
    * @return
    */
  def apply( connectionString: String, retry : Retry = Retry.default): ZkClient = {
    val f = () => {
      val cur = CuratorFrameworkFactory.newClient( connectionString, retry.policy)
      cur.start()
      sys.addShutdownHook { cur.close() }
      cur
    }

    new ZkClient( f )
  }
}

class ZkClient( connector: () => CuratorFramework ) extends Serializable {

  lazy val curator: Option[ CuratorFramework ] = maybe { connector() }

  /**
    * Save the zookeeper offset to the zookeeper node.
    * This offset range is used for failover.
    * When Adenium Freamework is running in restore mode, it will process the data in its offset range.
    *
    * @param path
    * @param topic
    * @param ar
    * @return
    */
  def setOffsets(path: String, topic: String, ar: Array[ OffsetRange ])
  : Option[String] = {

    curator.flatMap { cur =>

      val offsetStr = ar
        .map ( range => s"${range.topic}:${range.partition}:${range.fromOffset}")
        .mkString(",")

      state ( s"saveOffsets: $offsetStr")

      setPersistent( cur, path + "/" + topic, offsetStr )
    }
  }


  /**
    * Get the value stored in the Zookeeper node.
    * @param path
    * @return
    */
  def getZkString( path: String)
  : Option[String] = curator.flatMap ( readString(_, path) )


  /**
    * Gets the offset stored with the "setOffsets method".
    * @param path
    * @param topic
    * @return
    */
  def getOffsets(path : String, topic: String)
  : Option[Map[TopicAndPartition, Long]] = {

    val ret = curator.flatMap { cur =>

      val offsetStr = readString( cur, path + "/" + topic)
      val offsets = maybeWarn {
        offsetStr.map { str =>
          str.split(",")
            .map( _.split(":"))
            .map( ar => TopicAndPartition( ar(0), ar(1).toInt) -> ar(2).toLong )
            .toMap
        }
      }.flatten

      offsets
    }

    ret
  }


  /**
    * Obtain the partition id managed by kafka.
    * @param topic
    * @return
    */
  def get_Kf_PartitionIds ( topic: String)
  : Option[Seq[ Int ] ] = {

    val path = topicPartitions( topic)

    for {
      cur <- curator
      children <- getChildren( cur, path )
    } yield children.flatMap { c => maybeWarn{ c.toInt } }

  }

  /**
    * Obtain the topic and partition managed by kafka.
    * @param topic
    * @return
    */
  def get_kf_TopicAndPartition( topic: String)
  : Option[TopicAndPartition ] = {

    get_Kf_PartitionIds( topic ) map ( pids => TopicAndPartition( topic, pids.length ))  // TODO : length ??

  }

}