package com.nrinaudo.eshitsuji.storage

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, BeforeAndAfter}

/** Tests the [[com.nrinaudo.eshitsuji.storage.Storage]] class.
  *
  * @author Nicolas Rinaudo
  */
class StorageSpec extends FunSpec with BeforeAndAfter with ShouldMatchers {
  var storage: Storage = _

  before {
    storage = Storage.memory()
  }

  after {
    storage.close
  }

  describe("a storage") {
    it("creates new entries when requested") {
      storage("key") = "value"
      storage("key") should be (Some("value"))
    }

    it("gets properties directly through get") {
      storage("key") = "value"
      storage.get("key") should be ("value")
    }

    it("returns None when apply is called on a non-existent key") {
      storage("key") should be (None)
    }

    it("throws an exception when get is called on a non-existent key") {
      intercept[NoSuchElementException] {storage.get("key")}
    }

    it("overwrites existing values") {
      storage("key") = "value"
      storage("key") should be (Some("value"))

      storage("key") = "newValue"
      storage("key") should be (Some("newValue"))
    }
  }
}
