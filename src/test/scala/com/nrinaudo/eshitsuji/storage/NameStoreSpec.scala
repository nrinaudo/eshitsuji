package com.nrinaudo.eshitsuji.storage

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, BeforeAndAfter}

/** Tests the [[com.nrinaudo.eshitsuji.storage.NameStore]] class.
  *
  * @author Nicolas Rinaudo
  */
class NameStoreSpec extends FunSpec with ShouldMatchers with BeforeAndAfter {
  var store: NameStore = _

  before {
    store = new NameStore(Storage.memory(), "test")
  }

  after {
    store.close
  }

  describe("a NameStore") {
    it("has a size equal to the number of elements it contains") {
      store should have size(0)

      for(i <- 0 to 10) {
        store += "name" + i
        store should have size (i + 1)
      }

      for(i <- 10 to 0 by -1) {
        store -= "name" + i
        store should have size (i)
      }
    }

    it("contains all unique names added to it") {
      for(i <- 0 to 10) {
        val name = "name" + i

        store += name
        store should contain (name)
      }
    }

    it("forgets names when they are removed") {
      for(i <- 0 to 10) {
        store += "name" + i
      }

      for(i <- 10 to 0 by -1) {
        val name = "name" + i

        store should contain (name)
        store -= name
        store should not contain (name)
      }
    }

    it("silently ignores duplicate name addition") {
      store += "nicolas"
      store += "nicolas"

      store should have size (1)
    }

    it("silently ignores removal of unknown names") {
      store -= "nicolas"
    }

    it("refuse calls after it has been closed") {
      store.close()

      intercept[NullPointerException] {store += "nicolas"}
      intercept[NullPointerException] {store -= "nicolas"}
      intercept[NullPointerException] {store.size}
    }
  }
}
