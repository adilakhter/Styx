package com.styx.runner

import com.styx.common.LogFutureImplicit._
import com.styx.common.Logging
import com.styx.shopping.StyxCepJob
import com.styx.support.datagen.RawEventProducer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object StyxCepRunner extends Logging {

  def main(args: Array[String]): Unit = {
    val predefinedArgs = Array[String]() // ("--topic", "updatecardbalancetesttopic16", "--bootstrap.servers", "localhost:9092", "--zookeeper.connect", "localhost:2181", "--group.id", "styx--consumergroup")
    val flink = Future {
      StyxCepJob.main(predefinedArgs ++ args)
    }
    flink.logFailure(e => logger.error("Failed to run flink job", e))
    logger.info("Started flink processing.")
    logger.info("Will now start the raw event producer")
    RawEventProducer.main(predefinedArgs ++ args)
    val wait = 5 * 1000
    logger.warn(s"Waiting for $wait additional ms to allow flink to process the events")
    Thread.sleep(wait)
    logger.warn("Done, now would be safe to kill the jvm")
    scala.io.StdIn.readLine("Press <enter>  to stop this program: .... ")
    Runtime.getRuntime.exit(0)
  }
}
