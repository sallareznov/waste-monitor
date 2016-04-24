package models.repository

import java.sql.Date
import javax.inject.{Inject, Singleton}

import models.entity.WasteVolume
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WasteVolumeRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  class WasteVolumeTable(tag: Tag) extends Table[WasteVolume](tag, "WASTE_VOLUME") {

    def userId = column[Long]("USER_ID")
    def volume = column[Int]("VOLUME")
    def recordDate = column[Option[Date]]("RECORD_DATE")
    override def * = (userId, volume, recordDate) <>((WasteVolume.apply _).tupled, WasteVolume.unapply)

  }

  val wasteVolumes = TableQuery[WasteVolumeTable]

  def record(userId: Long, volume: Int): Future[WasteVolume] = {
    dbConfig.db.run {
      (wasteVolumes.map(wasteVolume => (wasteVolume.userId, wasteVolume.volume))
        returning wasteVolumes.map(_.recordDate)
        into ((params, recordDate) => WasteVolume(params._1, params._2, recordDate))
        ) +=(userId, volume)
    }
  }

  def getWasteVolumesFromUser(userId: Long): Future[Seq[WasteVolume]] = {
    dbConfig.db.run(wasteVolumes.filter(_.userId === userId).sortBy(_.recordDate).result)
  }

}
