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
    def username = column[String]("USERNAME", O.PrimaryKey)
    def expirationDelay = column[Option[Date]]("EXPIRATION_DELAY")

    override def * = (text, username, expirationDelay) <> ((Token.apply _).tupled, Token.unapply)
  }

  val tokens = TableQuery[TokenTable]

  def addToken(username: String, password: String) = {
    val token = Base64.encodeBase64String((username + ':' + password).getBytes)
    dbConfig.db.run(tokens.map(token => (token.text, token.username)).insertOrUpdate(token, username))
  }

  def getToken(tokenText: String): Future[Option[Token]] = {
    dbConfig.db.run(tokens.filter(_.text === tokenText).result.headOption)
  }

  def getTokenForUsername(username: String): Future[Option[Token]] = {
    dbConfig.db.run(tokens.filter(_.username === username).result.headOption)
  }

}
