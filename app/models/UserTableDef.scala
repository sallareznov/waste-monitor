package models

import slick.lifted.Tag
import slick.model.Table

class UserTableDef(tag: Tag) extends Table[User](tag, "users") {

  def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
  def username = column[String]("username")
  def password = column[String]("password")
  def tokenId = column[String]("token_id")

  override def * = (id, username, password, tokenId) <> (User.tupled, User.unapply)
}