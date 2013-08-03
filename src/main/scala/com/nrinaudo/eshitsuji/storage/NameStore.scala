package com.nrinaudo.eshitsuji.storage

import com.mongodb.casbah.Imports._

/** Acts as a `Set` of names backed by a MongoDB collection.
  *
  * The purpose of this class is to keep a list of values associated with a given name. It's meant to act as a cache
  * for implementations such as the Amazon bookstore monitor, who regularly look up new books for a known set of
  * authors.
  *
  * @author Nicolas Rinaudo
  */
class NameStore(private val col: MongoCollection) extends collection.mutable.Set[String] {
  // - Set implementation ----------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def contains(key: String) = col.count("_id" $eq key.toLowerCase) == 1

  def iterator = col.find flatMap {_.getAs[String]("_id")}

  def +=(elem: String) = {
    add(elem)
    this
  }

  def -=(elem: String) = {
    remove(elem)
    this
  }

  override def add(name: String): Boolean = {
    try {
      col.insert(MongoDBObject("_id" -> name.toLowerCase, "val" -> MongoDBList()))
      true
    }
    catch {
      case e: com.mongodb.MongoException.DuplicateKey => false
    }
  }

  override def remove(name: String): Boolean = col.remove("_id" $eq name.toLowerCase).getN > 0



  // - Name associations -----------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Associates the specified value with the specified name.
    *
    * @param  name  name with which to associate `value`.
    * @param  value value to associate with `name`.
    * @return       `true` if the association is new, `false` otherwise.
    */
  def associate(name: String, value: String): Boolean = {
    val v = value.toLowerCase

    col.update(("_id" $eq name.toLowerCase) ++ ("val" $nin MongoDBList(v)), $push("val" -> v)).getN > 0
  }
}
