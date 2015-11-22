package osgiapp

import org.osgi.framework.{BundleContext, BundleActivator}

/**
  * Created by marci on 22-11-2015.
  */
class Activator extends BundleActivator {
  def stop(context: BundleContext) = {println("stopping")}

  def start(context: BundleContext) = {println("starting")}
}
