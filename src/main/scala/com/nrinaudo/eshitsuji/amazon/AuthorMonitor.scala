package com.nrinaudo.eshitsuji.amazon

import com.nrinaudo.eshitsuji.storage._
import com.nrinaudo.eshitsuji.monitor._
import com.nrinaudo.eshitsuji.monitor.NameMatcher._
import com.nrinaudo.eshitsuji._
import scala.actors._
import scala.actors.Actor._

/** Used to monitor authors on [[http://www.amazon.com Amazon]].
  *
  * Acts as a [[com.nrinaudo.eshitsuji.storage.NameStore]] for the list of monitored authors:
  * {{{
  * // Adds Pratchett to the list of monitored authors.
  * monitor += "Pratchett"
  *
  * // Removes Brown from the list of monitored authors.
  * monitor -= "Brown"
  *
  * // Prints all monitored authors to stdout.
  * monitor foreach println
  * }}}
  *
  * Configuration values, monitored authors and books are stored in the underlying instance of
  * [[com.nrinaudo.eshitsuji.storage.Storage]] according to constants defined in the companion object.
  *
  * Whenever a new book by a monitored author is found, an instance of [[com.nrinaudo.eshitsuji.amazon.Book]] is passed
  * to the registered [[scala.actors.Actor]].
  *
  * @param  db       where to persist data and read configuration values.
  * @param  notifier where to send notifications whenever a new book by a monitored author is discovered.
  * @author          Nicolas Rinaudo
  */
class AuthorMonitor(private val db: Storage, private val notifier: Actor) extends Actor {
  private val matcher = new NameMatcher(new NameStore(db, "Amazon"))



  // - Actor implementation --------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Starts the monitoring process.
    *
    * This will cause a [[com.nrinaudo.eshitsuji.Timer]] to be started and to wake this instance up regularly. Upon
    * wake up, this instance will query [[com.nrinaudo.eshitsuji.amazon.Book]] for new releases.
    */
  override def start(): AuthorMonitor = {
    import AuthorMonitor._

    super.start()
    matcher.start()
    Timer(1000 * (db(RefreshRateKey) map {_.toInt} getOrElse DefaultRefreshRate), this)

    this
  }

  def +=(name: String): AuthorMonitor = {matcher ! AddName(name); this}
  def -=(name: String): AuthorMonitor = {matcher ! RemoveName(name); this}

  def act() {
    loop {
      react {
        // Whenever woken up, reloads all books.
        case Timer.WakeUp => Book.load(Book.ScienceFictionFantasy, this)

        // For all new books, makes sure that:
        // - their author is matched by at least one of the registered ones.
        // - we don't already know about them.
        case book: Book if book.isPublished => matcher ! Associate(book.author, book.title, notifier, book)
      }
    }
  }
}

/** Defines various constants used by [[com.nrinaudo.eshitsuji.AuthorMonitor]]. */
object AuthorMonitor {
  /** Name of the configuration variable that contains the number of seconds between two refreshes. */
  val RefreshRateKey     = "amazon.refresh"
  /** Default number of seconds between two refreshes. */
  val DefaultRefreshRate = 60 * 60
}
