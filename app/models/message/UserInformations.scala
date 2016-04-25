package models.message

import play.api.libs.json.Json

/**
  * Informations about a user
  * @param username the username
  * @param token the access token
  * @param nbTrashes the number of trashes owned by the user
  * @param totalWasteVolume the total waste volume of the user
  */
case class UserInformations(username: String, token: String, nbTrashes: Int, totalWasteVolume: Int)

object UserInformations {

  implicit val userJSONResponseReads = Json.reads[UserInformations]
  implicit val userJSONResponseWrites = Json.writes[UserInformations]
  implicit val userJSONResponseFormat = Json.format[UserInformations]

}
