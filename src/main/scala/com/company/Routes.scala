package com.company

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.company.actors.CoordinatorActor
import com.company.models.{Job, JobDivision, JobSummary}
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import Directives._
import com.company.actors.CoordinatorActor.{GetJobConfig, GetJobStatus, GetJobSummary}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.extras.Configuration
import io.circe.syntax.EncoderOps
import io.circe.generic.auto._
import JobDivision.decodeJobDivision
import com.typesafe.config.Config

import scala.concurrent.duration.DurationInt

class Routes(implicit val system: ActorSystem, config: Config) {

  implicit val timeout: Timeout = Timeout(5.seconds)

  val parallelism: Int = config.getInt("parallelism")
  val maxSummaryJobs: Int = config.getInt("max-summary-jobs")

  val coordinatorActor: ActorRef = system.actorOf(Props(classOf[CoordinatorActor], parallelism), "coordinator")

  implicit val circeConfig: Configuration = Configuration.default.withDefaults

  val jobRoutes: Route =
    concat(
      post {
        path("submit") {
          entity(as[Job]) { job =>
            onSuccess(ask(coordinatorActor, job)) { _ =>
              complete(StatusCodes.Created)
            }
          }
        }
      },
      get {
        path("status" / Segment) { jobId =>
          onSuccess(ask(coordinatorActor, GetJobStatus(jobId)).mapTo[Option[Job]]) {
            case Some(job) => complete(
              Map(
                "status" -> job.status.toString.toLowerCase,
                "name" -> job.name,
                "result" -> job.result.noSpaces
              ).asJson
            )
            case None => complete(StatusCodes.NotFound)
          }
        }
      },
      get {
        path("config" / Segment) { jobId =>
          onSuccess(ask(coordinatorActor, GetJobConfig(jobId)).mapTo[Option[Job]]) {
            case Some(job) => complete(
              Map(
                "status" -> job.status.toString.toLowerCase,
                "name" -> job.name,
                "config" -> job.config.noSpaces
              ).asJson
            )
            case None => complete(StatusCodes.NotFound)
          }
        }
      },
      get {
        path("summary") {
          onSuccess(ask(coordinatorActor, GetJobSummary).mapTo[JobSummary]) { summary =>
            complete(summary.withLimit(maxSummaryJobs))
          }
        }
      }
    )

}
