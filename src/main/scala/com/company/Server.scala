package com.company

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Server extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContext = system.dispatcher

  implicit val config: Config = ConfigFactory.load()

  val routes = new Routes().jobRoutes

  val futureBinding = Http()
    .newServerAt(config.getString("http.interface"), config.getInt("http.port"))
    .bind(routes)

  futureBinding.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
    case Failure(ex) =>
      system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()
  }

}
