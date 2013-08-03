package com.nrinaudo.eshitsuji.storage

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{FunSpec, BeforeAndAfter}

/** Tests the [[com.nrinaudo.eshitsuji.storage.Storage]] class.
  *
  * @author Nicolas Rinaudo
  */
class StorageSpec extends FunSpec with BeforeAndAfter with ShouldMatchers {
}

object StorageSpec {
  def testInstance() = Storage(db = "eShitsujiTests")
}
