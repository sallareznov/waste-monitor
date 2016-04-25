package models.entity

import play.api.libs.json.Json

/**
  * A trash
  * @param id the id of the trash
  * @param userId the id of the user who owns the trash
  * @param volume the volume of the trash
  * @param empty <code>true</code> if the trash is empty, <code>false</code> if it is filled
  */
case class Trash(id: Option[Long], userId: Long, volume: Int, empty: Boolean)

object Trash {

  implicit val trashReads = Json.reads[Trash]
  implicit val trashWrites = Json.writes[Trash]
  implicit val trashFormat = Json.format[Trash]

}
