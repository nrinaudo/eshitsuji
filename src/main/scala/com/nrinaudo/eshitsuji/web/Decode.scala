package com.nrinaudo.eshitsuji.web

/** Used to decode URI segments.
  *
  * This is ripped in its entirety from
  * [[http://stackoverflow.com/questions/18083311/url-decoding-with-unfiltered/18086794?noredirect=1#18086794 this]]
  * stack overflow post by [[http://stackoverflow.com/users/2311148/cmbaxter cmbaxter]].
  */
object Decode {
  import java.net.URLDecoder.decode
  import java.nio.charset.Charset
  import scala.util.Try

  trait Extract {
    def charset: Charset
    def unapply(raw: String) = Try(decode(raw, charset.name())).toOption
  }

  object utf8 extends Extract {
    val charset = Charset.forName("utf8")
  }
}
