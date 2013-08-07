package com.nrinaudo.eshitsuji.web

import com.nrinaudo.eshitsuji.storage.Configuration
import unfiltered.request._
import unfiltered.response._
import unfiltered.Cycle
import com.mongodb.casbah.Imports._
import org.apache.commons.codec.binary.Base64._
import java.security.MessageDigest

class Authentifier(conf: Configuration) {
  import Authentifier._

  // Makes sure we have a default password.
  conf.get(PasswordConf) getOrElse setPassword(DefaultPassword)

  // Since we're only keeping track of a single password, there really isn't any point in salting it.
  private def hash(pwd: String) = encodeBase64String(MessageDigest.getInstance("SHA-256").digest(pwd.getBytes("UTF-8")))

  def setPassword(pass: String) {
    conf(PasswordConf) = hash(pass)
  }

  def accept(user: String, pwd: String) = user == AdminUser && hash(pwd) == conf(PasswordConf)

  def apply[A, B](intent: Cycle.Intent[A, B]) = Cycle.Intent[A,B] {
    // We have acceptable tokens.
    case req @ BasicAuth(user, pass) if(accept(user, pass)) => Cycle.Intent.complete(intent)(req)

    // No access token, or not acceptable.
    case _  => Unauthorized ~> WWWAuthenticate("""Basic realm="/"""")
  }
}

object Authentifier {
  val PasswordConf    = "admin.pwd"
  val AdminUser       = "admin"
  val DefaultPassword = "EydeygIzIo"

  def apply[A, B](pwd: String)(intent: Cycle.Intent[A, B]) = Cycle.Intent[A,B] {
    // We have acceptable tokens.
    case req @ BasicAuth(user, pass) if(user == "admin" && pass == pwd) => Cycle.Intent.complete(intent)(req)

    // No access token, or not acceptable.
    case _  => Unauthorized ~> WWWAuthenticate("""Basic realm="/"""")
  }
}
