package com.nrinaudo.eshitsuji.monitor

import scala.actors._
import scala.actors.Actor._
import NameMatcher._

/** Provides simple interaction primitives with `NameMatcher` and automated wake-up calls.
  *
  * Implementations of this class will automatically receive a `Timer.WakeUp` actor message every `refreshRate`
  * seconds. This message is meant to allow them to update their state and call `associate` whenever appropriate.
  *
  * @param  matcher instance of `NameMatcher` with which to interact.
  * @param  notifer actor to notify whenever `associate` finds a match.
  * @author         Nicolas Rinaudo
  */
abstract class NameMonitor(protected val matcher: NameMatcher, private val notifier: Actor) extends Actor with Iterable[String] {
  /** Adds the specified entry to the list of monitored names. */
  def +=(name: String): this.type = {matcher ! AddName(name); this}

  /** Removes the specified entry from the list of monitored names. */
  def -=(name: String): this.type = {matcher ! RemoveName(name); this}

  /** Requests the specified value to be associated with the specified name, triggering the specified message
    * on success.
    *
    * @param name    name to which `value` will be associated.
    * @param value   value to associate with `name`.
    * @param message message to send to the underlying listener whenever a new association is succesfully created.
    */
  def associate(name: String, value: String, message: Any) = matcher ! Associate(name, value, notifier, message)

  // There *must* be a better way than runtime casts, but I've yet to find it.
  override def iterator = (matcher !? ListNames()).asInstanceOf[Iterable[String]].iterator

  /** Returns the delay, in seconds, between two `Timer.WakeUp` events. */
  def refreshRate(): Int

  override def start(): this.type = {
    super.start()
    matcher.start()
    Timer(refreshRate * 1000, this)
    this
  }
}
