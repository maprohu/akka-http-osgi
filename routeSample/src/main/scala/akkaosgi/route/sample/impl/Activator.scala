package akkaosgi.route.sample.impl

import akka.actor.{PoisonPill, ActorRef}
import akka.http.scaladsl.server.Directives
import akkaosgi.system._
import org.osgi.framework.{BundleContext, BundleActivator}
import akka.pattern.ask
import akka.pattern.pipe

import scala.concurrent.Future

/**
  * Created by pappmar on 25/11/2015.
  */
class Activator extends BundleActivator with Directives with AOActivator {
  val route =
    complete {
      "akkaosgi-route-sample ver. " + this.getClass.getPackage.getImplementationVersion
    }


  var ref : ActorRef = null

  override def start(context: BundleContext): Unit = {
    ref = AOHttp.register(route)
  }
  override def stop(context: BundleContext): Unit = {
    AOHttp.unregisterAndStop(ref)
    ref = null
  }

}
