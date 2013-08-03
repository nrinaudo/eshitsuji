package com.nrinaudo.eshitsuji.storage

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, BeforeAndAfter}

/** Tests the [[com.nrinaudo.eshitsuji.storage.Configuration]] class.
  *
  * @author Nicolas Rinaudo
  */
class ConfigurationSpec extends FunSpec with BeforeAndAfter with ShouldMatchers {
  val conf = StorageSpec.testInstance().conf

  // Makes sure we have a 'clean' instance before each test.
  before {
    conf.clear()
  }

  describe("a Configuration") {
    it("creates new entries when requested") {
      conf("key") = "value"
      conf.get("key") should be(Some("value"))
      conf("key") should be("value")
    }

    it("wraps properties in instances of Option through get") {
      conf("key") = "value"
      conf.get("key") should be(Some("value"))
    }

    it("gets properties directly through apply") {
      conf("key") = "value"
      conf("key") should be("value")
    }

    it("returns None when get is called on a non-existent key") {
      conf.get("key") should be(None)
    }

    it("throws an exception when apply is called on a non-existent key") {
      intercept[NoSuchElementException] {conf("key")}
    }

    it("overwrites existing values") {
      conf("key") = "value"
      conf("key") should be("value")

      conf("key") = "newValue"
      conf("key") should be("newValue")
    }
  }
}
