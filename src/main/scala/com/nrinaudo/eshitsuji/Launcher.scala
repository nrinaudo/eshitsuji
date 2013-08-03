package com.nrinaudo.eshitsuji

import com.nrinaudo.eshitsuji.storage.Storage

object Launcher extends App {
  val notifier = new notif.StdoutNotifier()
  val storage  = Storage()

  val authors = new amazon.AuthorMonitor(storage, notifier)
  val publishers = new apple.PublisherMonitor(java.util.Locale.FRENCH, storage, notifier)

  val plan = new web.AdminPlan(storage)

  plan.register("amazon", authors)
  plan.register("apple",  publishers)

  notifier.start()
  authors.start()
  publishers.start()

  unfiltered.jetty.Http.local(8080).filter(plan).run()
}
