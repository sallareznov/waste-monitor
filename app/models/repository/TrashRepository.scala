package models.repository

import javax.inject.{Inject, Singleton}

import models.entity.Trash
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TrashRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  class TrashTable(tag: Tag) extends Table[Trash](tag, "TRASH") {

    def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("USER_ID")
    def volume = column[Int]("VOLUME")
    def empty = column[Boolean]("EMPTY")
    override def * = (id, userId, volume, empty) <> ((Trash.apply _).tupled, Trash.unapply)

  }

  val trashes = TableQuery[TrashTable]

  def listTrashes(userId: Long): Future[Seq[Trash]] = {
    dbConfig.db.run(trashes.filter(_.userId === userId).result)
  }

  def createTrash(userId: Long, volume: Int): Future[Trash] = {
    dbConfig.db.run {
      (trashes.map(trash => (trash.userId, trash.volume))
        returning trashes.map(tr => (tr.id, tr.empty))
        into((params, gen) => Trash(gen._1, params._1, params._2, gen._2))
        ) += (userId, volume)
    }
  }

  def getTrash(trashId: Long): Future[Option[Trash]] = {
    dbConfig.db.run(trashes.filter(_.id === trashId).result.headOption)
  }

  def deleteTrash(trashId: Long): Future[Int] = {
    dbConfig.db.run(trashes.filter(_.id === trashId).delete)
  }

  def getNbTrashesForUserId(userId: Long): Future[Seq[Trash]] = {
    dbConfig.db.run(trashes.filter(_.userId === userId).result)
  }

  def changeEmptiness(trashId: Long, empty: Boolean): Future[Int] = {
    dbConfig.db.run(trashes.filter(_.id === trashId).map(_.empty).update(empty))
  }

  def getWasteVolume(userId: Long): Future[Option[Int]] = {
    dbConfig.db.run(trashes.filter(trash => trash.userId === userId && !trash.empty).map(_.volume).sum.result)
  }

}
