package com.nrinaudo.eshitsuji.storage

import com.mongodb.casbah.Imports._

/** Used to store names and associated values.
  *
  * @param  storage where to store names and their associated values.
  * @param  name    name of the collection in which names and their associated values will be stored.
  * @author         Nicolas Rinaudo
  */
class NameStore(storage: Storage, name: String) extends Iterable[String] {
  /** MongoDB collection in which all names and associations are stored. */
  private val col = storage.collection(name)



  // - Name list maintenance -------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Adds the specified name to the store.
    *
    * @param  name name to add to the store (will be stored lower-cased)
    * @return      `true` if `name` was added to the store, `false` if it was already present.
    */
  def add(name: String): Boolean = {
    try {
      col.insert(MongoDBObject("_id" -> name.toLowerCase, "val" -> MongoDBList()))
      true
    }
    catch {
      case e: com.mongodb.MongoException.DuplicateKey => false
    }
  }

  /** Removes the specified name from the store.
    *
    * @param  name name to remove from the store.
    * @return      `true` if the name was removed from the store, `false` if it wasn't found.
    */
  def remove(name: String): Boolean = col.remove("_id" $eq name.toLowerCase).getN > 0

  /** Adds the specified name to the store.
    *
    * @param  name name to add to the store.
    * @return      the store itself.
    */
  def +=(name: String): NameStore = {
    add(name)
    this
  }

  /** Removes the specified name from the store.
    *
    * @param  name name to remove the store.
    * @return      the store itself.
    */
  def -=(name: String): NameStore = {
    remove(name)
    this
  }



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



  // - Iterable implementation -----------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  override def iterator(): Iterator[String] = col.find flatMap {_.getAs[String]("_id")}
}
