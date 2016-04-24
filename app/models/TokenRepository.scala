package models

import java.sql.Date
import javax.inject.{Inject, Singleton}

import org.apache.commons.codec.binary.Base64
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TokenRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  class TokenTable(tag: Tag) extends Table[Token](tag, "TOKEN") {

    def text = column[String]("TEXT")
    def userId = column[Long]("USER_ID")
    def expirationDelay = column[Option[Date]]("EXPIRATION_DELAY")

    override def * = (text, userId, expirationDelay) <> ((Token.apply _).tupled, Token.unapply)
  }

  val tokens = TableQuery[TokenTable]

  def addToken(userId: Long, username: String, password: String): Future[Token] = {
    val tokenText = Base64.encodeBase64String((username + ':' + password).getBytes)
    dbConfig.db.run {
      (tokens.map(token => (token.text, token.userId))
      returning tokens.map(_.expirationDelay)
        into((params, expirationDelay) => Token(params._1, params._2, expirationDelay))
        ) += (tokenText, userId)
    }
  }

  def getToken(tokenText: String): Future[Option[Token]] = {
    dbConfig.db.run(tokens.filter(_.text === tokenText).result.headOption)
  }

  def getTokenForUserId(userId: Long): Future[Option[Token]] = {
    dbConfig.db.run(tokens.filter(_.userId === userId).result.headOption)
  }

}
