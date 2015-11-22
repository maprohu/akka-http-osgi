//package osgiapp
//
//import akka.actor.ActorSystem
//import akka.event.Logging
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.server.Directives
//import akka.stream.ActorMaterializer
//
//object Main extends App with Directives {
//  implicit val system = ActorSystem()
//  implicit val executor = system.dispatcher
//  implicit val materializer = ActorMaterializer()
//
//  val logger = Logging(system, getClass)
//
//  val routes =
//    get {
//      pathEndOrSingleSlash {
//        complete {
//          "hello"
//        }
//      }
//    }
//
//  Http().bindAndHandle(routes, "0.0.0.0", 8888).onComplete(println(_))
//}
