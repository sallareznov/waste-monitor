package controllers.api

import javax.inject.Inject

import models.{TokenRepository, UserRepository}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.{Future, ExecutionContext}

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
      usersRepository.getByUsername(username).map({
        case None =>
          /*usersRepository.add(username, password)
          tokenRepository.addToken(username, password)
          tokenRepository.getTokenForUsername(username) match {
            case Future(Some(token)) => Ok(Json.toJson(token))
            case _ => BadRequest
          }*/
          Ok
        case Some(_) => Conflict
      })

  }

  def currentUser() = Action.async {
    implicit request => {
      request.headers.get("Authorization") match {
        case Some(authorizationHeader) =>
          val tokenText = authorizationHeader.split(" ").last
          tokenRepository.getToken(tokenText).map({
            case Some(result) => Ok
            case None => Unauthorized(Json.obj("message" -> "Bad credentials", "documentation" -> "https://github.com/sallareznov/waste-monitor"))
          })
        case None => Future.successful(Unauthorized(Json.obj("message" -> "Requires authentication", "documentation" -> "https://github.com/sallareznov/waste-monitor")))
      }
    }
  }

}
