package com.nrinaudo.eshitsuji.storage

import com.mongodb.casbah.Imports._

/** Map of configuration (key, value) pairs backed by a MongoDB collection.
  *
  * @param  col collection in which to store the configuration pairs.
  * @author     Nicolas Rinaudo
  */
class Configuration(col: MongoCollection) extends collection.mutable.Map[String, String] {
  def +=(k: (String, String)) = {col.save(MongoDBObject("_id" -> k._1, "value" -> k._2)); this}

  def -=(key: String) = {col.remove("_id" $eq key); this}

  def get(key: String): Option[String] = col.findOneByID(key) flatMap {_.getAs[String]("value")}

  def iterator(): Iterator[(String, String)] =
    for(o     <- col.find;
        id    <- o.getAs[String]("_id");
        value <- o.getAs[String]("value")) yield (id, value)
}
