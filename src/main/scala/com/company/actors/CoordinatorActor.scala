package com.company.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.RoundRobinPool
import com.company.actors.CoordinatorActor.{GetJobConfig, GetJobStatus, GetJobSummary, Tick}
import com.company.models.{Job, JobSummary}
import com.company.models.JobStatus.{Failed, Pending, Running, Succeeded, Unknown}
import io.circe.syntax.EncoderOps

import java.time.Instant
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

object CoordinatorActor {
  final case class GetJobStatus(jobId: String)
  final case class GetJobConfig(jobId: String)
  final case object GetJobSummary

  object Tick
}

class CoordinatorActor(parallelism: Int) extends Actor with ActorLogging {

  var repo: mutable.Map[String, Job] = mutable.Map[String, Job]()

  context.system.scheduler.scheduleAtFixedRate(1.seconds, 1.seconds, self, Tick)

  val jobExecutorPool: ActorRef = context.actorOf(
    RoundRobinPool(parallelism).props(Props[JobExecutorActor]().withMailbox("priority-mailbox")), "pool"
  )

  def receive: PartialFunction[Any, Unit] = {
    case Tick =>
      repo ++= repo.collect {
        case (jobId, job) if (job.createdAt.plusMillis(5.seconds.toMillis) isAfter Instant.now) && job.status != Succeeded =>
          jobId -> job.withStatusAndResult(Failed, Map("error" -> "Job timeout").asJson)
      }

    case job: Job if job.status == Unknown =>
      log.debug("Submit a job: {}", job)

      val updatedJob = job.withStatusAndResult(Pending)

      repo += (updatedJob.jobId -> updatedJob)

      jobExecutorPool ! updatedJob

      sender() ! ()
    case job: Job =>
      log.debug("Submit a job: {}", job)

      repo += (job.jobId -> job)

    case GetJobStatus(jobId) =>
      log.debug(s"Get status for job with id: {}", jobId)

      sender() ! repo.get(jobId)

    case GetJobConfig(jobId) =>
      log.debug("Get config for job with id: {}", jobId)

      sender() ! repo.get(jobId)

    case GetJobSummary =>
      log.debug("Get summary")

      val summary = repo.foldLeft(JobSummary(pending = 0, running = 0, succeeded = 0, failed = 0)) {
        case (summary: JobSummary, (_: String, job: Job)) => job.status match {
          case Pending => summary.copy(pending = summary.pending + 1)
          case Running => summary.copy(running = summary.running + 1)
          case Succeeded => summary.copy(succeeded = summary.succeeded + 1)
          case Failed => summary.copy(failed = summary.failed + 1)
          case e => throw new RuntimeException(e.toString)
        }
      }

      sender() ! summary
    case _ =>
      log.error("Coordinator: something going wrong")
  }

}
