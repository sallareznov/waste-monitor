package models

import play.api.libs.json.Json

case class ErrorJSONMessage(message: String, documentation: String = "https://github.com/sallareznov/waste-monitor")

object ErrorJSONMessage {

  implicit val errorJSONMessageReads = Json.reads[ErrorJSONMessage]
  implicit val errorJSONMessageWrites = Json.writes[ErrorJSONMessage]
  implicit val errorJSONMessageFormat = Json.format[ErrorJSONMessage]

}