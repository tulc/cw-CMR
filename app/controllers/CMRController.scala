package controllers

import javax.inject.Inject

import dao.{CourseDAO, CMRDAO}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Await
import scala.concurrent.duration._


/**
  * Created by chinhnk on 2/19/16.
  */
class CMRController @Inject()(cmrDAO: CMRDAO, courseDAO: CourseDAO) extends Controller {

  def getCMRReport(cmrId: Int) = Action.async { implicit request =>
    cmrDAO.findCMRById(cmrId).map(cmr =>
      Ok(views.html.reportDetail(cmr.head))
    )
  }

  def addCMRReport(courseId: String) = Action.async { implicit request =>
    val checkExist = for {
      course <- courseDAO.findById(courseId)
      cmr <- cmrDAO.findCMRByCourseId(courseId)
    } yield (course, cmr)
    checkExist.map { case (course, cmr) =>
      if (course.isEmpty){
        Redirect(routes.CourseController.getCourses()).flashing("error" -> "Course Monitoring Report created failed. %s not exist".format(courseId))
      }else if(cmr.nonEmpty){
        Redirect(routes.CourseController.getCourses()).flashing("info" -> "Can not create Course Monitoring Report. Because CMR has been created","link" -> "/report/%s".format(cmr.head.cmrId))
      }else{
        val cmrId = Await.result(cmrDAO.insertAndReturnCMRId(courseId,10), Duration(2, SECONDS))
        Redirect(routes.CourseController.getCourses()).flashing("success" -> "Course Monitoring Report has been created", "link" -> "/report/%s".format(cmrId))
      }
    }
  }
}
