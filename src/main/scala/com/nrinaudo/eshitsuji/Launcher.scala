package com.nrinaudo.eshitsuji

import com.nrinaudo.eshitsuji.storage.Storage

object Launcher extends App with grizzled.slf4j.Logging {
  info("Starting up...")

  /** Safe integer to string conversion function. */
  private def toInt(str: String) =
    try {Some(str.toInt)}
    catch {
      case e: Exception => None
    }

  case class Config(port: Int = 8080, mongo: String = Storage.DefaultUri)

  val parser = new scopt.OptionParser[Config]("eShitsuji") {
    opt[Int]('p', "port") action {(p, c) => c.copy(port = p)} text("port on which the webservice should listen.")
    opt[String]('m', "mongo") action {(u, c) => c.copy(mongo = u)} text("URI of the MongoDB instance to connect to")
  }

  parser.parse(args, Config()) map {config =>
    val storage  = Storage(config.mongo)
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

    info("Starting web service on port %d..." format config.port)
    unfiltered.jetty.Http.local(config.port).filter(plan).run()
  }
}
