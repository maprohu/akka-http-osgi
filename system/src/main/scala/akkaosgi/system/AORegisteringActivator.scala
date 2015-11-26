package akkaosgi.system

import org.osgi.framework.{BundleContext, BundleActivator}
import rx._

/**
  * Created by pappmar on 26/11/2015.
  */
abstract class AORegisteringActivator[T](target: AORef[T]) extends BundleActivator {

  def create : Rx[T]

  var ref : Rx[T] = null

  override def start(context: BundleContext): Unit = {
    ref = create
    target.register(ref)
  }
  override def stop(context: BundleContext): Unit = {
    target.unregister(ref)
    ref.killAll()
    ref = null
  }

}
