package models.repository

import javax.inject.{Inject, Singleton}

import com.github.t3hnar.bcrypt._
import models.entity.User
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Repository for the USER table
  * @param dbConfigProvider the config provider
  * @param ec the execution context
  */
@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  /**
    * The USER table entity
    * @param tag the tag of the table
    */
  class UserTable(tag: Tag) extends Table[User](tag, "USER") {

    def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
    def username = column[String]("USERNAME")
    def hash = column[String]("HASH")
    override def * = (id, username, hash) <> ((User.apply _).tupled, User.unapply)

  }

  val users = TableQuery[UserTable]

  /**
    * inserts a new user
    * @param username the username
    * @param password the password
    * @return encapsulated in a [[Future]] object
    */
  def createUser(username: String, password: String): Future[Option[Long]] = {
    val entity = User(None, username, password.bcrypt)
    dbConfig.db.run(users returning users.map(_.id) += entity)
  }

  /**
    * retrieves a user by his id
    * @param userId the id of the user
    * @return the user option encapsulated in a [[Future]] object
    */
  def getById(userId: Long): Future[Option[User]] = {
    dbConfig.db.run(users.filter(_.id === userId).result.headOption)
  }

  /**
    * retrieves a user by his username
    * @param username the username
    * @return the user option encapsulated in a [[Future]] object
    */
  def getByUsername(username: String): Future[Option[User]] = {
    dbConfig.db.run(users.filter(_.username === username).result.headOption)
  }

  /**
    * retrieves a user by his username and his password
    * @param username the username
    * @param password the password
    * @return the user option encapsulated in a [[Future]] object
    */
  def getByUsernameAndPassword(username: String, password: String): Future[Option[User]] = {
    dbConfig.db.run(users.filter(user => user.username === username && user.hash === password.bcrypt).result.headOption)
  }

  /**
    * gets all the users
    * @return all the users
    */
  def listAll(): Future[Seq[User]] = {
    dbConfig.db.run(users.result)
  }

}
