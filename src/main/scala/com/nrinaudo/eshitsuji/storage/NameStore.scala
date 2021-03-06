package com.nrinaudo.eshitsuji.storage

import com.mongodb.casbah.Imports._
import scala.util.control.Exception._

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

  def +=(elem: String) = {add(elem); this}

  def -=(elem: String) = {remove(elem); this}

  override def add(name: String): Boolean =
    catching(classOf[com.mongodb.MongoException.DuplicateKey]).withApply {_ => false} {
      col.insert(MongoDBObject("_id" -> name.toLowerCase, "val" -> MongoDBList()))
      true
    }

  override def remove(name: String): Boolean = col.remove("_id" $eq name.toLowerCase).getN > 0



  // - Name associations -----------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Associates the specified value with the specified name.
    *
    * @param  name  name with which to associate `value`.
    * @param  value value to associate with `name`.
    * @param  date  date at which the association is created. Defaults to now.
    * @return       `true` if the association is new, `false` otherwise.
    */
  def associate(name: String, value: String, date: java.util.Date = new java.util.Date()): Boolean = {
    val v = value.toLowerCase
    val o = MongoDBObject("name" -> v, "date" -> date)

    col.update(("_id" $eq name.toLowerCase) ++ ("val.name" $nin MongoDBList(v)), $push("val" -> o)).getN > 0
  }

  def cleanOlderThan(date: java.util.Date) {
    col.update(MongoDBObject(), $pull("val" -> ("date" $lte date)), multi = true)
  }
}
