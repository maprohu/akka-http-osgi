package com.github.maprohu.httposgi.sample

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.osgi.ActorSystemActivator
import com.github.maprohu.httposgi.service.WebApp
import org.osgi.framework.{BundleActivator, BundleContext, ServiceEvent, ServiceListener}

import scala.collection.JavaConversions
import scala.concurrent.duration._

/**
  * Created by marci on 22-11-2015.
  */
class Activator extends BundleActivator with Directives {

  def start(context: BundleContext) = {
    context.registerService(
      classOf[WebApp],
      SampleWebapp,
      null
    )


  }

  def stop(context: BundleContext) = {}
}
