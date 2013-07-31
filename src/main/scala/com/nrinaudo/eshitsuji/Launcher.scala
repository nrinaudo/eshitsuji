package com.nrinaudo.eshitsuji

import com.nrinaudo.eshitsuji.storage.Storage

object Launcher extends App {
  val notifier = new notif.StdoutNotifier()
  val storage  = Storage()

  val authors = new amazon.AuthorMonitor(storage, notifier)
  val publishers = new apple.PublisherMonitor(java.util.Locale.FRENCH, storage, notifier)

  authors += "Gaiman"

  notifier.start()
  authors.start()
  publishers.start()
}
