package com.nrinaudo.eshitsuji.web

import com.nrinaudo.eshitsuji.storage.Storage
import unfiltered.request._
import unfiltered.response._
import unfiltered.Cycle
import com.mongodb.casbah.Imports._

object Authentifier {
  val AdminUser = "admin"
  val DefaultPassword = "EydeygIzIo"

  def apply[A, B](pwd: String)(intent: Cycle.Intent[A, B]) = Cycle.Intent[A,B] {
    // We have acceptable tokens.
    case req @ BasicAuth(user, pass) if(user == "admin" && pass == pwd) => Cycle.Intent.complete(intent)(req)

    // No access token, or not acceptable.
    case _  => Unauthorized ~> WWWAuthenticate("""Basic realm="/"""")
  }
}
