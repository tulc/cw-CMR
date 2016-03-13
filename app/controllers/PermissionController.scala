package controllers

import javax.inject.Inject

import dao.{PermissionDAO, RoleDAO, UserDAO}
import jp.t2v.lab.play2.auth.AuthElement
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.Controller
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by chinhnk on 3/14/16.
  */
class PermissionController @Inject()(val userDAO: UserDAO, roleDAO: RoleDAO, permissionDAO: PermissionDAO, val messagesApi: MessagesApi)
  extends Controller with I18nSupport with AuthConfigImpl with AuthElement {

  def list = AsyncStack(AuthorityKey -> roleDAO.authority("permission.list")) { implicit request =>
    val userLogin = loggedIn
    permissionDAO.findAll.map(permissions => Ok(views.html.permissions(permissions, userLogin)))
  }

}
