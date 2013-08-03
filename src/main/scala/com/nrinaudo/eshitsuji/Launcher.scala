package com.nrinaudo.eshitsuji

import com.nrinaudo.eshitsuji.storage.Storage

object Launcher extends App {
  // Initialises storage and notification
  val storage  = Storage()
  val notifier = notif.TwitterNotifier(storage.conf) getOrElse new notif.StdoutNotifier()

  // Initialises monitors.
  val authors    = new amazon.AuthorMonitor(storage, notifier)
  val publishers = new apple.PublisherMonitor(java.util.Locale.FRENCH, storage, notifier)

  // Initialises web UI
  val plan = new web.AdminPlan(storage)
  plan.register("amazon", authors)
  plan.register("apple",  publishers)

  // Starts all services.
  notifier.start()
  authors.start()
  publishers.start()
  unfiltered.jetty.Http.local(8080).filter(plan).run()
}
