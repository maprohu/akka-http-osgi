package akkaosgi.system

import akka.actor.{Props, ActorContext, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.{Directives, RoutingSetup}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akkaosgi.system.impl.DefaultHttpActor
import scala.concurrent.duration._
import akka.pattern.ask

/**
  * Created by pappmar on 25/11/2015.
  */
object AOSystem {

  private var vars : Vars = null

  implicit def actorSystem = vars.actorSystem
  implicit def actorMaterializer = vars.actorMaterializer
  implicit def http = vars.http
  implicit def routingSetup = vars.routingSetup

  private [akkaosgi] def init(system: ActorSystem) = {

    vars = new Vars()(system)

    import AOActivator._
    val binding = AOSystem.http.bindAndHandleAsync(
      req => (AOHttp.ref.ask(req)(akkaTimeout)).mapTo[HttpResponse],
      interface = "127.0.0.1",
      port = 8888
    )
    binding.onComplete(b => system.log.info(b.toString))(dispathcer)

    import Directives._
    val route = complete {
      "no routes - akkaosgi-system ver. " + this.getClass.getPackage.getImplementationVersion
    }
    AOHttp.register(system.actorOf(Props(classOf[DefaultHttpActor], route)))
  }


}

private class Vars(
  implicit val actorSystem : ActorSystem
) {
  val actorMaterializer = ActorMaterializer()
  val http = Http()
  def routingSetup(implicit routingSetup: RoutingSetup) = routingSetup
}

trait AOActivator extends AOTimeout {
  implicit val actorSystem = AOSystem.actorSystem
  implicit val dispathcer = actorSystem.dispatcher
}

object AOActivator extends AOActivator

trait AOTimeout {

  val timeout = 5 seconds
  implicit val akkaTimeout = Timeout(timeout)

}

object AOTimeout extends AOTimeout

trait AOActor extends AOTimeout {

  def context : ActorContext
  implicit val dispatcher = context.dispatcher

}

trait AOMaterializer {
  implicit val actorMaterializer = AOSystem.actorMaterializer
}

trait AORouter {
  implicit val routingSetup = AOSystem.routingSetup
}