package controllers.api

import javax.inject.Inject

import controllers.ActionDSL.MonadicActions
import models.entity.Token
import models.message.ErrorJSONMessage
import models.repository.{TokenRepository, TrashRepository, UserRepository, WasteVolumeRepository}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.EitherT

class ApiWasteVolumeController @Inject() (userRepository: UserRepository, tokenRepository: TokenRepository, trashRepository: TrashRepository, wasteVolumeRepository: WasteVolumeRepository, messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends AbstractApiController(tokenRepository, messagesApi) with I18nSupport with MonadicActions {

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

  def wasteVolumeOptions() = Action {
    implicit request => NoContent.withHeaders("Allow" -> "GET")
  }

}
