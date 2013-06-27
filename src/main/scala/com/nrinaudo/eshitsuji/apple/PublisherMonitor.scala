package com.nrinaudo.eshitsuji.apple

import com.nrinaudo.eshitsuji.storage._
import com.nrinaudo.eshitsuji._
import scala.actors._
import java.util.Locale

class PublisherMonitor(locale: Locale, db: Storage, val notifier: Actor) extends NameStore(db, "app_publishers")
    with Actor {
  private val uri = "https://itunes.apple.com/%s/rss/newapplications/limit=300/xml".format(locale.getCountry.toLowerCase)

  db.update("""|create table if not exists app_items
               |(id integer primary key, name text unique not null,publisher_id integer,
               |foreign key (publisher_id) references app_publishers(id))""".stripMargin)

  private def update() = this ! 'Update

  override def +=(name: String): PublisherMonitor = {
    super.+=(name)
    update()
    this
  }

  override def -=(name: String): PublisherMonitor = {
    super.-=(name)
    update()
    this
  }

  override def start(): PublisherMonitor = {
    import PublisherMonitor._

    super.start()
    Timer(1000 * (db(RefreshRate) map {_.toInt} getOrElse DefaultRefreshRate), this)

    this
  }


  private def publisherMatcher() = names map {(publisher) => publisher._1 -> ("^(?i).*" + publisher._2 + ".*").r.pattern}

  def act {
    var publishers = publisherMatcher
    loop {
      react {
        case Timer.WAKEUP =>
          IApp.load(uri) {app =>
            publishers find {publisher => publisher._2.matcher(app.publisher).matches} foreach {publisher =>
              if(db.prepare("insert or ignore into app_items (publisher_id, name) values (?, ?)").set(1, publisher._1)
                .set(2, app.name.toLowerCase).update() != 0) notifier ! app
            }
          }

        case 'Update => publishers = publisherMatcher
      }
    }
  }
}

object PublisherMonitor {
  val RefreshRate = "apple.refresh"
  val DefaultRefreshRate = 60 * 60
}
