package controllers

import play.api.mvc.{Action, Controller}

/**
  * Controller of the home page ({{{/}}})
  */
class HomeController extends Controller {

  /**
    * {{{GET /}}}
    * @return renders the home page, enabling the user to log in, or to create an account
    */
  def index() = Action {
    Ok(views.html.index.render())
  }

}
