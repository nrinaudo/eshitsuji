package com.nrinaudo.eshitsuji.storage

import com.mongodb.casbah.Imports._

class Storage private (private val db: MongoDB, confName: String = Storage.Configuration) {
  import Storage._

  val conf = new Configuration(collection(confName))

  /** Returns a new instance of `NameStore` stored in the specified collection. */
  def nameStore(name: String) = new NameStore(collection(name))

  /** Returns the MongoDB collection with the specified name. */
  def collection(name: String): MongoCollection = db(name)
}


/** Used to create instances of `Storage`. */
object Storage {
  def Configuration = "Configuration"

  def apply(host: String = "localhost", post: Int = 27017, db: String = "eShitsuji"): Storage = new Storage(MongoClient(host, post)(db))
}
