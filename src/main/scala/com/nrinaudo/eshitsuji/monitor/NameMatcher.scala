package com.nrinaudo.eshitsuji.monitor

import com.nrinaudo.eshitsuji.storage._
import scala.actors._
import scala.actors.Actor._

/** Actor used to interact with a `NameStore` through asynchronous messages.
  *
  * Within the context of this class, a name is considered to be a regex - only values that match it will be accepted
  * in associations.
  *
  * @param  store store with which to interract.
  * @param  ttl   number of milliseconds an association is allowed to live (ignored if set to -1).
  * @author       Nicolas Rinaudo
  */
class NameMatcher(private val store: NameStore, val ttl: Long = -1l) extends Actor {
  import NameMatcher._

  /** Returns an association between the specified name and the corresponding pattern. */
  private def matcher(name: String) = (name -> ("^(?i).*" + name + ".*").r.pattern)

  def act() {
    val names = collection.mutable.HashMap[String, java.util.regex.Pattern]()
    store foreach {name => names += matcher(name)}

    loop {
      react {
        // A new name should be monitored.
        case AddName(name) => if(store.add(name)) names += matcher(name)

        // An old name shouldn't be monitored anymore.
        case RemoveName(name) => if(store.remove(name)) names -= name

        // A new association is attempted.
        case Associate(name, value, dest, msg) =>
          names find {item => item._2.matcher(name).matches} foreach {item =>
            if(store.associate(item._1, value)) {
              if(ttl != -1) store.cleanOlderThan(new java.util.Date(System.currentTimeMillis - ttl))
              dest ! msg
            }
          }

        // Someone requested the list of monitored names.
        case ListNames() => reply {names map {_._1}}
      }
    }
  }
}

/** Defines case classes for the various messages accepted by `NameMatcher`. */
object NameMatcher {
  /** Message sent to the matcher to start matching a new name. */
  case class AddName(name: String)

  /** Message sent to the matcher to stop matching an existing name. */
  case class RemoveName(name: String)

  /** Message sent to the matcher to attempt a new association.
    *
    * @param name  name to which `value` should be associated.
    * @param value value to associate with `name`.
    * @param dest  actor to notify upon successful association.
    * @param msg   message to send to `dest` upon successful association.
    */
  case class Associate(name: String, value: String, dest: Actor, msg: Any)

  /** Message sent to the matcher to request a list of all monitored named. */
  case class ListNames()
}
