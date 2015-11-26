package akkaosgi.route.sample.impl

import akka.actor.{PoisonPill, ActorRef}
import rx._
import akka.http.scaladsl.server.{Route, Directives}
import akkaosgi.system._
import org.osgi.framework.{BundleContext, BundleActivator}
import akka.pattern.ask
import akka.pattern.pipe

import scala.concurrent.Future

/**
  * Created by pappmar on 25/11/2015.
  */
class Activator extends AORegisteringActivator[Route](AOHttp) with Directives with AOActivator {
  override def create: Rx[Route] = Rx {
    complete {
      "akkaosgi-route-sample ver. " + this.getClass.getPackage.getImplementationVersion
    }
  }
}
