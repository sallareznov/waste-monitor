package controllers.api

import javax.inject.Inject

import models.UserRepository
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

class ApiHomeController @Inject()(usersRepository: UserRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def listUsers() = Action.async {
    usersRepository.listAll().map(users => Ok(Json.toJson(users)))
  }

  def login() = TODO

}
