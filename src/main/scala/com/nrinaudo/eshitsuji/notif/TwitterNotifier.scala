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

/** Listens to eshitsuji events and tweets them.
  *
  * The companion object offers methods for retrieving Twitter configuration through instances of
  * [[com.nrinaudo.eshitsuji.storage.Storage]].
  *
  * @param  target  name of the twitter user to which messages will be sent.
  * @param  service how to connect to twitter.
  * @param  token   twitter authentification.
  * @author         Nicolas Rinaudo
  */
class TwitterNotifier(val target: String, private val service: OAuthService, private val token: Token) extends Actor {
  def act() {
    loop {
      react {
        case book: Book => notify("New book by %s: %s".format(book.author, book.uri))
        case app:  IApp  => notify("New app by %s: %s".format(app.publisher, app.uri))
      }
    }
  }

  /** Sends the specified message to `@target`. */
  private def notify(msg: String) {
    val request = new OAuthRequest(Verb.POST, "https://api.twitter.com/1.1/statuses/update.json")
    request.addBodyParameter("status", "@%s %s".format(target, msg))
    service.signRequest(token, request)
    request.send()
  }
}

/** Provides helper methods for extracting configuration values from a [[com.nrinaudo.eshitsuji.storage.Storage]]
  * instance.
  */
object TwitterNotifier {
  /** Name of the configuration variable that contains the name of the twitter user that will receive messages. */
  val TargetKey     = "twitter.target"
  /** Name of the configuration variable that contains the Twitter service key to use. */
  val ServiceKey    = "twitter.service.key"
  /** Name of the configuration variable that contains the Twitter service secret to use. */
  val ServiceSecret = "twitter.service.secret"
  /** Name of the configuration variable that contains the Twitter token to use. */
  val TokenValue    = "twitter.token.value"
  /** Name of the configuration variable that contains the Twitter token secret to use. */
  val TokenSecret   = "twitter.token.secret"

  /** Creates an instance of `TwitterNotifier` from configuration values found in the specified
    * [[com.nrinaudo.eshitsuji.storage.Storage]].
    */
  def apply(storage: Storage) = new TwitterNotifier(storage.get(TargetKey), service(storage), token(storage))

  /** Creates a new instance of `OAuthService` from configuration values found in the specified
    * [[com.nrinaudo.eshitsuji.storage.Storage]].
    */
  def service(storage: Storage): OAuthService = service(storage.get(ServiceKey), storage.get(ServiceSecret))

  /** Creates a new instance of `Token` from configuration values found in the specified
    * [[com.nrinaudo.eshitsuji.storage.Storage]].
    */
  def token(storage: Storage): Token = token(storage.get(TokenValue), storage.get(TokenSecret))

  /** Creates an instance of `Token` for the specified value and secret. */
  def token(token: String, secret: String): Token = new Token(token, secret
)
  /** Creates an instance of `OAuthService` for the specified key and secret. */
  def service(key: String, secret: String): OAuthService =
    new ServiceBuilder().provider(classOf[TwitterApi]).apiKey(key).apiSecret(secret).build
}
