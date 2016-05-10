package controllers.api

import javax.inject.Inject

import controllers.ActionDSL.MonadicActions
import models.entity.Token
import models.form.UserValidationData
import models.message.{ErrorJSONMessage, UserInformations}
import models.repository.{TokenRepository, TrashRepository, UserRepository, WasteVolumeRepository}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.EitherT

class ApiController @Inject()(userRepository: UserRepository, tokenRepository: TokenRepository, trashRepository: TrashRepository, wasteVolumeRepository: WasteVolumeRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport with MonadicActions {

  /**
    * Displays all the reachable entry points and their specification
    *
    * @return 200 (OK) if the operation proceeded successfully
    *         500 (Internal Server Error) if an error occurred on the server
    */
  /*def index2() = Action {
    Ok(Json.toJson(List(RouteInformations("Attempts to register a new user", "/api/register", "POST", "None", Json.toJson("None"), Json.toJson("None"), Json.obj("username" -> "johndoe", "password" -> "johndoe"), Json.toJson(List("201 (Created) if the operation proceeded successfully and the user was created", "409 (Conflict) if a user with the same username already exists", "500 (Internal Server Error) if an error occurred on the server"))),
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
  }*/

  def index() = Action {
    Ok(Json.obj("users" -> "/api/users",
      "user" -> "/api/user",
      "trashes" -> "/api/user/trashes",
      "trash" -> "api/user/trash",
      "wasteVolume" -> "/api/user/wasteVolume"
    ))
  }

  /**
    * An action with the necessity to be authenticated
    *
    * @param futureResult the future result if the authentication token is valid
    * @return
    */
  def actionWithAuthorization(futureResult: (Token, Request[AnyContent]) => scalaz.EitherT[scala.concurrent.Future, play.api.mvc.Result, play.api.mvc.Result]) = Action.async {
    implicit request =>
      for {
        authorizationHeader <- request.headers.get("Authorization") ?| Forbidden(Json.toJson(ErrorJSONMessage("Requires authentication")))
        tokenText <- authorizationHeader.split(" ").lastOption ?| BadRequest(Json.toJson(ErrorJSONMessage("Bad request")))
        token <- tokenRepository.getToken(tokenText) ?| Forbidden(Json.toJson(ErrorJSONMessage("Bad credentials")))
        result <- futureResult(token, request)
      } yield result
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
      for {
        user <- request.body.validate[UserValidationData] ?| /*BadRequest(JsError.toJson(_ : ActionDSL.JsErrorContent))*/ InternalServerError
        _ <- userRepository.usernameDoesntExists(user.username) ?| Conflict(Json.toJson(ErrorJSONMessage("The username " + user.username + " already exists")))
        newUserId <- userRepository.createUser(user.username, user.password) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
        newToken <- tokenRepository.addToken(newUserId, user.username, user.password) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
      } yield Created(Json.toJson(newToken))
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
  def user(): Action[AnyContent] = {
    def futureResult(token: Token, request: Request[AnyContent]): EitherT[Future, Result, Result] = {
      for {
        user <- userRepository.getById(token.userId) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
        trashes <- trashRepository.getTrashesForUserId(token.userId) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
      } yield {
        val totalWasteVolume = trashes.foldLeft(0)((r, c) => r + c.volume)
        Ok(Json.toJson(UserInformations(user.username, token.text, trashes.length, totalWasteVolume)))
      }
    }
    actionWithAuthorization(futureResult)
  }

  /**
    *
    * @return
    */
  def listTrashes(): Action[AnyContent] = {
    def futureResult(token: Token, request: Request[AnyContent]): EitherT[Future, Result, Result] = {
      for {
        trashes <- trashRepository.listTrashes(token.userId) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
      } yield Ok(Json.toJson(trashes))
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
    def futureResult(token: Token, request: Request[AnyContent]): EitherT[Future, Result, Result] = {
      for {
        trash <- trashRepository.createTrash(token.userId, trashVolume) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
        totalWasteVolume <- trashRepository.getTotalWasteVolume(trash.userId) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
        _ <- wasteVolumeRepository.record(trash.userId, totalWasteVolume) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
      } yield Created(Json.obj("message" -> "The trash has been successfully created", "trash" -> Json.toJson(trash)))
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
    def futureResult(token: Token, request: Request[AnyContent]): EitherT[Future, Result, Result] = {
      for {
        trash <- trashRepository.getTrash(trashId) ?| NotFound(Json.obj("message" -> ("The trash with the id " + trashId + " doesn't exist"), "trashesUrl" -> ("http://" + request.host + "/api/user/trashes")))
      } yield Ok(Json.toJson(trash))
    }
    actionWithAuthorization(futureResult)
  }

  /**
    * Empties the specified trashs
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
    def futureResult(token: Token, request: Request[AnyContent]): EitherT[Future, Result, Result] = {
      for {
        trash <- trashRepository.getTrash(trashId) ?| NotFound(Json.obj("message" -> ("The trash with the id " + trashId + " doesn't exist"), "trashesUrl" -> ("http://" + request.host + "/api/user/trashes")))
        updatedRows <- trashRepository.changeEmptiness(trashId, empty) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
        totalWasteVolume <- trashRepository.getTotalWasteVolume(trash.userId) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
        record <- wasteVolumeRepository.record(trashId, totalWasteVolume) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
        updatedTrash <- trashRepository.getTrash(trashId) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
      } yield Ok(Json.toJson(updatedTrash))
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
    def futureResult(token: Token, request: Request[AnyContent]): EitherT[Future, Result, Result] = {
      for {
        deleted <- trashRepository.deleteTrash(trashId) ?| NotFound(Json.obj("message" -> ("the trash with the id " + trashId + " doesn't exist")))
      } yield NoContent
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
  def wasteVolume(): Action[AnyContent] = {
    def futureResult(token: Token, request: Request[AnyContent]): EitherT[Future, Result, Result] = {
      for {
        wasteVolumes <- wasteVolumeRepository.getWasteVolumesFromUser(token.userId) ?| InternalServerError(Json.toJson(ErrorJSONMessage("Internal server error")))
      } yield Ok(Json.toJson(wasteVolumes))
    }
    actionWithAuthorization(futureResult)
  }

  def generateChart() = TODO

  def indexOptions() = Action {
    implicit request => NoContent.withHeaders("Allow" -> "GET")
  }

  def usersOptions() = Action {
    implicit request => NoContent.withHeaders("Allow" -> "GET, POST")
  }

  def userOptions() = Action {
    implicit request => NoContent.withHeaders("Allow" -> "GET")
  }

  def trashesOptions() = Action {
    implicit request => NoContent.withHeaders("Allow" -> "GET, POST")
  }

  def trashOptions() = Action {
    implicit request => NoContent.withHeaders("Allow" -> "GET, PUT, DELETE")
  }

  def wasteVolumeOptions() = Action {
    implicit request => NoContent.withHeaders("Allow" -> "GET")
  }

}
