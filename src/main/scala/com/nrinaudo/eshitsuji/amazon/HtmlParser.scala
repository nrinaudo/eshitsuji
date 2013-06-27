package com.nrinaudo.eshitsuji.amazon

import scala.xml._
import org.xml.sax._

/** Used to parse and sanitize HTML documents.
  *
  * This class is a simple wrapper for [[http://sourceforge.net/projects/nekohtml/ Cyberneko]], and plugs into
  * the standard Scala XML library. For example:
  * {{{
  * XML.withSAXParser(new HtmlParser()).load(new java.net.URL(uri))
  * }}}
  *
  * @author Nicolas Rinaudo
  */
class HtmlParser extends SAXParser {
  // This is actually an instance of XMLReader. One cannot help but wonder what the !@# they were thinking.
  val reader = new org.cyberneko.html.parsers.SAXParser

  // By default, CyberNeko turns all element names upper-case. I'm not a big fan.
  reader.setProperty("http://cyberneko.org/html/properties/names/elems", "lower")

  // Deprecated, no need to support.
  // This is going to generate warnings at compile time, but I don't see a way around it.
  override def getParser(): org.xml.sax.Parser = null

  override def getProperty(name: String): Object = reader.getProperty(name)

  override def getXMLReader(): XMLReader = reader

  override def isNamespaceAware(): Boolean = true

  override def isValidating(): Boolean = false

  override def setProperty(name: String, value: Object): Unit = reader.setProperty(name, value)
}
