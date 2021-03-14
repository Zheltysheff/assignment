package com.company.actors

import akka.actor.{ActorSystem, PoisonPill}
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.company.models.Job
import com.typesafe.config.Config

class PriorityMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
  PriorityGenerator {
    case job: Job => job.priority
    case PoisonPill => Int.MaxValue
    case _ => 0
  })
