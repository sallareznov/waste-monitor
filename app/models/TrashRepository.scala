package models

import java.sql.Date
import javax.inject.{Inject, Singleton}

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
    def emptyFrequency = column[Int]("EMPTY_FREQUENCY")
    def lastEmptyTime = column[Option[Date]]("LAST_EMPTY_TIME")

    override def * = (id, userId, volume, emptyFrequency, lastEmptyTime) <> ((Trash.apply _).tupled, Trash.unapply)
  }

  val trashes = TableQuery[TrashTable]

  def listTrashes(userId: Long): Future[Seq[Trash]] = {
    dbConfig.db.run(trashes.filter(_.userId === userId).result)
  }

  def createTrash(userId: Long, volume: Int, dumpFrequency: Int): Future[Trash] = {
    dbConfig.db.run {
      (trashes.map(trash => (trash.userId, trash.volume, trash.emptyFrequency))
        returning trashes.map(tr => (tr.id, tr.lastEmptyTime))
        into((params, gen) => Trash(gen._1, params._1, params._2, params._3, gen._2))
        ) += (userId, volume, dumpFrequency)
    }
  }

  def getTrash(trashId: Long): Future[Option[Trash]] = {
    dbConfig.db.run(trashes.filter(_.id === trashId).result.headOption)
  }

  def deleteTrash(trashId: Long) = {
    dbConfig.db.run(trashes.filter(_.id === trashId).delete)
  }

}
