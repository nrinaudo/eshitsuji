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
object Storage extends grizzled.slf4j.Logging {
  def Configuration = "Configuration"
  def DefaultUri    = "mongodb://localhost:27017"
  def DefaultDb     = "eShitsuji"

  def apply(uri: String = DefaultUri, db: String = DefaultDb): Storage = {
    info("Connecting to MongoDB database %s on url %s..." format(db, uri))
    new Storage(MongoClient(MongoClientURI(uri))(db))
  }
}
