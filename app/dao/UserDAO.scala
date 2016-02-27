package dao

import java.sql.Date
import javax.inject.{Inject, Singleton}

import models.User
import org.mindrot.jbcrypt.BCrypt
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

import play.api.libs.concurrent.Execution.Implicits.defaultContext

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
    def roleId = column[String]("RoleId")

    def * = (userId,firstName,lastName,email,password,createDate,isActive,roleId) <> ((User.apply _).tupled, User.unapply _)
  }
}

@Singleton
class UserDAO @Inject()(protected val dbConfigProvider:DatabaseConfigProvider) extends UsersComponent with HasDatabaseConfigProvider[JdbcProfile]{
  import driver.api._

  private lazy val users = TableQuery[Users]

  def findUserById(userId: Int): Future[Option[User]] = db.run(users.filter(_.userId === userId).result.headOption)

  def authenticate(email:String, password:String) : Option[User] =
    Await.result(findByEmail(email).map{u => u.filter(user => BCrypt.checkpw(password, user.password))}, Duration(2, SECONDS))

  def findByEmail(email: String): Future[Option[User]] = db.run(users.filter(_.email === email).result.headOption)

  def findByRole(roleId : String) : Future[Seq[User]] = db.run(users.filter(_.roleId === roleId).result)
}
