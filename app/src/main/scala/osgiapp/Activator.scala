package osgiapp

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import org.osgi.framework.{BundleContext, BundleActivator}
import org.osgi.service.log.LogService

/**
  * Created by marci on 22-11-2015.
  */
class Activator extends ActorSystemActivator {

  override def configure(context: BundleContext, system: ActorSystem): Unit = {
    val msg = "starting 14!!!!"

    println(msg)
    val ref = context.getServiceReference(classOf[LogService])
    val log = context.getService(ref)
    log.log(LogService.LOG_INFO, msg)

    server = Some(new Server)
    server.get.start()

  }

  var server : Option[Server] = None

  override def stop(context: BundleContext): Unit = {
    server.foreach( _. stop() )
    super.stop(context)
  }
}
