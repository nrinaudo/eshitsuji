package com.nrinaudo.eshitsuji.apple

import com.nrinaudo.eshitsuji.storage._
import com.nrinaudo.eshitsuji._
import com.nrinaudo.eshitsuji.monitor._
import com.nrinaudo.eshitsuji.monitor.NameMatcher._
import scala.actors._
import java.util.Locale
import scala.util.Try

class PublisherMonitor(locale: Locale, db: Storage, notifier: Actor)
    extends NameMonitor(new NameMatcher(db.nameStore("AppStore"), PublisherMonitor.CacheTtl), notifier)
    with grizzled.slf4j.Logging {

  private val uri = "https://itunes.apple.com/%s/rss/newapplications/limit=300/xml".format(locale.getCountry.toLowerCase)

  val refreshRate = {
    for(s <- db.conf.get(PublisherMonitor.RefreshRateKey);
        i <- Try {s.toInt}.toOption) yield i
  } getOrElse PublisherMonitor.DefaultRefreshRate

  info("AppStore configured to be refreshed every %,d seconds" format refreshRate)

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
  /** Number of milliseconds a cached association is allowed to live (7 days). */
  val CacheTtl = 604800000l
}
