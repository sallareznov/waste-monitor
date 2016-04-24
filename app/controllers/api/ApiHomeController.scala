package controllers.api

import javax.inject.Inject

import models.entity.{ErrorJSONMessage, RouteInformations, UserInformations}
import models.repository.{TokenRepository, TrashRepository, UserRepository, WasteVolumeRepository}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}

class ApiHomeController @Inject()(userRepository: UserRepository, tokenRepository: TokenRepository, trashRepository: TrashRepository, wasteVolumeRepository: WasteVolumeRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def index() = Action {
    Ok(Json.toJson(List(RouteInformations("Attempts to log the user", "/api/login", "POST", None, None, Some(Json.obj("username" -> "sallareznov", "password" -> "sallareznov"))),
      RouteInformations("Attempts to register a new user", "/api/register", "POST", None, None, Some(Json.obj("username" -> "sallareznov", "password" -> "sallareznov"))),
      RouteInformations("Lists the registered users", "/api/users", "GET", None, None, None),
      RouteInformations("Shows informations about the authenticated user (his username, the number of trashes he owns and the total waste volume that are in his trashes)", "/api/user", "GET", Some("Authorization: Basic <access_token>"), None, None),
      RouteInformations("Shows informations about the trashes owned by the authenticated user", "/api/user/trashes", "GET", Some("Authorization: Basic <access_token>"), None, None),
      RouteInformations("Creates a new trash for the authenticated user", "/api/user/createTrash", "PUT", Some("Authorization: Basic <access_token>"), None, Some(Json.obj("volume" -> 5, "dumpFrequency" -> 10))),
      RouteInformations("Deletes the specified trash owned by the authenticated user", "/api/user/deleteTrash", "DELETE", Some("Authorization: Basic <access_token>"), Some(Json.obj("trashId" -> "the identifier of the trash")), None),
      RouteInformations("Shows informations about the selected trash owned by the authenticated user", "/api/user/trash", "GET", Some("Authorization: Basic <access_token>"), Some(Json.obj("trashId" -> "the identifier of the trash")), None),
      RouteInformations("Shows informations about the evolution of the authenticated user's waste", "/api/user/evolution", "GET", Some("Authorization: Basic <access_token>"), None, None)
    )))
  }

  def register() = Action.async(parse.json) {
    implicit request =>
      val requestBody = request.body
      val username = (requestBody \ "username").as[String]
      val password = (requestBody \ "password").as[String]
      userRepository.getByUsername(username).flatMap {
        case None =>
          userRepository.createUser(username, password).flatMap {
            case Some(userId) => tokenRepository.addToken(userId, username, password).map(newToken => Ok(Json.toJson(newToken)))
            case None => Future.successful(InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error"))))
          }
        case _ => Future.successful(Conflict(Json.toJson(ErrorJSONMessage("You are already registered"))))
      }
  }

  def login() = Action.async(parse.json) {
    implicit request =>
      val requestBody = request.body
      val username = (requestBody \ "username").as[String]
      val password = (requestBody \ "password").as[String]
      userRepository.getByUsernameAndPassword(username, password).flatMap {
        case Some(user) => user.id match {
          case Some(userId) =>
            tokenRepository.getTokenForUserId(userId).flatMap {
              case Some(_) => tokenRepository.getTokenForUserId(userId).map(token => Ok(Json.toJson(token)))
              case None => tokenRepository.addToken(userId, username, password).map(newToken => Ok(Json.toJson(newToken)))
            }
          case None => Future.successful(InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error"))))
        }
        case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Bad credentials"))))
      }
  }

  def listUsers() = Action.async {
    userRepository.listAll().map(users => Ok(Json.toJson(users)))
  }

  def currentUser() = Action.async {
    implicit request => {
      request.headers.get("Authorization") match {
        case Some(authorizationHeader) =>
          val tokenTextOption = authorizationHeader.split(" ").lastOption
          tokenTextOption match {
            case None => Future.successful(BadRequest(Json.toJson(ErrorJSONMessage("Bad request"))))
            case Some(tokenText) =>
              tokenRepository.getToken(tokenText).flatMap {
                case Some(token) =>
                  userRepository.getById(token.userId).flatMap {
                    case Some(user) => trashRepository.getNbTrashesForUserId(token.userId).map(trashes => {
                      val totalWasteVolume = trashes.foldLeft(0)((r, c) => r + c.volume)
                      Ok(Json.toJson(UserInformations(user.username, token.text, trashes.length, totalWasteVolume)))
                    })
                    case None => Future.successful(InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error"))))
                  }
                case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Bad credentials"))))
              }
          }
        case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Requires authentication"))))
      }
    }
  }

  def createTrash(trashVolume: Int) = Action.async {
    implicit request =>
      request.headers.get("Authorization") match {
        case Some(authorizationHeader) =>
          val tokenTextOption = authorizationHeader.split(" ").lastOption
          tokenTextOption match {
            case None => Future.successful(BadRequest(Json.toJson(ErrorJSONMessage("Bad request"))))
            case Some(tokenText) =>
              tokenRepository.getToken(tokenText).flatMap({
                case Some(token) => trashRepository.createTrash(token.userId, trashVolume).flatMap(
                  trash => trashRepository.getWasteVolume(trash.userId).flatMap {
                    case Some(wasteVolume) => wasteVolumeRepository.record(trash.userId, wasteVolume).map(_ => Ok(Json.toJson(trash)))
                    case None => Future.successful(InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error"))))
                  })
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

  def changeEmptiness(trashId: Long, empty: Boolean) = Action.async {
    implicit request =>
      request.headers.get("Authorization") match {
        case Some(authorizationHeader) =>
          val tokenTextOption = authorizationHeader.split(" ").lastOption
          tokenTextOption match {
            case None => Future.successful(BadRequest(Json.toJson(ErrorJSONMessage("Bad request"))))
            case Some(tokenText) =>
              tokenRepository.getToken(tokenText).flatMap({
                case Some(token) => trashRepository.getTrash(trashId).flatMap {
                  case Some(trash) =>
                    trash.empty match {
                      case x if x == !empty => trashRepository.changeEmptiness(trashId, empty).flatMap {
                        _ => trashRepository.getWasteVolume(trash.userId).flatMap {
                          case Some(wasteVolume) => wasteVolumeRepository.record(trashId, wasteVolume).flatMap(_ => trashRepository.getTrash(trashId).map(updatedTrash => Ok(Json.toJson(updatedTrash))))
                          case None => Future.successful(InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error"))))
                        }
                      }
                      case true => Future.successful(Conflict(Json.toJson(ErrorJSONMessage("This trash is already empty"))))
                    }
                  case None => Future.successful(InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error"))))
                }
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

  def evolution() = Action.async {
    implicit request =>
      request.headers.get("Authorization") match {
        case Some(authorizationHeader) =>
          val tokenTextOption = authorizationHeader.split(" ").lastOption
          tokenTextOption match {
            case None => Future.successful(BadRequest(Json.toJson(ErrorJSONMessage("Bad request"))))
            case Some(tokenText) =>
              tokenRepository.getToken(tokenText).flatMap({
                case Some(token) => wasteVolumeRepository.getWasteVolumesFromUser(token.userId).map(wasteVolumes => Ok(Json.toJson(wasteVolumes)))
                case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Bad credentials"))))
              })
          }
        case None => Future.successful(Unauthorized(Json.toJson(ErrorJSONMessage("Requires authentication"))))
      }
  }

}
