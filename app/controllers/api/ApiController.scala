package controllers.api

import javax.inject.Inject

import models.entity.{Token, User}
import models.form.UserValidationData
import models.message.{ErrorJSONMessage, RouteInformations, UserInformations}
import models.repository.{TokenRepository, TrashRepository, UserRepository, WasteVolumeRepository}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class ApiController @Inject()(userRepository: UserRepository, tokenRepository: TokenRepository, trashRepository: TrashRepository, wasteVolumeRepository: WasteVolumeRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  /**
    * Displays all the reachable entry points and their specification
    *
    * @return 200 (OK) if the operation proceeded successfully
    *         500 (Internal Server Error) if an error occurred on the server
    */
  def index() = Action {
    Ok(Json.toJson(List(RouteInformations("Attempts to register a new user", "/api/register", "PUT", "None", Json.toJson("None"), Json.toJson("None"), Json.obj("username" -> "johndoe", "password" -> "johndoe"), Json.toJson(List("201 (Created) if the operation proceeded successfully and the user was created", "409 (Conflict) if a user with the same username already exists", "500 (Internal Server Error) if an error occurred on the server"))),
      RouteInformations("Attempts to log a user", "/api/login", "POST", "None", Json.toJson("None"), Json.toJson("None"), Json.obj("username" -> "johndoe", "password" -> "johndoe"), Json.toJson(List("200 (OK) if the operation proceeded successfully", "401 (Unauthorized) if the credentials are invalid", "500 (Internal Server Error) if an error occurred on the server"))),
      RouteInformations("Lists the registered users", "/api/users", "GET", "None", Json.toJson("None"), Json.toJson("None"), Json.toJson("None"), Json.toJson(List("200 (OK) if the operation proceeded successfully", "500 (Internal Server Error) if an error occurred on the server"))),
      RouteInformations("Shows informations about the authenticated user (his username, the number of trashes he owns and the total waste volume that are in his trashes)", "/api/user", "GET", "Authorization: Basic <access_token>", Json.toJson("None"), Json.toJson("None"), Json.toJson("None"), Json.toJson(List("200 (OK) if the operation proceeded successfully", "400 (Bad Request) if the authentication token wasn't provided", "403 (Forbidden) if the authentication token is invalid or has expired", "500 (Internal Server Error) if an error occurred on the server"))),
      RouteInformations("Shows informations about the trashes owned by the authenticated user", "/api/user/trashes", "GET", "Authorization: Basic <access_token>", Json.toJson("None"), Json.toJson("None"), Json.toJson("None"), Json.toJson(List("200 (OK) if the operation proceeded successfully", "400 (Bad Request) if the authentication token wasn't provided", "403 (Forbidden) if the authentication token is invalid or has expired", "500 (Internal Server Error) if an error occurred on the server"))),
      RouteInformations("Creates a new trash for the authenticated user", "/api/user/createTrash", "PUT", "Authorization: Basic <access_token>", Json.toJson("None"), Json.obj("volume" -> 5), Json.toJson("None"), Json.toJson(List("201 (Created) if the operation proceeded successfully and the trash was created", "400 (Bad Request) if the authentication token wasn't provided", "403 (Forbidden) if the authentication token is invalid or has expired", "500 (Internal Server Error) if an error occurred on the server"))),
      RouteInformations("Shows informations about the selected trash owned by the authenticated user", "/api/user/trash", "GET", "Authorization: Basic <access_token>", Json.obj("trashId" -> "the identifier of the trash"), Json.toJson("None"), Json.toJson("None"), Json.toJson(List("201 (Created) if the operation proceeded successfully and the trash was created", "400 (Bad Request) if the authentication token wasn't provided", "403 (Forbidden) if the authentication token is invalid or has expired", "500 (Internal Server Error) if an error occurred on the server"))),
      RouteInformations("Empties the specified trash", "/api/user/emptyTrash", "POST", "Authorization: Basic <access_token>", Json.obj("trashId" -> "the identifier of the trash"), Json.toJson("None"), Json.toJson("None"), Json.toJson(List("200 (OK) if the operation proceeded successfully", "400 (Bad Request) if the authentication token wasn't provided", "403 (Forbidden) if the authentication token is invalid or has expired", "404 (Not Found) if the trash with the specified id doesn't exist", "409 (Conflict) if the trash is already empty", "500 (Internal Server Error) if an error occurred on the server"))),
      RouteInformations("Fills the specified trash", "/api/user/fillTrash", "POST", "Authorization: Basic <access_token>", Json.obj("trashId" -> "the identifier of the trash"), Json.toJson("None"), Json.toJson("None"), Json.toJson(List("200 (OK) if the operation proceeded successfully", "400 (Bad Request) if the authentication token wasn't provided", "403 (Forbidden) if the authentication token is invalid or has expired", "404 (Not Found) if the trash with the specified id doesn't exist", "409 (Conflict) if the trash is already empty", "500 (Internal Server Error) if an error occurred on the server"))),
      RouteInformations("Deletes the specified trash owned by the authenticated user", "/api/user/deleteTrash", "DELETE", "Authorization: Basic <access_token>", Json.obj("trashId" -> "the identifier of the trash"), Json.toJson("None"), Json.toJson("None"), Json.toJson(List("200 (OK) if the operation proceeded successfully", "400 (Bad Request) if the authentication token wasn't provided", "403 (Forbidden) if the authentication token is invalid or has expired", "404 (Not Found) if the trash with the specified id doesn't exist", "500 (Internal Server Error) if an error occurred on the server"))),
      RouteInformations("Shows informations about the evolution of the authenticated user's waste", "/api/user/evolution", "GET", "Authorization: Basic <access_token>", Json.toJson("None"), Json.toJson("None"), Json.toJson("None"), Json.toJson(List("200 (OK) if the operation proceeded successfully", "400 (Bad Request) if the authentication token wasn't provided", "403 (Forbidden) if the authentication token is invalid or has expired", "500 (Internal Server Error) if an error occurred on the server")))
    )))
  }

  /**
    * An action with the necessity to be authenticated
    *
    * @param futureResult the future result if the authentication token is valid
    * @return
    */
  def actionWithAuthorization(futureResult: (Token, Request[AnyContent]) => Future[Result]) = Action.async {
    implicit request =>
      request.headers.get("Authorization") match {
        case Some(authorizationHeader) =>
          val tokenTextOption = authorizationHeader.split(" ").lastOption
          tokenTextOption match {
            case None => Future.successful(BadRequest(Json.toJson(ErrorJSONMessage("Bad request"))))
            case Some(tokenText) =>
              tokenRepository.getToken(tokenText).flatMap {
                case Some(token) => futureResult(token, request)
                case None => Future.successful(Forbidden(Json.toJson(ErrorJSONMessage("Bad credentials"))))
              }
          }
        case None => Future.successful(Forbidden(Json.toJson(ErrorJSONMessage("Requires authentication"))))
      }
  }

  /**
    * Attempts to register a new user
    *
    * @return 201 (Created) if the operation proceeded successfully and the user was created
    *         409 (Conflict) if a user with the same username already exists
    *         500 (Internal Server Error) if an error occurred on the server
    */
  def register() = Action.async(parse.json) {
    implicit request =>
      val requestBody = request.body
      val username = (requestBody \ "username").as[String]
      val password = (requestBody \ "password").as[String]
      userRepository.getByUsername(username).flatMap {
        case None =>
          userRepository.createUser(username, password).flatMap {
            case Some(userId) => tokenRepository.addToken(userId, username, password).map(newToken => Created(Json.toJson(newToken)))
            case None => Future.successful(InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error"))))
          }
        case _ => Future.successful(Conflict(Json.toJson(ErrorJSONMessage("You are already registered"))))
      }
  }

  /**
    * Attempts to log the user
    *
    * @return 200 (OK) if the operation proceeded successfully
    *         401 (Unauthorized) if the credentials are invalid
    *         500 (Internal Server Error) if an error occurred on the server
    */
  def login() = Action.async(parse.json) {
    implicit request =>
      //val userResult = request.body.validate[UserValidationData]
      val username = (request.body \ "username").as[String]
      val password = (request.body \ "password").as[String]
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

  /**
    * Lists the registered users
    *
    * @return 200 (OK) if the operation proceeded successfully
    *         500 (Internal Server Error) if an error occurred on the server
    */
  def listUsers() = Action.async {
    userRepository.listAll().map(users => Ok(Json.toJson(users)))
  }

  /**
    * Shows informations about the authenticated user (his username, the number of trashes he owns and the total waste volume that are in his trashes)
    *
    * @return 200 (OK) if the operation proceeded successfully
    *         400 (Bad Request) if the authentication token wasn't provided
    *         403 (Forbidden) if the authentication token is invalid or has expired
    *         500 (Internal Server Error) if an error occurred on the server
    */
  def currentUser(): Action[AnyContent] = {
    def futureResult(token: Token, request: Request[AnyContent]): Future[Result] = {
      userRepository.getById(token.userId).flatMap {
        case Some(user) => trashRepository.getTrashesForUserId(token.userId).map(trashes => {
          val totalWasteVolume = trashes.foldLeft(0)((r, c) => r + c.volume)
          Ok(Json.toJson(UserInformations(user.username, token.text, trashes.length, totalWasteVolume)))
        })
        case None => Future.successful(InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error"))))
      }
    }
    actionWithAuthorization(futureResult)
  }

  /**
    *
    * @return
    */
  def listTrashes(): Action[AnyContent] = {
    def futureResult(token: Token, request: Request[AnyContent]): Future[Result] = {
      trashRepository.listTrashes(token.userId).map(trashes => Ok(Json.toJson(trashes)))
    }
    actionWithAuthorization(futureResult)
  }

  /**
    * Creates a new trash for the authenticated user
    *
    * @param trashVolume the volume of the trash
    * @return 201 (Created) if the operation proceeded successfully and the trash was created
    *         400 (Bad Request) if the authentication token wasn't provided
    *         403 (Forbidden) if the authentication token is invalid or has expired
    *         500 (Internal Server Error) if an error occurred on the server
    */
  def createTrash(trashVolume: Int): Action[AnyContent] = {
    def futureResult(token: Token, request: Request[AnyContent]): Future[Result] = {
      trashRepository.createTrash(token.userId, trashVolume).flatMap(
        trash => trashRepository.getTotalWasteVolume(trash.userId).flatMap {
          case Some(wasteVolume) => wasteVolumeRepository.record(trash.userId, wasteVolume).map(_ => Created(Json.toJson(trash)))
          case None => Future.successful(InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error"))))
        })
    }
    actionWithAuthorization(futureResult)
  }


  /**
    * Shows informations about the selected trash owned by the authenticated user
    *
    * @param trashId the id of the trash
    * @return 200 (OK) if the operation proceeded successfully
    *         400 (Bad Request) if the authentication token wasn't provided
    *         403 (Forbidden) if the authentication token is invalid or has expired
    *         500 (Internal Server Error) if an error occurred on the server
    */
  def trash(trashId: Long): Action[AnyContent] = {
    def futureResult(token: Token, request: Request[AnyContent]): Future[Result] = {
      trashRepository.getTrash(trashId).map {
        case Some(trash) => Ok(Json.toJson(trash))
        case None => NotFound(Json.obj("message" -> ("The trash with the id " + trashId + " doesn't exist"), "trashesUrl" -> ("http://" + request.host + "/api/user/trashes")))
      }
    }
    actionWithAuthorization(futureResult)
  }

  /**
    * Empties the specified trash
    *
    * @param trashId the id of the trash
    * @param empty   <code>true</code> if the trash will be emptied, <code>false</code> if it will be filled
    * @return 200 (OK) if the operation proceeded successfully
    *         400 (Bad Request) if the authentication token wasn't provided
    *         403 (Forbidden) if the authentication token is invalid or has expired
    *         404 (Not Found) if the trash with the specified id doesn't exist
    *         409 (Conflict) if the trash is already empty
    *         500 (Internal Server Error) if an error occurred on the server
    */
  def changeEmptiness(trashId: Long, empty: Boolean): Action[AnyContent] = {
    def futureResult(token: Token, request: Request[AnyContent]): Future[Result] = {
      trashRepository.getTrash(trashId).flatMap {
        case Some(trash) =>
          trash.empty match {
            case x if x != empty => trashRepository.changeEmptiness(trashId, empty).flatMap {
              _ => trashRepository.getTotalWasteVolume(trash.userId).flatMap {
                case Some(wasteVolume) => wasteVolumeRepository.record(trashId, wasteVolume).flatMap(_ => trashRepository.getTrash(trashId).map(updatedTrash => Ok(Json.toJson(updatedTrash))))
                case None => Future.successful(InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error"))))
              }
            }
            case _ => Future.successful(Conflict(Json.toJson(ErrorJSONMessage("This trash is already " + (if (empty) "empty" else "filled")))))
          }
        case None => Future.successful(NotFound(Json.obj("message" -> ("The trash with the id " + trashId + " doesn't exist"), "trashesUrl" -> ("http://" + request.host + "/api/user/trashes"))))
      }
    }
    actionWithAuthorization(futureResult)
  }

  /**
    * Deletes the specified trash owned by the authenticated user
    *
    * @param trashId the id of the trash
    * @return 200 (OK) if the operation proceeded successfully
    *         400 (Bad Request) if the authentication token wasn't provided
    *         403 (Forbidden) if the authentication token is invalid or has expired
    *         404 (Not Found) if the trash with the specified id doesn't exist
    *         500 (Internal Server Error) if an error occurred on the server
    */
  def deleteTrash(trashId: Long): Action[AnyContent] = {
    def futureResult(token: Token, request: Request[AnyContent]): Future[Result] = {
      trashRepository.deleteTrash(trashId).map {
        case 1 => Ok(Json.obj("message" -> "the trash has been successfully deleted", "trashId" -> trashId))
        case _ => NotFound(Json.obj("message" -> ("the trash with the id " + trashId + " doesn't exist")))
      }
    }
    actionWithAuthorization(futureResult)
  }

  /**
    * Shows informations about the evolution of the authenticated user's waste
    *
    * @return 200 (OK) if the operation proceeded successfully
    *         400 (Bad Request) if the authentication token wasn't provided
    *         403 (Forbidden) if the authentication token is invalid or has expired
    *         500 (Internal Server Error) if an error occurred on the server
    */
  def evolution(): Action[AnyContent] = {
    def futureResult(token: Token, request: Request[AnyContent]): Future[Result] = {
      wasteVolumeRepository.getWasteVolumesFromUser(token.userId).map(wasteVolumes => Ok(Json.toJson(wasteVolumes)))
    }
    actionWithAuthorization(futureResult)
  }


  def generateChart() = TODO

}
