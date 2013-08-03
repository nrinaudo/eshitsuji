package com.nrinaudo.eshitsuji.web

import com.nrinaudo.eshitsuji.storage.Storage
import unfiltered.request._
import unfiltered.response._
import unfiltered.Cycle
import com.mongodb.casbah.Imports._

class Authentifier(storage: Storage) extends Iterable[String] {
  import Authentifier._

  private val col = storage.collection("Users")

  // Creates the default user if it doesn't exist.
  col.findOneByID(AdminUser) getOrElse update(AdminUser, DefaultPassword)



  // - Salt & Base64 encoding / decoding -------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Generates a new random salt. */
  private def randomSalt() = {
    val data = new Array[Byte](15)
    new java.security.SecureRandom().nextBytes(data)
    data
  }

  /** Encodes the specified byte array in Base64. */
  private def encode(data: Array[Byte]) = org.apache.commons.codec.binary.Base64.encodeBase64String(data)

  /** Decodes the specified Base64 encoded string to a byte array. */
  private def decode(data: String) = org.apache.commons.codec.binary.Base64.decodeBase64(data)

  /** Hahes the specified password, salting it with the specified value. */
  private def hashPassword(salt: Array[Byte], pwd: String) = {
    val digest = java.security.MessageDigest.getInstance("SHA-256")

    digest.update(salt)
    digest.update(pwd.getBytes("UTF-8"))

    encode(digest.digest())
  }



  // - Iterable implementation -----------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def iterator: Iterator[String] = col.find flatMap {_.getAs[String]("_id")}



  // - User handling ---------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Checks whether the specified user / password is valid. */
  def accept(user: String, pass: String): Boolean = {
    col.findOneByID(user) flatMap {obj =>
      obj.getAs[String]("salt") flatMap {salt =>
        obj.getAs[String]("pwd") map {pwd =>
          hashPassword(decode(salt), pass) == pwd
        }
      }
    } getOrElse false
  }

  /** Creates or updates the specified user. */
  def update(user: String, pass: String) {
    val salt = randomSalt
    col.save(MongoDBObject("_id" -> user, "salt" -> encode(salt), "pwd" -> hashPassword(salt, pass)))
  }

  /** Deletes the specified user. */
  def -=(user: String): Boolean = col.remove("_id" $eq user).getN > 0



  // - Unfiltered kit --------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def apply[A, B](intent: Cycle.Intent[A, B]) = Cycle.Intent[A,B] {
    // We have acceptable tokens.
    case req @ BasicAuth(user, pass) if(accept(user, pass)) => Cycle.Intent.complete(intent)(req)

    // No access token, or not acceptable.
    case _  => Unauthorized ~> WWWAuthenticate("""Basic realm="/"""")
  }
}


object Authentifier {
  /** Administrator, must exist. */
  val AdminUser       = "admin"
  /** Default administrator password (should obviously be changed for live instances). */
  val DefaultPassword = "EydeygIzIo"
}
