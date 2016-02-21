package controllers

import javax.inject.Inject

import dao.UserDAO
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import jp.t2v.lab.play2.stackc.{Attribute, RequestWithAttributes}
import models.Role._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future


class Application @Inject()(val userDAO: UserDAO,val messagesApi: MessagesApi) extends Controller
  with LoginLogout with AuthConfigImpl with I18nSupport with AuthElement {

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

  def index = StackAction(AuthorityKey -> CL) { implicit request =>
    Ok(views.html.index("Ok"))
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded
  }
}
