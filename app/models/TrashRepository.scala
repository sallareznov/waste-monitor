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

  class TrashTable(tag: Tag) extends Table[Trash](tag, "trash") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def volume = column[Int]("volume")
    def dumpDate = column[Date]("dump_date")

    override def * = (id, userId, volume, dumpDate) <> ((Trash.apply _).tupled, Trash.unapply)
  }

  val trashes = TableQuery[TrashTable]

  def listTrashes(userId: Long): Future[Seq[Trash]] = {
    dbConfig.db.run(trashes.filter(_.userId === userId).result)
  }

}
