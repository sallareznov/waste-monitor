package controllers

import javax.inject.Inject

import models.UsersRepository
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

class ApiHomeController @Inject()(usersDAL: UsersRepository, val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport{

  def listUsers() = Action.async {
    usersDAL.listAll().map(users => Ok(Json.toJson(users)))
  }

}
