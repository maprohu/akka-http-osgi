package osgiapp

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer

import scala.concurrent._
import scala.concurrent.duration._

class Server(implicit val system : ActorSystem) extends Directives {

  val timeout = 10 seconds

  var binding : Option[Future[ServerBinding]] = None

  def start() = {
    implicit val executor = system.dispatcher
    implicit val materializer = ActorMaterializer()

    val logger = Logging(system, getClass)

    val routes =
      get {
        pathEndOrSingleSlash {
          complete {
            "hello"
          }
        }
      }

    val bnd = Http().bindAndHandle(routes, "0.0.0.0", 8888)
    bnd.onComplete(println(_))
    binding = Some(bnd)
  }

  def stop(): Unit = {
    binding.foreach{ f =>
      val b = Await.result(f, timeout)
      Await.ready(b.unbind(), timeout)
    }
  }

}
