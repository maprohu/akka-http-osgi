package com.github.maprohu.httposgi


import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.{server, Http}
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

import scala.concurrent._
import scala.concurrent.duration._

class Server(implicit val system : ActorSystem, materializer: ActorMaterializer) extends Directives {

  val timeout = 10 seconds

  @volatile
  var routeMap : Map[String, server.Route] = Map()

  implicit val executor = system.dispatcher

  val logger = Logging(system, getClass)

  val routes =
    get {
      pathEndOrSingleSlash {
        complete {
          "available: " +
            routeMap.keys.mkString(", ")
        }
      }
    } ~
    path( Segment ) { segment =>
      routeMap
        .get(segment)
        .getOrElse(
          get {
            complete {
              s"nohting for: $segment"
            }
          }
        )
    }

  val handler: Flow[HttpRequest, HttpResponse, Any] = routes


  val http = Http()
  val binding = http.bindAndHandle(
    handler,
    interface = "127.0.0.1",
    port = 8888
  )
  binding.onComplete(r => system.log.info(r.toString))

  def stop(): Unit = {
    system.log.info("unbind")
    val b = Await.result(binding, timeout)
    Await.ready(b.unbind(), timeout)
    system.log.info("pools")
    Await.ready(http.shutdownAllConnectionPools(), timeout)
//    system.log.info("materializer")
//    materializer.shutdown()
    system.log.info("stop end")
  }

}
