package models.entity

import play.api.libs.json.Json

case class UserInformations(username: String, token: String, nbTrashes: Int, totalWasteVolume: Int)

object UserInformations {

  implicit val userJSONResponseReads = Json.reads[UserInformations]
  implicit val userJSONResponseWrites = Json.writes[UserInformations]
  implicit val userJSONResponseFormat = Json.format[UserInformations]

}
