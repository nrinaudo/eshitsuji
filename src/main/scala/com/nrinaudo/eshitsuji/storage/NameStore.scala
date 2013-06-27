package com.nrinaudo.eshitsuji.storage

/** Used to store lists of names in an SQLite database.
  *
  * @param  storage where to store names.
  * @param  table   name of the table in which to store names.
  * @author Nicolas Rinaudo
  */
class NameStore(private val storage: Storage, private val table: String) extends Iterable[String] {
  // Makes sure the name store exists.
  storage.update("create table if not exists %s (id integer primary key, name text unique not null)".format(table))



  // - Name list maintenance -------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Method called whenever the name list has been modified. */
  protected def updated() {}

  /** Adds the specified name to the list, if not already present.
    *
    * Names are stored in their lower-cased version.
    *
    * @param name name to store.
    */
  def +=(name: String): NameStore = {
    storage.prepare("insert or replace into %s (name) values (?)".format(table)).set(1, name.toLowerCase).update()
    updated()
    this
  }

  /** Removes the specified name from the list.
    *
    * This operation is case-insensitive.
    *
    * @param name name to remove from the list.
    */
  def -=(name: String): NameStore = {
    storage.prepare("delete from %s where name=?".format(table)).set(1, name.toLowerCase).update()
    updated()
    this
  }



  // - Maintenance -----------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Closes the underlying instance of [[com.nrinaudo.eshitsuji.storage.Storage]]. */
  def close() = storage.close



  // - Iterable implementation -----------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  override def toSet[B >: String]: Set[B] = (names map {_._2}).toSet

  override def iterator(): Iterator[String] = toSet.iterator

  /** Returns a `Map` that associates a name to its unique database identifier. */
  protected def names(): Map[Int, String] = {
    storage.query("select id, name from %s".format(table)) {r =>
      val l = Map.newBuilder[Int, String]
      while(r.next())
        l += (r.getInt(1) -> r.getString(2))
      l.result
    }
  }
}
