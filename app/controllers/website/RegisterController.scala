package controllers.website

import javax.inject.Inject

import models.{UserRegisterData, UserRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Controller for the registering process ({{{/signup}}})
  *
  * @param messagesApi the messages (within the i18n context)
  */
class RegisterController @Inject()(usersRepository: UserRepository, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  /**
    * the form for the registering process
    */
  val registerForm = Form(
    mapping(
      "username" -> text(minLength = 3, maxLength = 20)
        .verifying("Username already exists", username => true)
        .verifying("The accepted characters for the username are : letters, numbers, . and _", _.matches("[a-zA-Z0-9._]+")),
      "passwords" -> tuple(
        "main" -> text(minLength = 6, maxLength = 30)
          .verifying("The accepted characters for the password are : letters and numbers.", _.matches("[a-zA-Z0-9]+")),
        "confirm" -> text
      ).verifying("Passwords must match", passwords => passwords._1 == passwords._2)
    )(UserRegisterData.apply)(UserRegisterData.unapply)
  )

  /**
    * {{{GET /signUp}}}
    *
    * @return renders the form enabling the user to sign up
    */
  def registerGet() = Action {
    Ok(views.html.register(registerForm))
  }

  /**
    * {{{POST /signUp}}}
    *
    * @return signs the user up if the form is valid, otherwise displays errors and re-sends the form
    */
  def registerPost() = Action.async {
    implicit request =>
      registerForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.register(formWithErrors)))
        },
        userData => {
          val username = userData.username
          usersRepository.getByUsername(username).map({
            case None =>
              usersRepository.add(username, userData.passwords._1)
              Redirect(routes.LoginController.index())
            case Some(_) => Conflict
          })
        })
  }


}
