package models

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

import com.github.t3hnar.bcrypt._

@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  class UserTable(tag: Tag) extends Table[User](tag, "USER") {

    def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
    def username = column[String]("USERNAME")
    def hash = column[String]("HASH")

    override def * = (id, username, hash) <> ((User.apply _).tupled, User.unapply)
  }

  val users = TableQuery[UserTable]

  def add(username: String, password: String) = {
    dbConfig.db.run(users += User(None, username, password.bcrypt))
  }

  def getById(id: Long): Future[Option[User]] = {
    dbConfig.db.run(users.filter(_.id === id).result.headOption)
  }

  def getByUsername(username: String): Future[Option[User]] = {
    dbConfig.db.run(users.filter(_.username === username).result.headOption)
  }

  def listAll(): Future[Seq[User]] = {
    dbConfig.db.run(users.result)
  }

}
