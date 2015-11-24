package osgiapp

import akka.actor.{Props, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives
import akka.osgi.ActorSystemActivator
import akka.stream.ActorMaterializer
import org.osgi.framework.{ServiceEvent, ServiceListener, BundleContext, BundleActivator}
import org.osgi.service.log.LogService
import osgiapp.service.WebApp
import scala.collection.JavaConversions
import scala.concurrent.duration._

import scala.concurrent.{Future, Await}

/**
  * Created by marci on 22-11-2015.
  */
class Activator extends ActorSystemActivator with Directives {

  val timeout = 10 seconds

  var serverOpt: Option[Server] = None

  override def configure(context: BundleContext, system: ActorSystem): Unit = {

    implicit val sys = system
    val server = new Server

    val webapps = context.getServiceReferences(classOf[WebApp], null)

    import JavaConversions._

    server.routeMap ++= webapps.map({ ref =>
      val s = context.getService(ref)
      s.context -> s.route
    }).toMap

    context.addServiceListener(
      new ServiceListener {
        def serviceChanged(event: ServiceEvent) = {
          val s = context.getService(event.getServiceReference).asInstanceOf[WebApp]
          event.getType match {
            case ServiceEvent.UNREGISTERING =>
              server.routeMap -= s.context
            case ServiceEvent.REGISTERED =>
              server.routeMap += s.context -> s.route
          }
        }
      },
      s"(objectClass=${classOf[WebApp].getName})"
    )



    serverOpt = Some(server)




//    val ref = context.getServiceReference(classOf[LogService])
//    val log = context.getService(ref)
//    log.log(LogService.LOG_INFO, msg)


  }



  override def stop(context: BundleContext) = {
    serverOpt.foreach(_.stop())
    super.stop(context)
  }
}
