package controllers

import javax.inject.Inject

import dao.{RoleDAO, UserDAO}
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.{Await, Future}


class Application @Inject()(val userDAO: UserDAO, roleDAO: RoleDAO, val messagesApi: MessagesApi) extends Controller
  with LoginLogout with AuthConfigImpl with I18nSupport with AuthElement {

  //authority all user have role active
  private def authorityIndex()(user: User) : Future[Boolean] = roleDAO.findById(user.roleId).map(x => x.nonEmpty)

  val loginForm = Form (
    mapping("email" -> email, "password" -> nonEmptyText
    )(userDAO.authenticate)(_.map(u => (u.email,"")))
      .verifying("The email and password you entered don't match. Please try again", result => result.isDefined)
  )

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.login(formWithErrors))),
      user => gotoLoginSucceeded(user.get.userId)
    )
  }

  def index = StackAction(AuthorityKey -> authorityIndex()) { implicit request =>
    Ok(views.html.index("Ok"))
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded
  }
}
