package com.styx.jobs

import com.styx.shopping.StyxCepJob
import com.styx.common.ConfigUtils
import com.styx.domain.kafka.{TopicDef, TopicDefManager}
import com.styx.{EmbeddedFlink, LocalKafkaTopic, StyxEmbeddedKafka}
import com.typesafe.config.Config
import org.scalatest.Suites

import scala.collection.JavaConverters._

class EmbeddedShoppingSpec extends Suites with ShoppingSpec with StyxEmbeddedKafka with EmbeddedFlink {

  val jobToBeDeployed: StyxCepJob.type = StyxCepJob

  val jobConfigPrefix = "cep"

  logger.info(s"Runtime.getRuntime.availableProcessors: ${Runtime.getRuntime.availableProcessors()}")

  val parallelism = 4

  val brokerAddress: List[String] = List("localhost:6001")

  val config: Config = ConfigUtils.loadConfig(
    Some("styx-appRunner/src/main/resources/reference.conf"),
    Map(
      "styx." + configNameForDataFile -> "/raw-events.csv",
      "styx.cep.read.bootstrap.servers" -> brokerAddress.asJava,
      "styx.cep.write.bootstrap.servers" -> brokerAddress.asJava,
      "styx.repository.type" -> "stub" // TODO use CassandraUnit for embedded Cassandra
    )
  )

  val rawEventTopicName: String = config.getString("cep.read.topic")
  val businessEventTopicName: String = config.getString("cep.write.topic")

  val topicsForEmbeddedKafka = Seq(
    LocalKafkaTopic(topic = rawEventTopicName, partitions = parallelism),
    LocalKafkaTopic(topic = businessEventTopicName, partitions = parallelism)
  )

  lazy val topicDefinitionMap: Map[String, TopicDef] = TopicDefManager.getKafkaTopics(config.getConfig("kafka"))

  override def producerTopicDef: TopicDef = topicDefinitionMap(rawEventTopicName)

  override def consumerTopicDef: TopicDef = topicDefinitionMap(businessEventTopicName)
}
