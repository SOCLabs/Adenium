package com.adenium.externals.kafka

object KfPaths {

  val consumers = "/consumers"
  val brokerIds = "/brokers/ids"
  val brokerTopics = "/brokers/topics"
  val topicConfigs = "/config/topics"
  val topicConfigChanges = "/config/changes"
  val controller = "/controller"
  val controllerEpoch = "/controller_epoch"
  val reassignPartitions = "/admin/reassign_partitions"
  val deleteTopics = "/admin/delete_topics"
  val preferredReplicaLeaderElection = "/admin/preferred_replica_election"
  val admin = "/admin"

  def topic(topic_name: String): String = brokerTopics + "/" + topic_name
  def topicPartitions(topic_name: String): String = topic(topic_name) + "/partitions"
  def topicPartition(topic_name: String, partitionId: Int): String = topicPartitions(topic_name) + "/" + partitionId
  def topicPartitionState(topic_name: String, partitionId: Int): String = topicPartition(topic_name, partitionId) + "/" + "state"
  def topicConfig(topic_name: String): String = topicConfigs + "/" + topic_name
  def deleteTopic(topic_name: String): String = deleteTopics + "/" + topic_name

}
