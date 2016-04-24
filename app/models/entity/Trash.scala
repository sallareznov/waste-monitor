package models.entity

import play.api.libs.json.Json

case class Trash(id: Option[Long], userId: Long, volume: Int, empty: Boolean)

object Trash {

  implicit val trashReads = Json.reads[Trash]
  implicit val trashWrites = Json.writes[Trash]
  implicit val trashFormat = Json.format[Trash]

}
