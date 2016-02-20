package controllers

import java.util.Date
import javax.inject.Inject

import dao._
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import util.EmailUtil

import scala.concurrent.Await
import scala.concurrent.duration._


/**
  * Created by chinhnk on 2/19/16.
  */
class CMRController @Inject()(cmrDAO: CMRDAO, courseDAO: CourseDAO, userDAO: UserDAO, roleDAO: RoleDAO,
                              gradeStatisticDAO: GradeStatisticDAO, gradeDistributionDAO: GradeDistributionDAO,
                              assessmentMethodDAO: AssessmentMethodDAO,emailUtil: EmailUtil) extends Controller {

  def get(cmrId: Int) = Action.async { implicit request =>
    //Get user role in session
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

  def add(courseId: String) = Action.async { implicit request =>
    //Get use in session
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

  def delete(cmrId: Int) = Action.async { implicit request =>
    cmrDAO.removeCMRById(cmrId).map(rowRemoved =>
      Ok(rowRemoved.toString)
    )
  }

  //TODO: CL submitCMR to CL
  def submit(cmrId: Int) = Action.async { implicit request =>
    //Get user role in session if user role is CL -> Submitted
    val process = for{
      rowUpdated <- cmrDAO.updateStatusCMR(cmrId,"Submitted")
      cmr <- cmrDAO.findCMRById(cmrId)
      course <- courseDAO.findById(cmr.head.courseId)
      userCM <- userDAO.findUserById(course.head.cmId)
      email <- emailUtil.sendEmail(s"""Notification about Course no.${course.head.courseId} - ${course.head.title} has been submitted""",userCM.head,cmrId)
    } yield (rowUpdated)
    process.map(rowUpdated =>
      Ok(rowUpdated.toString)
    )
  }

  //TODO: Get all reports by role who can view
  def getCMRReports = Action.async { implicit request =>
    //Get use in session
    ???
  }
}
