package models

import play.api.libs.json.Json

case class User(id: Long, username: String, password: String)

object User {

  implicit val userReads = Json.reads[User]
  implicit val userWrites = Json.writes[User]
  implicit val userFormat = Json.format[User]

}
