package akkaosgi.system

import akka.actor.{PoisonPill, ActorRef}
import akka.pattern.pipe
import rx._

/**
  * Created by pappmar on 25/11/2015.
  */
trait AORef[T] {

  private val refs = Var(Seq[Rx[T]]())
  val ref : Rx[Option[T]] = Rx(refs().headOption.map(_()))


  def register(value : T) : Var[T] = {
    val v = Var(value)
    register(v)
    v
  }

  def register(value: Rx[T]) : Unit = this.synchronized {
    refs() = value +: refs()
  }

  def unregister(value: Rx[T]) : Unit = this.synchronized {
    refs() = refs() diff Seq(value)
  }

}
