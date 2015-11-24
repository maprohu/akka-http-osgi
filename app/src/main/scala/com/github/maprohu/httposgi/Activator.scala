package com.github.maprohu.httposgi

import akka.actor.{Props, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives
import akka.osgi.ActorSystemActivator
import akka.stream.ActorMaterializer
import com.github.maprohu.httposgi.service.WebApp
import org.osgi.framework._
import org.osgi.service.log.LogService
import scala.collection.JavaConversions
import scala.concurrent.duration._

import scala.concurrent.{Future, Await}

/**
  * Created by marci on 22-11-2015.
  */
class Activator extends ActorSystemActivator with Directives {

  type Ref = ServiceReference[WebApp]
  type Queue = Seq[Ref]

  val timeout = 10 seconds

  var serverOpt: Option[Server] = None

  override def configure(context: BundleContext, system: ActorSystem): Unit = {

    implicit val sys = system
    implicit val materializer = ActorMaterializer()
    val server = new Server

    val webapps = context.getServiceReferences(classOf[WebApp], null)

    import JavaConversions._


    var queue = Map[String, Queue]().withDefaultValue(Seq())

    def updateServer(ctx: String, refs: Queue) = {
      if (refs.isEmpty) {
        server.routeMap -= ctx
      } else {
        server.routeMap += ctx -> context.getService(refs.head).route
      }
    }

    def modifyService(ref: Ref)(f: Queue => Queue) = this.synchronized {
      val service = context.getService(ref)
      val ctx = service.context
      val modified = f(queue(ctx))
      queue += ctx -> modified
      updateServer(ctx, modified)
    }

    def addService(ref: Ref) = modifyService(ref)(q => ref +: (q diff Seq(ref)))

    def removeService(ref: ServiceReference[WebApp]) = modifyService(ref)(_ diff Seq(ref))

    webapps.foreach(addService(_))

    context.addServiceListener(
      new ServiceListener {
        def serviceChanged(event: ServiceEvent) = {
          val s = event.getServiceReference.asInstanceOf[Ref]
          event.getType match {
            case ServiceEvent.UNREGISTERING =>
              removeService(s)
            case _ =>
              addService(s)
          }
        }
      },
      s"(objectClass=${classOf[WebApp].getName})"
    )



    serverOpt = Some(server)


    context.registerService(classOf[ActorMaterializer], materializer, null)


//    val ref = context.getServiceReference(classOf[LogService])
//    val log = context.getService(ref)
//    log.log(LogService.LOG_INFO, msg)


  }



  override def stop(context: BundleContext) = {
    serverOpt.foreach(_.stop())
    super.stop(context)
  }
}
