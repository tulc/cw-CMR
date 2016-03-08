package controllers

import javax.inject.Inject

import dao.{CMRDAO, RoleDAO, UserDAO, CourseDAO}
import jp.t2v.lab.play2.auth.AuthElement
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by chinhnk on 2/19/16.
  */
class CourseController @Inject()(courseDAO: CourseDAO, cmrDAO: CMRDAO, val userDAO: UserDAO, roleDAO: RoleDAO, val messagesApi: MessagesApi)
  extends Controller with AuthConfigImpl with AuthElement with I18nSupport {

  //TODO: Need to search and paging and order by
  def list = AsyncStack(AuthorityKey -> roleDAO.authority("courses.list")) { implicit request =>
    val userLogin = loggedIn
    courseDAO.findByUserRole(userLogin.roleId, userLogin.userId.get).map { pageData =>
      Ok(views.html.courses(pageData, userLogin))
    }
  }
}