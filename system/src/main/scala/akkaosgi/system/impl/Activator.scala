package akkaosgi.system.impl

import akka.actor.ActorSystem
import akka.osgi.ActorSystemActivator
import akkaosgi.system.AOSystem
import org.osgi.framework.BundleContext


/**
  * Created by pappmar on 25/11/2015.
  */
class Activator extends ActorSystemActivator {

  override def configure(context: BundleContext, system: ActorSystem): Unit = {
    AOSystem.init(system)
    system.log.info("AOSystem started")
  }

}
