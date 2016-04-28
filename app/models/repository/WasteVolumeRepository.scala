package models.repository

import java.sql.Date
import javax.inject.{Inject, Singleton}

import models.entity.WasteVolume
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Repository for the WASTE_VOLUME table
  * @param dbConfigProvider the config provider
  * @param ec the execution context
  */
@Singleton
class WasteVolumeRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  /**
    * the WASTE_VOLUME table entity
    * @param tag the tag of the table
    */
  class WasteVolumeTable(tag: Tag) extends Table[WasteVolume](tag, "WASTE_VOLUME") {

    def userId = column[Long]("USER_ID")
    def volume = column[Int]("VOLUME")
    def recordDate = column[Option[Date]]("RECORD_DATE")
    override def * = (userId, volume, recordDate) <>((WasteVolume.apply _).tupled, WasteVolume.unapply)

  }

  val wasteVolumes = TableQuery[WasteVolumeTable]

  /**
    * inserts a new record
    * @param userId the id of the user
    * @param volume his total waste volume
    * @return the inserted waste volume encapsulated in a [[scala.concurrent.Future]] object
    */
  def record(userId: Long, volume: Int): Future[WasteVolume] = {
    dbConfig.db.run {
      (wasteVolumes.map(wasteVolume => (wasteVolume.userId, wasteVolume.volume))
        returning wasteVolumes.map(_.recordDate)
        into ((params, recordDate) => WasteVolume(params._1, params._2, recordDate))
        ) +=(userId, volume)
    }
  }

  /**
    * retrieves all the waste volumes over time from a user
    * @param userId the id of the user
    * @return all the waste volumes over time from a user, encapsulated in a [[scala.concurrent.Future]] object
    */
  def getWasteVolumesFromUser(userId: Long): Future[Seq[WasteVolume]] = {
    dbConfig.db.run(wasteVolumes.filter(_.userId === userId).sortBy(_.recordDate).result)
  }

}
