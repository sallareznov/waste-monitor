package controllers

import javax.inject.Inject

import models.{UserSignInData, UsersRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext

/**
  * Controller of the home page ({{{/}}})
  */
class WebsiteHomeController @Inject()(usersDAL: UsersRepository, val messagesApi: MessagesApi)
                                     (implicit ec: ExecutionContext) extends Controller with I18nSupport {

  val signInForm = Form(
    mapping(
      "username" -> text(minLength = 3, maxLength = 20),
      "password" -> text(minLength = 6, maxLength = 30)
    )(UserSignInData.apply)(UserSignInData.unapply)
      //.verifying("Invalid credentials", data => true)
  )

  /**
    * {{{GET /}}}
    *
    * @return renders the home page, enabling the user to log in, or to create an account
    */
  def index() = Action {
    Ok(views.html.index(signInForm))
  }

  def signInPost() = Action {
    implicit request =>
      signInForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.index(formWithErrors))
        },
        userData => {
          // TODO
          println(userData)
          usersDAL.listAll()
          Ok("HW")
        })
  }

}
