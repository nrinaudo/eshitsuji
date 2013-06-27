package com.nrinaudo.eshitsuji.amazon

import scala.xml._
import scala.actors._
import scala.actors.Actor._
import java.util.Date

/**
  * Represents a book available on [[http://www.amazon.com Amazon]].
  *
  * The companion object offers methods for crawling Amazon and retrieving `Book` instances.
  *
  * @param  author name of the person who wrote the book.
  * @param  title  title of the book. This might not always be the complete title, depending on what Amazon serves.
  * @param  date   date at which the book was / will be published. This can sometimes be in the future.
  * @param  uri    URI at which the book can be found.
  * @author Nicolas Rinaudo
  */
class Book(val author: String, val title: String, val date: Date, val uri: String) extends Equals {
  /** Checks whether the book has already been published.
    *
    * @return `true` if the book is already published, `false` otherwise.
    */
  def isPublished(): Boolean = date.before(new Date())

  def canEqual(that: Any) = that.isInstanceOf[Book]

  override def toString(): String = "%s (by %s)".format(title, author)

  override def hashCode(): Int = (41 + author.hashCode) * 41 + title.hashCode

  override def equals(o: Any): Boolean = o match {
    case t: Book => t.canEqual(this) && t.author == author && t.title == title
    case _       => false
  }
}

/** Provides tools for crawling subsets of the Amazon store and load all the books they describe. */
object Book {
  /** Identifier of the Science Fiction / Fantasy category. */
  val ScienceFictionFantasy = 25

  /** Root URI of Amazon's new releases pages. */
  private val RootUri = "http://www.amazon.com/gp/new-releases/books/"



  // - Download helpers ------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** DOwnloads the content of the specified URI. */
  private def download(uri: String) = XML.withSAXParser(new HtmlParser()).load(new java.net.URL(uri))



  // - Book extraction -------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  private def parseDate(date: String) = {
    import java.text._

    val index = date.lastIndexOf(':')
    if(index == -1) throw new ParseException("Illegal date format: '%s'".format(date), 0)
    new SimpleDateFormat("MMM dd, yyyy") parse date.substring(index + 1).trim
  }

  /** Extracts a book from the specified nodes. */
  private def nodeToBook(node: NodeSeq) = {
    val title = node \\ "div" filter {_ \ "@class" contains Text("zg_title")}

    new Book((node \\ "div" filter {_ \ "@class" contains Text("zg_byline")}).text.trim.replaceAll("^by", "").trim,
      title.text.trim,
      parseDate((node \\ "div" filter {_ \ "@class" contains Text("zg_releaseDate")}).text),
      (title \\ "@href").text.trim)
  }

  /** Extracts all books declared in the specified page and passes them to the specified actor. */
  private def extractBooks(page: Elem, caller: Actor) =
    (page \\ "div" filter {_ \ "@class" contains Text("zg_itemWrapper")}) foreach {node =>
      try {caller ! nodeToBook(node)}
      catch {
        case e: java.text.ParseException =>
        // Nothing we can do here, except ignore the book.
        // Log, maybe?
      }
    }



  // - Page analysis ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Finds all sub-categories of the current category, downloads and analyses them. */
  private def loadCategories(html: Elem, caller: Actor) {
    ((html \\ "ul" filter {
      _ \ "li" \ "span" exists {_ \ "@class" contains Text("zg_selected")}
    }) \ "ul" \\ "a" map {_ \ "@href"}) foreach {link =>
      actor {
        load(download(link.text), caller)
      }
    }
  }

  /** Finds all 'next' page for the current page. */
  private def loadPages(html: Elem, caller: Actor) {
    (((html \\ "ol" filter {_ \ "@class" contains Text("zg_pagination")})
      \\ "li" drop (1)) \\ "a" map {_ \ "@href"}) foreach {link =>
      actor {
        extractBooks(download(link.text), caller)
      }
    }
  }

  private def load(html: Elem, caller: Actor) {
    loadCategories(html, caller)
    extractBooks(html, caller)
    loadPages(html, caller)
  }



  // - Category loading ------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Asynchronously loads all books in the specified category and sends them to the specified actor.
    *
    * @param category Amazon identifier of the category to crawl.
    * @param caller   actor to notify with `Book` events whenever a new book is found.
    */
  def load(category: Int, caller: Actor) {
    actor {
      load(download(RootUri + category), caller)
    }
  }
}
