package models.entity

import java.sql.Date

import play.api.libs.json.Json

/**
  * A token
  * @param text the text of the token
  * @param userId the id of the user who owns the token
  * @param expirationDelay the expiration delay of the token
  */
case class Token(text: String, userId: Long, expirationDelay: Option[Date])

object Token {

  implicit val tokenFormat = Json.format[Token]

}
