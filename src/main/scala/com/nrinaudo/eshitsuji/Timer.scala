package com.nrinaudo.eshitsuji

import scala.actors._
import scala.actors.Actor._
import scala.actors.scheduler.DaemonScheduler

/** Simple [[com.scala.actors.Actor]] whose role is to wake another actor at configurable intervals.
  *
  * The destination actor will receive a `WAKEUP` message every `timeout` milliseconds.
  *
  * For example:
  * {{{
  * new Timer(1000, actor {
  * loop {
  *   react {
  *     case Timer.WAKEUP => println("Woken up")
  *   }
  * }
  * }).start()
  * }}}
  *
  * The companion object provides a helper method for creating and starting a timer in one call.
  *
  * @param  timeout number of milliseconds between two wake up calls.
  * @param  dest    actor to wake up.
  * @author Nicolas Rinaudo
  */
class Timer(val timeout: Long, private val dest: Actor) extends DaemonActor {
  def act {
    // If the destination actor is dead, there is no point in keeping the timer active.
    link(dest)

    dest ! Timer.WAKEUP
    loop {
      reactWithin(timeout) {
        case TIMEOUT => dest ! Timer.WAKEUP
      }
    }
  }
}

object Timer {
  /** Message passed to actors to notify them that they should wake. */
  val WAKEUP = 'WakeUp

  /** Convenience method for creating and starting a `Timer` in a single call.
    *
    * @param timeout number of milliseconds between two wake up calls.
    * @param dest    actor that will receive wake up messages.
    * @return        the newly started timer.
    */
  def apply[A >: Actor](timeout: Long, dest: Actor): A = new Timer(timeout, dest).start
}
