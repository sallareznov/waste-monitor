package models.repository

import javax.inject.{Inject, Singleton}

import models.entity.Trash
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Repository for the TRASH table
  * @param dbConfigProvider the config provider
  * @param ec the execution context
  */
@Singleton
class TrashRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  /**
    * The TRASH table entity
    * @param tag the tag of the table
    */
  class TrashTable(tag: Tag) extends Table[Trash](tag, "TRASH") {

    def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("USER_ID")
    def volume = column[Int]("VOLUME")
    def empty = column[Boolean]("EMPTY")
    override def * = (id, userId, volume, empty) <> ((Trash.apply _).tupled, Trash.unapply)

  }

  val trashes = TableQuery[TrashTable]

  /**
    * lists the trashes owned by a user
    * @param userId the id of the user
    * @return the list of trashes owned by the user, encapsulated in a [[scala.concurrent.Future]] object
    */
  def listTrashes(userId: Long): Future[Seq[Trash]] = {
    dbConfig.db.run(trashes.filter(_.userId === userId).result)
  }

  /**
    * inserts a new trash
    * @param userId the id of the user
    * @param volume the volume of the trash
    * @return the inserted trash encapsulated in a [[scala.concurrent.Future]] object
    */
  def createTrash(userId: Long, volume: Int): Future[Trash] = {
    dbConfig.db.run {
      (trashes.map(trash => (trash.userId, trash.volume))
        returning trashes.map(tr => (tr.id, tr.empty))
        into((params, gen) => Trash(gen._1, params._1, params._2, gen._2))
        ) += (userId, volume)
    }
  }

  /**
    * retrieves a trash by its id
    * @param trashId the id of the trash
    * @return the trash option encapsulated in a [[Future]] object
    */
  def getTrash(trashId: Long): Future[Option[Trash]] = {
    dbConfig.db.run(trashes.filter(_.id === trashId).result.headOption)
  }

  /**
    * deletes a trash
    * @param trashId the id of the trash
    * @return encapsulated in a [[Future]] object, <code>1</code> if one row has been deleted, <code>0</code> otherwise
    */
  def deleteTrash(trashId: Long): Future[Boolean] = {
    dbConfig.db.run(trashes.filter(_.id === trashId).delete.map(result => result != 0))
  }

  /**
    * retrieves the trashes owned by a user
    * @param userId the id of the user
    * @return the trashes owned by a user, encapsulated in a [[Future]] object
    */
  def getTrashesForUserId(userId: Long): Future[Seq[Trash]] = {
    dbConfig.db.run(trashes.filter(_.userId === userId).result)
  }

  /**
    * changes the emptiness of a trash
    * @param trashId the id of the trash
    * @param empty <code>true</code> the trash will be emptied, <code>false</code> if it will be filled
    * @return encapsulated in a [[Future]] object, <code>1</code> if one row has been updated, <code>0</code> otherwise
    */
  def changeEmptiness(trashId: Long, empty: Boolean): Future[Int] = {
    dbConfig.db.run(trashes.filter(_.id === trashId).map(_.empty).update(empty))
  }

  /**
    * gets the total waste volume of a user
    * @param userId the id of the user
    * @return the total waste volume of a user encapsulated in a [[Future]] object
    */
  def getTotalWasteVolume(userId: Long): Future[Option[Int]] = {
    dbConfig.db.run(trashes.filter(trash => trash.userId === userId && !trash.empty).map(_.volume).sum.result)
  }

}
