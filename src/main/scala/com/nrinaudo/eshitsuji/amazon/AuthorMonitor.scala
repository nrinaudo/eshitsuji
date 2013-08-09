package com.nrinaudo.eshitsuji.amazon

import com.nrinaudo.eshitsuji.storage._
import com.nrinaudo.eshitsuji.monitor._
import com.nrinaudo.eshitsuji.monitor.NameMatcher._
import com.nrinaudo.eshitsuji._
import scala.actors._
import scala.actors.Actor._
import scala.util.Try

/** Used to monitor authors on [[http://www.amazon.com Amazon]].
  *
  * @param  db       where to persist data and read configuration values.
  * @param  notifier where to send notifications whenever a new book by a monitored author is discovered.
  * @author          Nicolas Rinaudo
  */
class AuthorMonitor(db: Storage, notifier: Actor)
    extends NameMonitor(new NameMatcher(db.nameStore("Amazon"), AuthorMonitor.CacheTtl), notifier)
    with grizzled.slf4j.Logging {

  val refreshRate = {
    for(s <- db.conf.get(AuthorMonitor.RefreshRateKey);
        i <- Try {s.toInt}.toOption) yield i
  } getOrElse AuthorMonitor.DefaultRefreshRate

  info("Amazon configured to be refreshed every %,d seconds" format refreshRate)

  // - Actor implementation --------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def act() {
    loop {
      react {
        // Whenever woken up, reloads all books.
        case Timer.WakeUp =>
          info("Refreshing authors...")
          Book.load(Book.ScienceFictionFantasy, this)

        // For all new books, makes sure that:
        // - their author is matched by at least one of the registered ones.
        // - we don't already know about them.
        case book: Book if book.isPublished => associate(book.author, book.title, book)
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
  /** Number of milliseconds a cached association is allowed to live (15 days). */
  val CacheTtl = 1296000000l
}
