package com.nrinaudo.eshitsuji.notif

import com.nrinaudo.eshitsuji.amazon.Book
import com.nrinaudo.eshitsuji.apple.IApp
import scala.actors.Actor
import scala.actors.Actor._

class LogNotifier extends Actor with grizzled.slf4j.Logging {
  def act() {
    loop {
      react {
        case book: Book => info("New book: %s" format book)
        case app:  IApp => info("New app: %s" format app)
      }
    }
  }
}
