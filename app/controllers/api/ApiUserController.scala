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

class ApiUserController @Inject()(userRepository: UserRepository, tokenRepository: TokenRepository, trashRepository: TrashRepository, wasteVolumeRepository: WasteVolumeRepository, messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends AbstractApiController(tokenRepository, messagesApi) with I18nSupport with MonadicActions {

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

  def usersOptions() = Action {
    implicit request => NoContent.withHeaders("Allow" -> "GET, POST")
  }

  def userOptions() = Action {
    implicit request => NoContent.withHeaders("Allow" -> "GET")
  }

}
