package models.entity

import java.sql.Date

import play.api.libs.json.Json

/**
  * A waste volume
  * @param userId the id of the user
  * @param volume the volume of the waste
  * @param recordDate the date of the record
  */
case class WasteVolume(userId: Long, volume: Int, recordDate: Option[Date])

object WasteVolume {

  implicit val wasteVolumeReads = Json.reads[WasteVolume]
  implicit val wasteVolumeWrites = Json.writes[WasteVolume]
  implicit val wasteVolumeFormat = Json.format[WasteVolume]

}