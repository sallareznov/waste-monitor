package controllers.website

import javax.inject.Inject

import com.github.t3hnar.bcrypt._
import models.form.UserValidationData
import models.repository.{UserRepository, TokenRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}

class LoginController @Inject()(usersRepository: UserRepository, tokenRepository: TokenRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  val signInForm = Form(
    mapping(
      "username" -> text,
      "password" -> text
    )(UserValidationData.apply)(UserValidationData.unapply)
    //.verifying("Invalid credentials", data => true)
  )

  /**
    * Entry point of the website
 *
    * @return renders the home page, enabling the user to log in, or to create an account
    */
  def index() = Action {
    Ok(views.html.index(signInForm))
  }

  /**
    * @return attempt to log in a user
    */
  def loginPost() = Action.async {
    implicit request =>
      signInForm.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(views.html.index(formWithErrors))),
        userData =>
          usersRepository.getByUsername(userData.username).map({
            case Some(user) if userData.password.isBcrypted(user.hash) => Ok("It works!")
            case None => Unauthorized(views.html.index(signInForm))
          })(ec)
      )
  }

}
