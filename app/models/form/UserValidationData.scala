package models.form

import play.api.libs.json.Json

/**
  * Data for validation
 *
  * @param username the username
  * @param password the password
  */
case class UserValidationData(username: String, password: String)

object UserValidationData {

  implicit val userValidationDataFormat = Json.format[UserValidationData]

}
