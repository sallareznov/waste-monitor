package controllers.api

import javax.inject.Inject

import models.{ErrorJSONMessage, TokenRepository, UserRepository}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}

class ApiHomeController @Inject()(usersRepository: UserRepository, tokenRepository: TokenRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def listUsers() = Action.async {
    usersRepository.listAll().map(users => Ok(Json.toJson(users)))
  }

  def login() = Action {
    Ok
  }

  def register(credentials: String) = Action.async {
    implicit request =>
      val tokens = credentials.split(",")
      val (username, password) = (tokens(0), tokens(1))
      usersRepository.getByUsername(username).flatMap {
        case None =>
          usersRepository.add(username, password)
          tokenRepository.addToken(username, password)
          tokenRepository.getTokenForUsername(username).map({
            case Some(token) => Ok(Json.toJson(token))
            case _ => InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
          })
        case _ => Future.successful(Conflict(Json.toJson(ErrorJSONMessage("You are already registered"))))
      }
  }

  def currentUser() = Action.async {
    implicit request => {
      request.headers.get("Authorization") match {
        case Some(authorizationHeader) =>
          val tokenTextOption = authorizationHeader.split(" ").lastOption
          tokenTextOption match {
            case None => Future.successful(BadRequest(Json.toJson(ErrorJSONMessage("Bad request"))))
            case Some(tokenText) =>
              tokenRepository.getToken(tokenText).map({
                case Some(result) => Ok
                case None => Unauthorized(Json.toJson(ErrorJSONMessage("Bad credentials")))
              })
          }
        case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Requires authentication"))))
      }
    }
  }

}
