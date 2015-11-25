package akkaosgi.system

import akka.actor.{PoisonPill, ActorRef}
import akka.pattern.pipe

/**
  * Created by pappmar on 25/11/2015.
  */
trait AORef {

  object Changed

  def ref = _ref

  @volatile
  private var _ref : ActorRef = null

  private var refs = Seq[ActorRef]()

  def register(newRef: ActorRef): Unit = {
    refs = newRef +: refs
    _ref = newRef

    publish
  }

  def publish {
    listeners foreach (_ ! Changed)
  }

  def unregister(oldRef: ActorRef) = {
    val old = _ref

    refs = refs diff Seq(oldRef)
    _ref = refs.headOption.getOrElse(null)

    if (old != _ref) publish
  }

  def unregisterAndStop(oldRef: ActorRef) = {
    unregister(oldRef)
    oldRef ! PoisonPill
  }

  private var listeners = Set[ActorRef]()

  def listen(listener: ActorRef): Unit = {
    listeners += listener
  }

  def unlisten(listener: ActorRef) = {
    listeners -= listener
  }

}
