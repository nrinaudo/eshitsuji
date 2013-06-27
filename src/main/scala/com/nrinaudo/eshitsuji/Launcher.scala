package com.nrinaudo.eshitsuji

import com.nrinaudo.eshitsuji.storage.Storage

object Launcher extends App {
  case class Config(
    data:       String = "eshitsuji.db",
    authors:    List[String] = List.empty,
    publishers: List[String] = List.empty,
    help:       Boolean = false,
    start:      Boolean = false,
    conf:       Map[String, String] = Map())


  val parser = new scopt.immutable.OptionParser[Config]("eShitsuji", "1.0") {
    def options = Seq(
      // SQLite data file.
      opt("d", "data",  "Use file <value> for storage. Defaults to eshitsuji.db") {(d, c) => c.copy(data = d)},

      // Help.
      flag("h", "help", "Display this message and exit") {c => c.copy(help = true)},

      flag("s", "start", "Start monitoring (daemon mode)") {c => c.copy(start = true)},

      // Configuration value.
      keyValueOpt("s", "set", "<property>", "<value>", "Set and persist configuration entry <property> to <value>") {
        (k, v, c) => c.copy(conf = c.conf + (k -> v))
      },

      // Author search.
      opt("a", "author", "Monitor Amazon for new releases of author <value>") {(a, c) => c.copy(authors = a :: c.authors)},

      // AppStore search.
      opt("i", "iapp", "Monitor the Apple AppStore for releases by publisher <value>") {(i, c) => c.copy(publishers = i :: c.publishers)}

    )
  }

  parser.parse(args, Config()) map {config =>
    val notifier = new notif.StdoutNotifier()
    val storage  = Storage(new java.io.File(config.data))

    // Initialises configuration if necessary.
    config.conf foreach {case (key, value) => storage(key) = value}

    // Initialises app monitoring
    val publishers = new apple.PublisherMonitor(java.util.Locale.FRENCH, storage, notifier)
    println("Monitoring app releases by publishers:")
    config.publishers foreach {publishers += _}
    publishers foreach {publisher => println("- %s".format(publisher))}

    // Initialises books monitoring
    val authors = new amazon.AuthorMonitor(storage, notifier)
    config.authors foreach {authors += _}
    println("Monitoring book releases by authors:")
    authors foreach {author => println("- %s".format(author))}

    //if(config.start) {
      notifier.start()
      authors.start()
      //publishers.start()
    //}
  }
}
