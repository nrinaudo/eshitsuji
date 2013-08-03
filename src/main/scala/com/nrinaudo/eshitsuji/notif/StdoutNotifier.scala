package com.nrinaudo.eshitsuji.notif

import com.nrinaudo.eshitsuji.amazon.Book
import com.nrinaudo.eshitsuji.apple.IApp
import scala.actors.Actor
import scala.actors.Actor._

/** Simple notifier that will print e-Shitsuji events to stdout.
  *
  * @author Nicolas Rinaudo
  */
class StdoutNotifier extends Actor {
  def act() {
    loop {
      react {
        case book: Book => println("New book: %s".format(book))
        case app:  IApp  => println("New app: %s".format(app))
      }
    }
  }
}
