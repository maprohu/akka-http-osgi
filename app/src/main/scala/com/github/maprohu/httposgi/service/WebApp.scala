package com.github.maprohu.httposgi.service

import akka.http.scaladsl.server

/**
  * Created by marci on 24-11-2015.
  */
trait WebApp {

  def context : String
  def route: server.Route

}
