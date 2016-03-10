package controllers

import java.sql.Date
import javax.inject.Inject

import dao.{UserDAO, RoleDAO}
import jp.t2v.lab.play2.auth.AuthElement
import models.User
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.Controller
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by chinhnk on 3/9/16.
  */
class UserController @Inject()(val userDAO: UserDAO, roleDAO: RoleDAO, val messagesApi: MessagesApi)
  extends Controller with I18nSupport with AuthConfigImpl with AuthElement {

  val userForm = Form(
    mapping(
      "userId" -> optional(number),
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText,
      "createDate" -> optional(sqlDate("yyyy-MM-dd")),
      "isActive" -> nonEmptyText(1, 1),
      "roleId" -> nonEmptyText
    )(createApply)(_.map(u => (u.userId, u.firstName, u.lastName, u.email, "", u.createDate, u.isActive + "", u.roleId)))
      .verifying("Please try again", result => result.isDefined)
  )

  def createApply(userId: Option[Int], firstName: String, lastName: String, email: String, password: String,
                  createDate: Option[Date], isActive: String, roleId: String) =
    Option(User(userId, firstName, lastName, email, password, createDate, isActive.charAt(0), roleId))

  def create = AsyncStack(AuthorityKey -> roleDAO.authority("user.create")) { implicit request =>
    val userLogin = loggedIn
    roleDAO.list.map(listRole =>
      Ok(views.html.userCreateForm(userForm, listRole, userLogin))
    )
  }

  def list = AsyncStack(AuthorityKey -> roleDAO.authority("user.list")) { implicit request =>
    val userLogin = loggedIn
    userDAO.findAll.map(listUsers => Ok(views.html.users(listUsers, userLogin)))
  }

  def update(userId: Int) = AsyncStack(AuthorityKey -> roleDAO.authority("user.update")) { implicit request =>
    val userLogin = loggedIn

    userForm.bindFromRequest.fold(
      formWithError => {
        roleDAO.list.map(listRole => BadRequest(views.html.userEditForm(userId,formWithError, listRole, userLogin)))
      },
      user => {
        userDAO.update(userId, user.get).map(count =>
            if(count > 0){
              Redirect(routes.UserController.list()).flashing("success" -> "User has been updated")
            }else{
              Redirect(routes.UserController.list()).flashing("error" -> "User update failed")
            }
        )
      }
    )
  }

  def edit(userId: Int) = AsyncStack(AuthorityKey -> roleDAO.authority("user.edit")) { implicit request =>
    val userLogin = loggedIn

    val userForEdit = for {
      user <- userDAO.findUserById(userId)
      listRole <- roleDAO.list
    } yield (user, listRole)

    userForEdit.map{ case (user, listRole) =>
      user match {
        case Some(c) => Ok(views.html.userEditForm(userId, userForm.fill(user), listRole, userLogin))
        case None => NotFound(views.html.notFound())
      }
    }
  }

  def save = AsyncStack(AuthorityKey -> roleDAO.authority("user.save")) { implicit request =>
    val userLogin = loggedIn

    userForm.bindFromRequest.fold(
      formWithError => {
        roleDAO.list.map(listRole => BadRequest(views.html.userCreateForm(formWithError, listRole, userLogin)))
      },
      user => {
        userDAO.findByEmail(user.get.email).map(isEmailExist =>
          if (isEmailExist.isEmpty) {
            userDAO.insert(user.get)
            Redirect(routes.UserController.create()).flashing("success" -> "User has been created")
          } else {
            Redirect(routes.UserController.create()).flashing("error" -> "Email %s is existed".format(user.get.email))
          }
        )
      }
    )
  }

  def delete(userId: Int) = AsyncStack(AuthorityKey -> roleDAO.authority("user.delete")) { implicit request =>
    userDAO.delete(userId).map(u =>
      Redirect(routes.UserController.list()).flashing("success" -> "User has been deleted")
    )
  }
}
