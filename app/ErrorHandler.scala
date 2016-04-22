import models.ErrorJSONMessage
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.Future


class ErrorHandler extends HttpErrorHandler {

  def onServerError(request: RequestHeader, exception: Throwable) = {
    Future.successful(InternalServerError(Json.toJson(ErrorJSONMessage("A server error occurred: " + exception.getMessage))))
  }

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(Status(statusCode)(Json.toJson(ErrorJSONMessage("A client error occurred: " + message))))
  }

}
