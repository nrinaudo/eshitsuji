package com.nrinaudo.eshitsuji.storage

import com.mongodb.casbah.Imports._

/** Used to wrap a MongoDB database.
  *
  * @author Nicolas Rinaudo
  */
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
  /** Name of the collection used to store configuration variables. */
  def Configuration = "Configuration"
  /** Default database name. */
  def DefaultDb     = "eShitsuji"
  /** Default MongoDB host. */
  def DefaultHost   = "mongodb://localhost:27017/"
  /** Default MongoDB connection URI. */
  def DefaultUri    = DefaultHost + DefaultDb

  /** Returns a valid MongoDB URI for the specified host and database. */
  def uri(host: String = DefaultHost, db: String = DefaultDb) = MongoClientURI(host + db)

  /** Creates a new instance of `Storage` connected to the specified MongoDB URI. */
  def apply(uri: String = DefaultUri): Storage = apply(MongoClientURI(uri))

  /** Creates a new instance of `Storage` connected to the specified MongoDB URI.
    *
    * @throws IllegalArgumentException if the specified URI isn't correct, doesn't contain a database name or contains
    *                                  credentials that are refused by the MongoDB host.
    */
  def apply(uri: com.mongodb.casbah.MongoClientURI): Storage = {
    uri.database map {name =>
      info("Connecting to MongoDB database %s..." format name)

      // Connects to the database, authenticate if we have both a user and a password.
      val db = MongoClient(uri)(name)
      for(user <- uri.username; password <- uri.password) {
        info("Authenticating as %s..." format user)
        if(!db.underlying.authenticate(user, password)) throw new IllegalArgumentException("Invalid user name or password")
      }

      new Storage(db)
    } getOrElse {
      throw new IllegalArgumentException("Missing database name")
    }
  }
}
