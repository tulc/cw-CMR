package controllers

import javax.inject.Inject

import dao._
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Await
import scala.concurrent.duration._


/**
  * Created by chinhnk on 2/19/16.
  */
class CMRController @Inject()(cmrDAO: CMRDAO, courseDAO: CourseDAO, userDAO: UserDAO,
                              gradeStatisticDAO: GradeStatisticDAO, gradeDistributionDAO: GradeDistributionDAO,
                              assessmentMethodDAO: AssessmentMethodDAO) extends Controller {

  def getCMRReport(cmrId: Int) = Action.async { implicit request =>
    val cmrPage = for {
      cmr <- cmrDAO.findCMRById(cmrId)
      course <- courseDAO.findById(cmr.head.courseId)
      clUser <- userDAO.findUserById(course.head.clId)
      statics <- gradeStatisticDAO.findByCMRId(cmr.head.cmrId)
      distributions <- gradeDistributionDAO.findByCMRId(cmr.head.cmrId)
      assessments <- assessmentMethodDAO.findAll
    } yield (cmr, course, clUser,statics,distributions,assessments)
    cmrPage.map(page =>
      Ok(views.html.reportDetail(page))
    )
  }

  def addCMRReport(courseId: String) = Action.async { implicit request =>
    val checkExist = for {
      course <- courseDAO.findById(courseId)
      cmr <- cmrDAO.findCMRByCourseId(courseId)
    } yield (course, cmr)
    checkExist.map { case (course, cmr) =>
      if (course.isEmpty) {
        Redirect(routes.CourseController.getCourses()).flashing("error" -> "Course Monitoring Report created failed. %s not exist".format(courseId))
      } else if (cmr.nonEmpty) {
        Redirect(routes.CourseController.getCourses()).flashing("info" -> "Can not create Course Monitoring Report. Because CMR has been created", "link" -> "/report/%s".format(cmr.head.cmrId))
      } else {
        Await.result(cmrDAO.insertCMR(courseId, 10), Duration(2, SECONDS))
        val cmrId = Await.result(cmrDAO.findMaxId(courseId,10),Duration(2, SECONDS))
        Redirect(routes.CourseController.getCourses()).flashing("success" -> "Course Monitoring Report has been created", "link" -> "/report/%s".format(cmrId))
      }
    }
  }

  def removeCMRReport(cmrId: Int) = Action.async { implicit request =>
    cmrDAO.removeCMRById(cmrId).map(rowRemove =>
      Ok(rowRemove.toString)
    )
  }
  //TODO: complete submitCMR -> chage status -> submited -> sendEmail
  def submitCMR = Action.async { implicit request =>
    ???
  }
}
