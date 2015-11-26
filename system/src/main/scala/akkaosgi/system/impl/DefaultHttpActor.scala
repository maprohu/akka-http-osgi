package akkaosgi.system.impl

import akka.actor.Actor
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server._
import akka.pattern.pipe
import akkaosgi.system.{AORouter, AOActor}
import rx._

class DefaultHttpActor(routex: Rx[Option[Route]]) extends Actor with AOActor with AORouter {

  val obs = Obs(routex) {
    self ! NewRoute(routex())
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    obs.kill()
    super.postStop()
  }

  override def receive: Receive = waiting

  val waiting : Receive = {
    case NewRoute(route) =>
      context.become(
        route.map(
          working(_) orElse waiting
        ).getOrElse(
          waiting
        )
      )
    case x =>
      println(x)
  }

  def working(route: Route) : Receive = {
    val flow = createRouteHandler(route)

    {
      case req : HttpRequest =>
        flow(req) pipeTo sender
    }
  }


  def createRouteHandler(route: Route) = {
    Route.asyncHandler(route)
  }

  case class NewRoute(route: Option[Route])
}


