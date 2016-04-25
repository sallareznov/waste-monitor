package models.entity

import play.api.libs.json.Json

/**
  * A user
  * @param id the id of the user
  * @param username the username
  * @param hash the hashed password
  */
case class User(id: Option[Long], username: String, hash: String)

object User {

  implicit val userReads = Json.reads[User]
  implicit val userWrites = Json.writes[User]
  implicit val userFormat = Json.format[User]

}
