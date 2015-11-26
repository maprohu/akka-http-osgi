package akkaosgi.system.impl

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import akkaosgi.system.{AOInitializer, AOSystem}
import org.osgi.framework.BundleContext


/**
  * Created by pappmar on 25/11/2015.
  */
class Activator extends ActorSystemActivator {

  var system : ActorSystem = null

  override def configure(context: BundleContext, system: ActorSystem): Unit = {
    AOInitializer.init(system)
    system.log.info("AOSystem started")
    this.system = system
  }

  override def stop(context: BundleContext) = {
    super.stop(context)
    system.awaitTermination()
    system = null
  }
}
