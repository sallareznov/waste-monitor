package models.form

/**
  * Informations the user has to fill in order to sign up
  * @param username the username provided by the user
  * @param passwords the password and its confirmation, provided by the user
  */
case class UserRegisterData(username: String, passwords: (String, String))
