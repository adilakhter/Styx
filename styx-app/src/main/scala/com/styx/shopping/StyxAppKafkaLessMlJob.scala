package com.styx.shopping

import com.typesafe.config.Config
import com.styx.common.ConfigUtils

/**
  * This job runs all processes in one environment, in 1 long pipeline without intermediate kafka busses (except the datagen).
  *
  * There are also independent jobs available if each one needs to run in its own environment/job
  */
object StyxAppKafkaLessMlJob {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigUtils.loadConfig(args)

    val jobFactory = ConfigBasedJobBuilderDefaults.datagenJobWithDefaultsGivenConfig(Some(config))
    for(rawSource <- jobFactory.randomBusinessEvents();
        scoresStream <- jobFactory.businessEventsToNotificationEvents(rawSource);
      ccStream <- jobFactory.notificationEventsToCcEvents(scoresStream);
         _ <- jobFactory.ccEventsToKafka(ccStream)
    ){
    }
    jobFactory.execute()

  }
}
