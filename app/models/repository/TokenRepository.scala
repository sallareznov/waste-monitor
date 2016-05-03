package models.repository

import java.sql.Date
import javax.inject.{Inject, Singleton}
import models.entity.Token
import org.apache.commons.codec.binary.Base64
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
  * Repository for the TOKEN table
  * @param dbConfigProvider the config provider
  * @param ec the execution context
  */
@Singleton
class TokenRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  /**
    * The TOKEN table entity
    * @param tag the tag of the table
    */
  class TokenTable(tag: Tag) extends Table[Token](tag, "TOKEN") {

    def text = column[String]("TEXT")
    def userId = column[Long]("USER_ID")
    def expirationDelay = column[Option[Date]]("EXPIRATION_DELAY")
    override def * = (text, userId, expirationDelay) <> ((Token.apply _).tupled, Token.unapply)

  }

  val tokens = TableQuery[TokenTable]

  /**
    * Inserts a token
    * @param userId the id of the user
    * @param username the username
    * @param password the password
    * @return the inserted token encapsulated in a [[scala.concurrent.Future]] object
    */
  def addToken(userId: Long, username: String, password: String): Future[Token] = {
    val tokenText = Base64.encodeBase64String((username + ':' + password).getBytes)
    dbConfig.db.run {
      (tokens.map(token => (token.text, token.userId))
      returning tokens.map(_.expirationDelay)
        into((params, expirationDelay) => Token(params._1, params._2, expirationDelay))
        ) += (tokenText, userId)
    }
  }

  /**
    * Retrieves a token by its text
    * @param tokenText the text of the token
    * @return the token option encapsulated in a [[scala.concurrent.Future]] object
    */
  def getToken(tokenText: String): Future[Option[Token]] = {
    dbConfig.db.run(tokens.filter(_.text === tokenText).result.headOption)
  }

  def tokenDoesntExist(userId: Long): Future[Boolean] = {
    dbConfig.db.run(tokens.filter(_.userId === userId).exists.result)
  }

  /**
    * Retrieves a token by the id of its user
    * @param userId the id of the user
    * @return the token option encapsulated in a [[scala.concurrent.Future]] object
    */
  def getTokenForUserId(userId: Long): Future[Option[Token]] = {
    dbConfig.db.run(tokens.filter(_.userId === userId).result.headOption)
  }

}
