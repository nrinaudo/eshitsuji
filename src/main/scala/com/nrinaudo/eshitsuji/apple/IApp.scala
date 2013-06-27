package com.nrinaudo.eshitsuji.apple

import scala.xml._

/** Represents an application on the Apple Store.
  *
  * @param  name      name of the application.
  * @param  publisher publisher of the application.
  * @param  uri       URI of the application.
  * @author           Nicolas Rinaudo
  */
class IApp(val name: String, val publisher: String, val uri: String) extends Equals {
  override def toString(): String = "%s (by %s)".format(name, publisher)

  def canEqual(that: Any) = that.isInstanceOf[IApp]

  override def hashCode(): Int = publisher.hashCode * 31 + name.hashCode

  override def equals(o: Any): Boolean = o match {
    case t: IApp => t.canEqual(this) && t.publisher == publisher && t.name == name
    case _       => false
  }
}

/** Used to rerieve [[com.nrinaudo.eshitsuji.apple.IApp]] instances. */
object IApp {
  private def download(uri: String) = XML.load(new java.net.URL(uri))

  /** Extracts all app declarations found at the specified uri and passes them to the specified callback.
    *
    * @param uri      URI to download and parse.
    * @param callback function to call with each analyse instance of `IApp`.
    */
  def load(uri: String)(callback: IApp => Unit) {
    download(uri) \\ "entry" foreach {entry =>
      callback(new IApp((entry \\ "name").text.trim,
                       (entry \\ "artist").text.trim,
                       (entry \\ "link" \\ "@href").text.trim))
    }
  }
}
