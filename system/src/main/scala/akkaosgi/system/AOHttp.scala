package akkaosgi.system

import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.server.Route
import akkaosgi.system.impl.DefaultHttpActor

/**
  * Created by pappmar on 25/11/2015.
  */
object AOHttp extends AORef[Route] {

//  def register(route: Route) : ActorRef = {
//    val actor = AOSystem.actorSystem.actorOf(Props(classOf[DefaultHttpActor], route))
//    register(actor)
//    actor
//  }

}
