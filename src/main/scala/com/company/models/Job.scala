package com.company.models

import com.company.models.JobStatus.{Failed, Succeeded, Unknown}
import io.circe.{Decoder, HCursor, Json}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps

import java.time.Instant

sealed trait Job {
  def jobId: String
  def name: String
  def priority: Int
  def config: Json
  def result: Json
  def status: JobStatus
  def createdAt: Instant

  def compute: Job

  def withStatusAndResult(status: JobStatus, result: Json = Json.Null): Job
}

case class JobDivision(jobId: String,
                       priority: Int,
                       config: Json,
                       result: Json,
                       status: JobStatus = Unknown,
                       createdAt: Instant = Instant.now) extends Job {

  case class Config(divined: Double, divisor: Double)

  override val name = "divisionOfTwoNumbers"

  def compute: JobDivision = config.as[Config] match {
    case Left(value) =>
      this.copy(status = Failed, result = Map("error" -> s"Bad request with json $value").asJson)
    case Right(value) if value.divisor == 0 =>
      this.copy(status = Failed, result = Map("error" -> "Division by 0").asJson)
    case Right(value) =>
      this.copy(status = Succeeded, result = Map("result" -> value.divined / value.divisor).asJson)
  }

  override def withStatusAndResult(status: JobStatus, result: Json): Job =
    this.copy(status = status, result = result)
}

object JobDivision {

  implicit val decodeJobDivision: Decoder[Job] = (c: HCursor) => for {
    foo <- c.downField("jobId").as[String]
    bar <- c.downField("priority").as[Int]
    config <- c.downField("config").as[Json]
  } yield {
    JobDivision(foo, bar, config, Json.Null)
  }

}
