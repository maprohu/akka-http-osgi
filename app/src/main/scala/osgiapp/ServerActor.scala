package osgiapp

import akka.actor.Actor
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.pattern._

/**
  * Created by marci on 23-11-2015.
  */
class ServerActor extends Actor with Directives {

//  import context.system
  import context.dispatcher
  implicit val materializer = ActorMaterializer()

  val routes =
    get {
      pathEndOrSingleSlash {
        complete {
          "hello"
        }
      }
    }

  val binding = Http()(context.system).bindAndHandle(routes, "0.0.0.0", 8888)

  def receive = {
    case _ =>
  }

  @throws[Exception](classOf[Exception])
  override def postStop() = {
    binding.onSuccess { case b =>
      println("unbinding...")
      b.unbind()
    }
  }
}
