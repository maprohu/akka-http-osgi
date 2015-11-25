package akkaosgi.system

import akka.actor.{PoisonPill, ActorRef}

/**
  * Created by pappmar on 25/11/2015.
  */
trait AORef {

  def ref = _ref

  @volatile
  private var _ref : ActorRef = null

  private var refs = Seq[ActorRef]()

  def register(newRef: ActorRef): Unit = {
    refs = newRef +: refs
    _ref = newRef
  }

  def unregister(oldRef: ActorRef) = {
    refs = refs diff Seq(oldRef)
    _ref = refs.headOption.getOrElse(null)
  }

  def unregisterAndStop(oldRef: ActorRef) = {
    unregister(oldRef)
    oldRef ! PoisonPill
  }

}
