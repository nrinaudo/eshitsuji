package com.nrinaudo.eshitsuji.apple

import com.nrinaudo.eshitsuji.storage._
import com.nrinaudo.eshitsuji._
import com.nrinaudo.eshitsuji.monitor._
import com.nrinaudo.eshitsuji.monitor.NameMatcher._
import scala.actors._
import java.util.Locale

class PublisherMonitor(locale: Locale, db: Storage, notifier: Actor)
    extends NameMonitor(new NameMatcher(db.nameStore("AppStore")), notifier) {

  private val uri = "https://itunes.apple.com/%s/rss/newapplications/limit=300/xml".format(locale.getCountry.toLowerCase)

  val refreshRate = db.conf.get(PublisherMonitor.RefreshRateKey) map {_.toInt} getOrElse PublisherMonitor.DefaultRefreshRate

  def act {
    loop {
      react {
        case Timer.WakeUp => IApp.load(uri) {app => associate(app.publisher, app.name, app)}
      }
    }
  }
}

object PublisherMonitor {
  val RefreshRateKey = "apple.refresh"
  val DefaultRefreshRate = 60 * 60
}
