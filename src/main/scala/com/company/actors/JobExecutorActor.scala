package com.company.actors

import akka.actor.{Actor, ActorLogging}
import com.company.models.Job
import com.company.models.JobStatus.Running

class JobExecutorActor extends Actor with ActorLogging {

  def receive: PartialFunction[Any, Unit] = {
    case job: Job =>
      log.debug("Execution a job: {}", job)

      sender() ! job.withStatusAndResult(Running)
      sender() ! job.compute
    case _ =>
      log.error("JobExecutor: something going wrong")
  }

}
