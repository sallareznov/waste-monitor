package controllers.api

import javax.inject.Inject

import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}

class ApiHomeController @Inject()(usersRepository: UserRepository, tokenRepository: TokenRepository, trashRepository: TrashRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def index() = Action {
    Ok(Json.toJson(List(RouteInformations("Attempts to log the user", "/api/login", "POST", None, None, Some(Json.obj("username" -> "john", "password" -> "john"))),
      RouteInformations("Attempts to register a new user", "/api/register", "POST", None, None, Some(Json.obj("username" -> "john", "password" -> "john"))),
      RouteInformations("Lists the registered users", "/api/users", "GET", None, None, None),
      RouteInformations("Shows informations about the authenticated user (his username, the number of trashes he owns and the total waste volume that are in his trashes)", "/api/user", "GET", Some("Authorization: Basic <access_token>"), None, None),
      RouteInformations("Shows informations about the trashes owned by the authenticated user", "/api/user/trashes", "GET", Some("Authorization: Basic <access_token>"), None, None),
      RouteInformations("Creates a new trash for the authenticated user", "/api/user/createTrash", "PUT", Some("Authorization: Basic <access_token>"), None, Some(Json.obj("volume" -> 5, "dumpFrequency" -> 10))),
      RouteInformations("Deletes the specified trash owned by the authenticated user", "/api/user/deleteTrash", "DELETE", Some("Authorization: Basic <access_token>"), Some(Json.obj("trashId" -> "the identifier of the trash")), None),
      RouteInformations("Shows informations about the selected trash owned by the authenticated user", "/api/user/trash", "GET", Some("Authorization: Basic <access_token>"), Some(Json.obj("trashId" -> "the identifier of the trash")), None),
      RouteInformations("Shows informations about the evolution of the authenticated user's waste", "/api/user/monitor", "GET", Some("Authorization: Basic <access_token>"), None, None)
    )))
  }

  def listUsers() = Action.async {
    usersRepository.listAll().map(users => Ok(Json.toJson(users)))
  }

  def login() = Action.async(parse.json) {
    implicit request =>
      val requestBody = request.body
      val username = (requestBody \ "username").as[String]
      val password = (requestBody \ "password").as[String]
      usersRepository.getByUsernameAndPassword(username, password).map {
        case Some(user) => Ok(Json.toJson(user))
        case None => Unauthorized(Json.toJson(ErrorJSONMessage("Bad credentials")))
      }
  }

  def register() = Action.async(parse.json) {
    implicit request =>
      val requestBody = request.body
      val username = (requestBody \ "username").as[String]
      val password = (requestBody \ "password").as[String]
      usersRepository.getByUsername(username).flatMap {
        case None =>
          usersRepository.createUser(username, password).flatMap {
            case Some(userId) => tokenRepository.addToken(userId, username, password).map(newToken => Ok(Json.toJson(newToken)))
            case None => Future.successful(InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error"))))
          }
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

  def createTrash() = Action.async(parse.json) {
    implicit request =>
      val requestBody = request.body
      val trashVolume = (requestBody \ "volume").as[Int]
      val trashDumpFrequency = (requestBody \ "dumpFrequency").as[Int]
      request.headers.get("Authorization") match {
        case Some(authorizationHeader) =>
          val tokenTextOption = authorizationHeader.split(" ").lastOption
          tokenTextOption match {
            case None => Future.successful(BadRequest(Json.toJson(ErrorJSONMessage("Bad request"))))
            case Some(tokenText) =>
              tokenRepository.getToken(tokenText).flatMap({
                case Some(token) => trashRepository.createTrash(token.userId, trashVolume, trashDumpFrequency).map(trash => Ok(Json.toJson(trash)))
                case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Bad credentials"))))
              })
          }
        case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Requires authentication"))))
      }
  }

  def listTrashes() = Action.async {
    implicit request =>
      request.headers.get("Authorization") match {
        case Some(authorizationHeader) =>
          val tokenTextOption = authorizationHeader.split(" ").lastOption
          tokenTextOption match {
            case None => Future.successful(BadRequest(Json.toJson(ErrorJSONMessage("Bad request"))))
            case Some(tokenText) =>
              tokenRepository.getToken(tokenText).flatMap({
                case Some(token) => trashRepository.listTrashes(token.userId).map(trashes => Ok(Json.toJson(trashes)))
                case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Bad credentials"))))
              })
          }
        case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Requires authentication"))))
      }
  }

  def trash(trashId: Long) = Action.async {
    implicit request =>
      request.headers.get("Authorization") match {
        case Some(authorizationHeader) =>
          val tokenTextOption = authorizationHeader.split(" ").lastOption
          tokenTextOption match {
            case None => Future.successful(BadRequest(Json.toJson(ErrorJSONMessage("Bad request"))))
            case Some(tokenText) =>
              tokenRepository.getToken(tokenText).flatMap({
                case Some(token) => trashRepository.getTrash(trashId).map(trash => Ok(Json.toJson(trash)))
                case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Bad credentials"))))
              })
          }
        case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Requires authentication"))))
      }
  }

  def deleteTrash(trashId: Long) = Action.async {
    implicit request =>
      request.headers.get("Authorization") match {
        case Some(authorizationHeader) =>
          val tokenTextOption = authorizationHeader.split(" ").lastOption
          tokenTextOption match {
            case None => Future.successful(BadRequest(Json.toJson(ErrorJSONMessage("Bad request"))))
            case Some(tokenText) =>
              tokenRepository.getToken(tokenText).flatMap({
                case Some(token) => trashRepository.deleteTrash(trashId).map(deletedTrashId => Ok(Json.obj("message" -> "the trash has been successfully deleted", "trashId" -> deletedTrashId)))
                case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Bad credentials"))))
              })
          }
        case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Requires authentication"))))
      }
  }

  def evolution() = TODO

}
