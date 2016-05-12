package controllers.api

import javax.inject.Inject

import models.entity.Token
import models.message.ErrorJSONMessage
import models.repository.{TokenRepository, TrashRepository, UserRepository, WasteVolumeRepository}
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.EitherT

class ApiTrashController @Inject()(userRepository: UserRepository, trashRepository: TrashRepository, wasteVolumeRepository: WasteVolumeRepository, tokenRepository: TokenRepository, messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends AbstractApiController(tokenRepository, messagesApi) {

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

  def trashesOptions() = Action {
    implicit request => NoContent.withHeaders("Allow" -> "GET, POST")
  }

  def trashOptions() = Action {
    implicit request => NoContent.withHeaders("Allow" -> "GET, PUT, DELETE")
  }

}
