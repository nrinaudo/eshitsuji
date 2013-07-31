package com.nrinaudo.eshitsuji.apple

import com.nrinaudo.eshitsuji.storage._
import com.nrinaudo.eshitsuji._
import com.nrinaudo.eshitsuji.monitor._
import com.nrinaudo.eshitsuji.monitor.NameMatcher._
import scala.actors._
import java.util.Locale

class PublisherMonitor(locale: Locale, private val db: Storage, private val notifier: Actor) extends Actor {
  private val matcher = new NameMatcher(new NameStore(db, "AppStore"))
  private val uri     = "https://itunes.apple.com/%s/rss/newapplications/limit=300/xml".format(locale.getCountry.toLowerCase)

  override def start(): PublisherMonitor = {
    import PublisherMonitor._

    super.start()
    matcher.start()
    Timer(1000 * (db(RefreshRate) map {_.toInt} getOrElse DefaultRefreshRate), this)

    this
  }

  def +=(name: String): PublisherMonitor = {matcher ! AddName(name); this}
  def -=(name: String): PublisherMonitor = {matcher ! RemoveName(name); this}

  def act {
    loop {
      react {
        case Timer.WakeUp => IApp.load(uri) {app => matcher ! Associate(app.publisher, app.name, notifier, app)}
      }
    }
  }
}

object PublisherMonitor {
  val RefreshRate = "apple.refresh"
  val DefaultRefreshRate = 60 * 60
}
