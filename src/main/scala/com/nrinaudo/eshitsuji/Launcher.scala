package com.nrinaudo.eshitsuji

import com.nrinaudo.eshitsuji.storage.Storage

object Launcher extends App with grizzled.slf4j.Logging {
  info("Starting up...")

  val storage  = Storage()
  val notifier = notif.TwitterNotifier(storage.conf) getOrElse {
    warn("Twitter notification unavailable, defaulting to logging.")
    new notif.LogNotifier()
  }

  val authors    = new amazon.AuthorMonitor(storage, notifier)
  val publishers = new apple.PublisherMonitor(java.util.Locale.FRENCH, storage, notifier)

  val plan = new web.AdminPlan(storage)
  plan.register("amazon", authors)
  plan.register("apple",  publishers)

  info("Starting monitors...")
  notifier.start()
  authors.start()
  publishers.start()

  info("Starting web service...")
  unfiltered.jetty.Http.local(8080).filter(plan).run()
}
