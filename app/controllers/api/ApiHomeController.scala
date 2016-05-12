package controllers.api

import javax.inject.Inject

import controllers.ActionDSL.MonadicActions
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class ApiHomeController @Inject()(val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport with MonadicActions {

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

  def generateChart() = TODO

  def indexOptions() = Action {
    implicit request => NoContent.withHeaders("Allow" -> "GET")
  }

}
