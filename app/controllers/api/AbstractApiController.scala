package controllers.api

import javax.inject.Inject

import controllers.ActionDSL.MonadicActions
import models.entity.Token
import models.message.ErrorJSONMessage
import models.repository.TokenRepository
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller, Request}

import scala.concurrent.ExecutionContext

abstract class AbstractApiController @Inject()(tokenRepository: TokenRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport with MonadicActions {

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

}
