package osgiapp.sample

import akka.http.scaladsl.server.Directives
import osgiapp.service.WebApp

/**
  * Created by marci on 24-11-2015.
  */
object SampleWebapp extends WebApp with Directives {
  def context = "sample"

  def route = get {
    complete {
      "sample !!!"
    }
  }

}
