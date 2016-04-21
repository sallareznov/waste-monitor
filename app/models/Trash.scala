package models

import play.api.libs.json.Json

case class Trash(id: Option[Long], userId: Long, volume: Int, dumpFrequency: Int)

object Trash {

  implicit val trashReads = Json.reads[Trash]
  implicit val trashWrites = Json.writes[Trash]
  implicit val trashFormat = Json.format[Trash]

}
