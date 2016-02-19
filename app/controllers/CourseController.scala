package controllers

import javax.inject.Inject

import dao.{UserDAO, CourseDAO}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by chinhnk on 2/19/16.
  */
class CourseController @Inject()(courseDAO: CourseDAO, userDAO: UserDAO, val messagesApi: MessagesApi) extends Controller with I18nSupport{

  def getCourses = Action.async { implicit request =>
    courseDAO.findByCLId(10).zip(userDAO.findUserById(10)).map { case (courses, user) =>
      Ok(views.html.courses(courses, user))
    }
  }
}
