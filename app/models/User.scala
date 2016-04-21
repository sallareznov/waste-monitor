package models

import play.api.libs.json.Json

case class User(id: Option[Long], username: String, hash: String)

object User {

  implicit val userReads = Json.reads[User]
  implicit val userWrites = Json.writes[User]
  implicit val userFormat = Json.format[User]

}
