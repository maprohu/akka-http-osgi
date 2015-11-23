package osgiapp

import akka.actor.{Props, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives
import akka.osgi.ActorSystemActivator
import akka.stream.ActorMaterializer
import org.osgi.framework.{BundleContext, BundleActivator}
import org.osgi.service.log.LogService
import scala.concurrent.duration._

import scala.concurrent.{Future, Await}

/**
  * Created by marci on 22-11-2015.
  */
class Activator extends ActorSystemActivator with Directives {

  val timeout = 10 seconds

  var binding : Option[Future[ServerBinding]] = None

  override def configure(context: BundleContext, system: ActorSystem): Unit = {
//    val msg = "starting 14!!!!"
//    println(msg)

    implicit val sys = system


    implicit val materializer = ActorMaterializer()

    val routes =
      get {
        pathEndOrSingleSlash {
          complete {
            "hello"
          }
        }
      }

    val handle = Http().bindAndHandle(routes, "0.0.0.0", 8888)
    import sys.dispatcher
    handle.onComplete(println(_))
    binding = Some(handle)


//    val ref = context.getServiceReference(classOf[LogService])
//    val log = context.getService(ref)
//    log.log(LogService.LOG_INFO, msg)

//    server = Some(new Server()(system))
//    server.get.start()

//    system.actorOf(Props[ServerActor])

  }



//  var server : Option[Server] = None
//
//  override def stop(context: BundleContext): Unit = {
//    server.foreach( _. stop() )
//    super.stop(context)
//  }
  override def stop(context: BundleContext) = {
    binding.foreach { f =>
      println("unbinding")
      val b = Await.result(f, timeout)
      Await.ready(b.unbind(), timeout)
    }

    super.stop(context)
  }
}
