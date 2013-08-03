package com.nrinaudo.eshitsuji.storage

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, BeforeAndAfter}

/** Tests the [[com.nrinaudo.eshitsuji.storage.NameStore]] class.
  *
  * @author Nicolas Rinaudo
  */
class NameStoreSpec extends FunSpec with ShouldMatchers with BeforeAndAfter {
  val store: NameStore = StorageSpec.testInstance().nameStore("NameStoreSpec")

  before {
    store foreach {store -= _}
  }

  describe("a NameStore") {
    it("has a size equal to the number of names it contains") {
      store should have size(0)

      for(i <- 0 to 10) {
        store.add("name" + i) should be(true)
        store should have size (i + 1)
      }

      for(i <- 10 to 0 by -1) {
        store.remove("name" + i) should be(true)
        store should have size (i)
      }
    }

    it("contains all unique names added to it") {
      for(i <- 0 to 10) {
        val name = "name" + i

        store.add(name) should be(true)
        store should contain (name)
      }
    }

    it("forgets names when they are removed") {
      for(i <- 0 to 10) {
        store.add("name" + i) should be(true)
      }

      for(i <- 10 to 0 by -1) {
        val name = "name" + i

        store should contain (name)
        store.remove(name) should be(true)
        store should not contain (name)
      }
    }

    it("silently ignores duplicate name addition") {
      store.add("nicolas") should be(true)
      store.add("nicolas") should be(false)

      store should have size (1)
    }

    it("silently ignores removal of unknown names") {
      store.remove("nicolas") should be(false)
    }

    it("associates new values with a given name") {
      store.add("nicolas") should be(true)
      store.associate("nicolas", "rinaudo") should be(true)
      store.associate("nicolas", "yamamoto") should be(true)
    }

    it("doesn't associate known values with a given name") {
      store.add("nicolas") should be(true)
      store.associate("nicolas", "rinaudo") should be(true)
      store.associate("nicolas", "rinaudo") should be(false)
    }
  }
}
