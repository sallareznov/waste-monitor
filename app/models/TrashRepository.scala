package models

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
    def dumpFrequency = column[Int]("DUMP_FREQUENCY")

    override def * = (id, userId, volume, dumpFrequency) <> ((Trash.apply _).tupled, Trash.unapply)
  }

  val trashes = TableQuery[TrashTable]

  def listTrashes(userId: Long): Future[Seq[Trash]] = {
    dbConfig.db.run(trashes.filter(_.userId === userId).result)
  }

}
