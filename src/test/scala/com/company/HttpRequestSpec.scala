package com.company

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.ContentTypes._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import akka.http.scaladsl.server.Route
import akka.util.ByteString
import com.company.models.JobSummary
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import com.typesafe.config.Config

class HttpRequestSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  implicit val config: Config = testConfig

  val maxSummaryJobs: Int = config.getInt("max-summary-jobs")

  lazy val routes: Route = new Routes().jobRoutes

  "JobRoutes" should {
    "submit a job and return 201 (POST /submit)" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |  "jobId": "1",
           |  "priority": 1,
           |  "config": {
           |    "divined": 1,
           |    "divisor": 1
           |  }
           |}
        """.stripMargin)

      Post("/submit").withEntity(`application/json`, jsonRequest) ~> routes ~> check {
        status shouldBe StatusCodes.Created
      }
    }

    "get job status by id (GET /status/)" in {
      Get("/status/1") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe `application/json`
      }
    }

    "get job config by id (GET /config/)" in {
      Get("/config/1") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe `application/json`
      }
    }

    "return NOT FOUND for job status with unknown id (GET /status/)" in {
      Get("/status/2") ~> routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "return NOT FOUND for job config with unknown id (GET /config/)" in {
      Get("/config/2") ~> routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    "get summary count for all jobs (GET /summary)" in {
      Get("/summary") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe `application/json`
        responseAs[JobSummary] shouldBe JobSummary(0, 0, succeeded = 1, 0)
      }
    }
  }

}
