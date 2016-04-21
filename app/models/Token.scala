package models

import java.sql.Date

import play.api.libs.json.Json

case class Token(text: String, username: String, expirationDelay: Option[Date])

object Token {

  implicit val tokenReads = Json.reads[Token]
  implicit val tokenWrites = Json.writes[Token]
  implicit val tokenFormat = Json.format[Token]

}
