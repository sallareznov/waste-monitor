package models

import java.sql.Date

import play.api.libs.json.Json

case class Trash(id: Option[Long], userId: Long, volume: Int, dumpDate: Date)

object Trash {

  implicit val trashReads = Json.reads[Trash]
  implicit val trashWrites = Json.writes[Trash]
  implicit val trashFormat = Json.format[Trash]

}
