package com.nrinaudo.eshitsuji.web

import com.nrinaudo.eshitsuji.storage.StorageSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, BeforeAndAfter}

/** Tests the [[com.nrinaudo.eshitsuji.web.Authentifier]] class.
  *
  * @author Nicolas Rinaudo
  */
class AuthentifierSpec extends FunSpec with BeforeAndAfter with ShouldMatchers {
  val auth = new Authentifier(StorageSpec.testInstance())

  // Makes sure we have a 'clean' instance before each test.
  before {
    auth withFilter {_ != Authentifier.AdminUser} foreach {auth -= _}
  }

  describe("a Configuration") {
    it("Accepts the default user") {
      auth.accept(Authentifier.AdminUser, Authentifier.DefaultPassword) should be(true)
      auth.accept(Authentifier.AdminUser, Authentifier.DefaultPassword + "zorglub") should be(false)
    }

    it("Creates new users and accepts their password") {
      auth.add("nrinaudo", "zorglub")

      auth.accept("nrinaudo", "zorglub") should be(true)
      auth.accept("nrinaudo", "bulgroz") should be(false)
    }

    it("Updates existing users' password, and accept the new password") {
      auth.add("nrinaudo", "zorglub")

      auth.accept("nrinaudo", "zorglub") should be(true)
      auth.accept("nrinaudo", "bulgroz") should be(false)

      auth("nrinaudo") = "bulgroz"

      auth.accept("nrinaudo", "zorglub") should be(false)
      auth.accept("nrinaudo", "bulgroz") should be(true)
    }

    it("Refuses a user's password after it has been deleted") {
      auth.add("nrinaudo", "zorglub")

      auth.accept("nrinaudo", "zorglub") should be(true)

      auth -= "nrinaudo"

      auth.accept("nrinaudo", "zorglub") should be(false)
    }

    it("Refuses to change a non-existing user's password") {
      (auth("nrinaudo") = "zorglub") should be(false)
    }

    it("Refuses to create an already existing user") {
      auth.add("nrinaudo", "zorglub") should be(true)
      auth.add("nrinaudo", "bulgroz") should be(false)
    }
  }
}
