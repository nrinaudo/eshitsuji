package com.nrinaudo.eshitsuji.storage

import com.mongodb.casbah.Imports._

/** Used to persist configuration values and connector specific information in an SQLite database.
  *
  * @author Nicolas Rinaudo
  */
class Storage private (private val db: MongoDB) {
  import Storage._

  private val conf = db(Configuration)


  def collection(name: String): MongoCollection = db(name)


  // - Configuration values --------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Retrieves the requested configuration value.
    *
    * @throws NoSuchElementException if the requested configuration value does not exist.
    */
  def get(k: String): String = apply(k).getOrElse {
    throw new NoSuchElementException("Missing %s configuration key".format(k))
  }

  /** Retrieves the requested configuration option. */
  def apply(k: String): Option[String] = {
    conf.findOneByID(k) flatMap {_.getAs[String]("value")}
  }

  /** Sets the specified configuration key to the specified value, overriding any previous value. */
  def update(k: String, v: String) {
    conf.save(MongoDBObject("_id" -> k, "value" -> v))
  }

  /** Removes the specified key. */
  def -=(k: String) {
    conf.remove("_id" $eq k)
  }

  def keys(): Iterator[String] = conf.find() flatMap {_.getAs[String]("_id")}
}


/** Used to create instances of `Storage`. */
object Storage {
  def Configuration = "Configuration"

  def apply(host: String = "localhost", post: Int = 27017, db: String = "eShitsuji"): Storage = new Storage(MongoClient(host, post)(db))
}
