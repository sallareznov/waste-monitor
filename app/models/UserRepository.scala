package models

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  class UserTable(tag: Tag) extends Table[User](tag, "USER") {

    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def hash = column[String]("hash")

    override def * = (id, username, hash) <> ((User.apply _).tupled, User.unapply)
  }

  val users = TableQuery[UserTable]

  def add(user: User): Future[String] = {
    dbConfig.db.run(users += user).map(res => "User successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
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
