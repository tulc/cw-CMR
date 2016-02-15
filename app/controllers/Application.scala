package controllers

import java.util.concurrent.Future
import javax.inject.Inject

import dao._
import models.{GradeDistribution, GradeStatistic, CMR}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent

class Application @Inject()(courseDAO: CourseDAO, facultyDAO: FacultyDAO, userDAO: UserDAO, cmrDAO: CMRDAO,
                            gradeDistributionDAO: GradeDistributionDAO, gradeStatisticDAO: GradeStatisticDAO) extends Controller {

  def index = Action { request =>
    Ok(views.html.index("Ok"))
  }

  def getCourseReports = Action {
    Ok(views.html.courseReports("Ok"))
  }

  def getCourses = Action.async { implicit request =>
    courseDAO.findByCLId(10).zip(userDAO.findUserById(10)).map { case (courses, user) =>
      Ok(views.html.courses(courses, user))
    }
  }

  def getReport(cmrId: Int) = Action.async { implicit request =>
    ???
  }

  def createReport(courseId: String) = Action.async { implicit request =>
    cmrDAO.findCMRByCourseId(courseId).zip(courseDAO.findById(courseId)).map { case (cmr, course) =>
      if (course == null) Redirect(routes.Application.getCourses()) //Course not found
      else {
        if (cmr != null) Redirect(routes.Application.getCourses()) //Exits cmr with @courseId
        else {
          cmrDAO.insertAndReturnCMRId(courseId, 10).map(cmrId =>
            Redirect(routes.Application.getReport(cmrId.head)) // create cmr success
          )
          Redirect(routes.Application.getCourses()) //Error in CMR process
        }
      }
    }
  }
}
