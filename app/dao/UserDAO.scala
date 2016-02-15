package dao

import java.sql.Date
import javax.inject.{Inject, Singleton}

import models.{User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by chinhnk on 2/12/16.
  */

trait UsersComponent{ self: HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._

  class Users(tag:Tag) extends Table[User](tag,"User"){
    def userId = column[Int]("UserId",O.PrimaryKey, O.AutoInc)
    def firstName = column[String]("FirstName")
    def lastName = column[String]("LastName")
    def email = column[String]("Email")
    def password = column[String]("Password")
    def createDate = column[Date]("CreateDate")
    def isActive = column[Char]("isActive")

    def * = (userId,firstName,lastName,email,password,createDate,isActive) <> ((User.apply _).tupled, User.unapply _)
  }

}

@Singleton
class UserDAO @Inject()(protected val dbConfigProvider:DatabaseConfigProvider) extends UsersComponent with HasDatabaseConfigProvider[JdbcProfile]{
  import driver.api._

  private lazy val users = TableQuery[Users]

  def findUserById(userId: Int): Future[Seq[User]] = db.run(users.filter(_.userId === userId).result)

//  def findAllUserByPlainSQL : Future[Seq[String]] = db.run(sql"SELECT userId FROM [User]".as[String])
}
