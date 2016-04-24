package models.entity

import java.sql.Date

import play.api.libs.json.Json

case class WasteVolume(userId: Long, volume: Int, recordDate: Option[Date])

object WasteVolume {

  implicit val wasteVolumeReads = Json.reads[WasteVolume]
  implicit val wasteVolumeWrites = Json.writes[WasteVolume]
  implicit val wasteVolumeFormat = Json.format[WasteVolume]

}