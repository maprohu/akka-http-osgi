package akkaosgi.system

import akka.actor.{Props, ActorContext, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.{Route, Directives, RoutingSetup}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akkaosgi.system.impl.DefaultHttpActor
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import akka.pattern.ask

/**
  * Created by pappmar on 25/11/2015.
  */
object AOSystem extends AOTimeout {

  private [akkaosgi] var vars : Vars = null

  implicit def actorSystem = vars.actorSystem
  implicit def actorMaterializer = vars.actorMaterializer
  implicit def http = vars.http
  implicit def routingSetup = vars.routingSetup
  implicit def dispatcher = actorSystem.dispatcher



}

class Vars(
  implicit val actorSystem : ActorSystem
) {
  val actorMaterializer = ActorMaterializer()
  val http = Http()
  def routingSetup(implicit routingSetup: RoutingSetup) = routingSetup
}

trait AOActivator extends AOTimeout {
  implicit val actorSystem = AOSystem.actorSystem
}

object AOActivator extends AOActivator

trait AOTimeout {

  val timeout = 5 seconds
  implicit val akkaTimeout : Timeout = Timeout(timeout)

}

object AOTimeout extends AOTimeout

trait AOActor extends AOTimeout {

  def context : ActorContext
  implicit val dispatcher = context.dispatcher

}

trait AOExecutionContext {
  implicit val dispathcer : ExecutionContextExecutor = AOSystem.actorSystem.dispatcher
}

trait AOMaterializer {
  implicit val actorMaterializer : ActorMaterializer = AOSystem.actorMaterializer
}

trait AORouter {
  implicit val routingSetup = AOSystem.routingSetup
}


object AOInitializer {

  def init(system: ActorSystem) = {

    AOSystem.vars = new Vars()(system)

    import AOSystem._

    val httpActor = system.actorOf(Props(classOf[DefaultHttpActor], AOHttp.ref))

    val binding = AOSystem.http.bindAndHandleAsync(
      req => (httpActor ? req).mapTo[HttpResponse],
      interface = "127.0.0.1",
      port = 8888
    )
    binding.onComplete(b => system.log.info(b.toString))

    import Directives._
    val route : Route = complete {
      "no routes - akkaosgi-system ver. " + this.getClass.getPackage.getImplementationVersion
    }
    AOHttp.register(route)
  }

}

