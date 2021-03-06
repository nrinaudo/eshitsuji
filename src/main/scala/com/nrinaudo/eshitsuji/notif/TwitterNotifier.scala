package com.nrinaudo.eshitsuji.notif

import com.nrinaudo.eshitsuji.storage._
import com.nrinaudo.eshitsuji.amazon.Book
import com.nrinaudo.eshitsuji.apple.IApp
import org.scribe.builder._
import org.scribe.builder.api._
import org.scribe.model._
import org.scribe.oauth._
import scala.actors.Actor
import scala.actors.Actor._
import grizzled.slf4j.Logging

/** Listens to e-Shitsuji events and tweets them.
  *
  * The companion object offers methods for retrieving Twitter configuration through instances of
  * [[com.nrinaudo.eshitsuji.storage.Configuration]].
  *
  * @param  service how to connect to twitter.
  * @param  token   twitter authentification.
  * @author         Nicolas Rinaudo
  */
class TwitterNotifier(private val service: OAuthService, private val token: Token) extends Actor with Logging {
  info("Twitter notification enabled.")

  def act() {
    loop {
      react {
        case book: Book =>
          debug("Received book %s" format book)
          notify("New book by %s: %s" format(book.author, book.uri))

        case app: IApp =>
          debug("Received iapp %s" format app)
          notify("New app by %s: %s" format(app.publisher, app.uri))
      }
    }
  }

  /** Tweets the specified message. */
  private def notify(msg: String) {
    val request = new OAuthRequest(Verb.POST, "https://api.twitter.com/1.1/statuses/update.json")

    request.addBodyParameter("status", msg)
    service.signRequest(token, request)

    try {request.send()}
    catch {case e: Exception => warn("Failed to send Twitter notification: %s" format e.getMessage, e)}
  }
}

/** Provides helper methods for extracting configuration values from a [[com.nrinaudo.eshitsuji.storage.Storage]]
  * instance.
  */
object TwitterNotifier {
  /** Name of the configuration variable that contains the Twitter service key to use. */
  val ServiceKey    = "twitter.service.key"
  /** Name of the configuration variable that contains the Twitter service secret to use. */
  val ServiceSecret = "twitter.service.secret"
  /** Name of the configuration variable that contains the Twitter token to use. */
  val TokenValue    = "twitter.token.value"
  /** Name of the configuration variable that contains the Twitter token secret to use. */
  val TokenSecret   = "twitter.token.secret"

  def apply(conf: Configuration): Option[TwitterNotifier] =
    for(s <- service(conf); t <- token(conf)) yield new TwitterNotifier(s, t)

  def service(conf: Configuration): Option[OAuthService] =
    for(k <- conf.get(ServiceKey); s <- conf.get(ServiceSecret)) yield service(k, s)

  def token(conf: Configuration): Option[Token] =
    for(v <- conf.get(TokenValue); s <- conf.get(TokenSecret)) yield token(v, s)

  /** Creates an instance of `Token` for the specified value and secret. */
  def token(token: String, secret: String): Token = new Token(token, secret)

  /** Creates an instance of `OAuthService` for the specified key and secret. */
  def service(key: String, secret: String): OAuthService =
    new ServiceBuilder().provider(classOf[TwitterApi]).apiKey(key).apiSecret(secret).build
}
