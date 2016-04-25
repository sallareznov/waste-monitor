package models.message

import play.api.libs.json.Json

/**
  * A JSON error message
  * @param message the message
  * @param documentation the documentation (links)
  */
case class ErrorJSONMessage(message: String, documentation: Array[String] = Array("https://github.com/sallareznov/waste-monitor", "/api"))

object ErrorJSONMessage {

  implicit val errorJSONMessageReads = Json.reads[ErrorJSONMessage]
  implicit val errorJSONMessageWrites = Json.writes[ErrorJSONMessage]
  implicit val errorJSONMessageFormat = Json.format[ErrorJSONMessage]

}
