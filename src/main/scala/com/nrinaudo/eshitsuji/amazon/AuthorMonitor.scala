package com.nrinaudo.eshitsuji.amazon

import com.nrinaudo.eshitsuji.storage._
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
  * @param  db       where to persist monitored authors and their known books.
  * @param  notifier where to send notifications whenever a new book by a monitored author is discovered.
  * @author          Nicolas Rinaudo
  */
class AuthorMonitor(private val db: Storage, private val notifier: Actor) extends
    NameStore(db, AuthorMonitor.AuthorsTable) with Actor {
  // - Initialisation --------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  // Makes sure the books table exists.
  db.update("""|create table if not exists %s
               |(id integer primary key, title text unique not null,author_id integer,
               |foreign key (author_id) references book_authors(id))""".stripMargin.format(AuthorMonitor.BooksTable))



  // - NameStore implementation ----------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  private def update() = this ! 'Update

  override def +=(name: String): AuthorMonitor = {
    super.+=(name)
    update()
    this
  }

  override def -=(name: String): AuthorMonitor = {
    super.-=(name)
    update()
    this
  }



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
    Timer(1000 * (db(RefreshRateKey) map {_.toInt} getOrElse DefaultRefreshRate), this)

    this
  }

  private def authorMatcher() = names map {(author) => author._1 -> ("^(?i).*" + author._2 + ".*").r.pattern}

  def act() {
    var authors = authorMatcher

    loop {
      react {
        // Whenever woken up, reloads all books.
        case Timer.WAKEUP => Book.load(Book.ScienceFictionFantasy, this)

        // When we receive the Update message, the author list has been updated. Reload it.
        // Note that we could probably be more granular and only add / delete the authors that have been modified,
        // but this happens so rarely that it's not worth the hassle.
        case 'Update => authors = authorMatcher

        // For all new books, makes sure that:
        // - their author is matched by at least one of the registered ones.
        // - we don't already know about them.
        case book: Book if book.isPublished =>
          authors find {author => author._2.matcher(book.author).matches} foreach {author =>
            notifyIfNotKnown(author._1, book)
          }
      }
    }
  }

  private def notifyIfNotKnown(author: Int, book: Book) = {
    if(db.prepare("insert or ignore into book_items (author_id, title) values (?, ?)").set(1, author)
      .set(2, book.title.toLowerCase).update() != 0) {notifier ! book}
  }
}

/** Defines various constants used by [[com.nrinaudo.eshitsuji.AuthorMonitor]]. */
object AuthorMonitor {
  /** Name of the table in which books are persisted. */
  val BooksTable         = "book_items"
  /** Name of the table in which monitored authors are persisted. */
  val AuthorsTable       = "book_authors"
  /** Name of the configuration variable that contains the number of seconds between two refreshes. */
  val RefreshRateKey     = "amazon.refresh"
  /** Default number of seconds between two refreshes. */
  val DefaultRefreshRate = 60 * 60
}
