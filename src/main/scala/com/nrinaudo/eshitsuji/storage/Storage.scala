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
  def DefaultDb     = "eShitsuji"
  def DefaultHost   = "mongodb://localhost:27017/"
  def DefaultUri    = DefaultHost + DefaultDb

  def uri(host: String = DefaultHost, db: String = DefaultDb) = MongoClientURI(host + db)

  def apply(uri: String = DefaultUri): Storage = apply(MongoClientURI(uri))

  def apply(uri: com.mongodb.casbah.MongoClientURI): Storage = {
    uri.database map {dbName =>
      info("Connecting to MongoDB database %s..." format dbName)

      val db = MongoClient(uri)(dbName)

      uri.username.map {user =>
        uri.password.map {password =>
          db.underlying.authenticate(user, password)
        }
      }
      new Storage(db)
    } getOrElse {throw new IllegalArgumentException("Missing database name")}
  }
}
