package akkaosgi.system.impl

import akka.actor.Actor
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server._
import akka.pattern.pipe
import akkaosgi.system.{AOActor, AORouter, RegisterRoute, UnregisterRoute}

/**
  * Created by pappmar on 25/11/2015.
  */
class DefaultHttpActor(route: Route) extends Actor with AOActor with AORouter {

  override def receive: Receive = {
    val flow = createRouteHandler(route)

    {
      case req : HttpRequest =>
        flow(req) pipeTo sender
    }
  }

  def createRouteHandler(route: Route) = {
    Route.asyncHandler(route)
  }
}


