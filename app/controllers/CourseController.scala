package controllers

import javax.inject.Inject

import dao.{FacultyDAO, RoleDAO, UserDAO, CourseDAO}
import jp.t2v.lab.play2.auth.AuthElement
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * Created by chinhnk on 2/19/16.
  */
class CourseController @Inject()(courseDAO: CourseDAO, val userDAO: UserDAO, roleDAO: RoleDAO, facultyDAO: FacultyDAO,
                                 val messagesApi: MessagesApi) extends Controller with I18nSupport with AuthConfigImpl with AuthElement{

  //TODO: Insert into database ->
  private def authorityList()(user: User) : Future[Boolean] = roleDAO.findById(user.roleId).map(x => x.nonEmpty) //TODO: Except GUEST
  //TODO: Need to search and paging and order by
  def list = AsyncStack(AuthorityKey -> authorityList()) { implicit request =>
    val userLogin = loggedIn
    courseDAO.findByUserRole(userLogin.roleId,userLogin.userId).map { courses =>
      Ok(views.html.courses(courses, userLogin))
    }
  }
}
