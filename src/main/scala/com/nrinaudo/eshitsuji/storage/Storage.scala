package com.nrinaudo.eshitsuji.storage

import java.sql._

/** Used to persist configuration values and connector specific information in an SQLite database.
  *
  * @author Nicolas Rinaudo
  */
class Storage private (private val sql: Connection) {
  import Storage._

  // Makes sure the configuration table exists.
  update("create table if not exists configuration (key text primary key, value text not null)")



  // - Configuration values --------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Retrieves the requested configuration value.
    *
    * @throws NoSuchElementException if the requested configuration value does not exist.
    */
  def get(k: String): String = apply(k).getOrElse {throw new NoSuchElementException("Missing %s configuration key".format(k))}

  /** Retrieves the requested configuration option. */
  def apply(k: String): Option[String] = {
    prepare("select value from configuration where key=? limit 1").set(1, k).query {r =>
      if(r.next()) Some(r.getString(1))
      else         None
    }
  }

  /** Sets the specified configuration key to the specified value, overriding any previous value. */
  def update(k: String, v: String) {
    prepare("insert or replace into configuration (key, value) values (?, ?)").set(1, k)
      .set(2, v).update()
  }



  // - Querying --------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Prepares the specified statement. */
  def prepare(q: String): StatementBuilder = new StatementBuilder(sql.prepareStatement(q))

  /** Executes the specified update or create SQL query. */
  def update(u: String): Int = sql.createStatement.executeUpdate(u)

  /** Execute the specified select query.
    *
    * @param q query to execute.
    * @param f function that will handle the resulting result set.
    */
  def query[A](q: String)(f: (ResultSet => A)): A = {
    val r = sql.createStatement.executeQuery(q)
    try {f(r)}
    finally {r.close()}
  }


  // - Maintenance -----------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Closes this instance. */
  def close() = sql.close
}

/** Used to create instances of `Storage`. */
object Storage {
  Class.forName("org.sqlite.JDBC");

  def apply(file: java.io.File): Storage = new Storage(DriverManager.getConnection("jdbc:sqlite:%s".format(file.getPath)))

  def memory(): Storage = new Storage(DriverManager.getConnection("jdbc:sqlite::memory:"))
}



// - Statement handling ------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------
/** Convenience class for prepared statement handling. */
class StatementBuilder(private val s: PreparedStatement) {
  /** Sets the parameter at the specified index to the specified string.*
    *
    * @return `this`
    */
  def set(index: Int, str: String): StatementBuilder = {
    s.setString(index, str)
    this
  }

  /** Sets the parameter at the specified index to the specified integer.
    *
    * @return `this`
    */
  def set(index: Int, value: Int): StatementBuilder = {
    s.setInt(index, value)
    this
  }

  /** Executes the statement as an update or create query.
    *
    * @return the number of rows that were affected by the update.
    */
  def update(): Int = s.executeUpdate()

  /** Executes the statement as a select query.
    *
    * @param f function used to transform the query's result set into something usable.
    * @return  `f`'s return value, when applied to the query's result set.
    */
  def query[A](f: (ResultSet => A)): A = {
    val r =s.executeQuery()
    try {f(r)}
    finally {r.close()}
  }
}
