package controllers

import java.util.Date
import javax.inject.Inject

import dao._
import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import util.EmailUtil

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._


/**
  * Created by chinhnk on 2/19/16.
  */
class CMRController @Inject()(cmrDAO: CMRDAO, courseDAO: CourseDAO, val userDAO: UserDAO, roleDAO: RoleDAO,
                              gradeStatisticDAO: GradeStatisticDAO, gradeDistributionDAO: GradeDistributionDAO,
                              assessmentMethodDAO: AssessmentMethodDAO,emailUtil: EmailUtil, facultyDAO:FacultyDAO)
  extends Controller with AuthConfigImpl with AuthElement {

  private def authority(functionName: String)(user: User) : Future[Boolean] = {
    functionName match{
      case "add" | "delete" => roleDAO.findById(user.roleId).map(r => r.head.roleId == "CL") //only CL can call add function
      case "get" | "list" => roleDAO.findById(user.roleId).map(r => r.nonEmpty) //TODO: except GUEST and ADMIN
      case "submit" => roleDAO.findById(user.roleId).map(r => r.head.roleId == "CL" || r.head.roleId == "CM" || r.head.roleId == "DLT")
    }
  }

  def get(cmrId: Int) = AsyncStack(AuthorityKey -> authority("get")) { implicit request =>
    val userLogin = loggedIn
    val cmrPage = for {
      cmr <- cmrDAO.findCMRById(cmrId)
      course <- courseDAO.findById(cmr.head.courseId)
      clUser <- userDAO.findUserById(course.head.clId)
      statics <- gradeStatisticDAO.findByCMRId(cmr.head.cmrId)
      distributions <- gradeDistributionDAO.findByCMRId(cmr.head.cmrId)
      assessments <- assessmentMethodDAO.findAll
    } yield (cmr, course, clUser,statics,distributions,assessments)
    cmrPage.map(page =>
      Ok(views.html.reportDetail(page, userLogin))
    )
  }

  def add(courseId: String) = AsyncStack(AuthorityKey -> authority("add")) { implicit request =>
    val userLogin = loggedIn
    val checkExist = for {
      course <- courseDAO.findById(courseId)
      cmr <- cmrDAO.findCMRByCourseId(courseId)
    } yield (course, cmr)
    checkExist.map { case (course, cmr) =>
      if (course.isEmpty) {
        Redirect(routes.CourseController.list()).flashing("error" -> "Course Monitoring Report created failed. %s not exist".format(courseId))
      } else if (cmr.nonEmpty) {
        Redirect(routes.CourseController.list()).flashing("info" -> "Can not create Course Monitoring Report. Because CMR has been created", "link" -> "/report/%s".format(cmr.head.cmrId))
      } else {
        Await.result(cmrDAO.insertCMR(courseId, userLogin.userId), Duration(2, SECONDS))
        val cmrId = Await.result(cmrDAO.findMaxId(courseId,userLogin.userId),Duration(2, SECONDS))
        Redirect(routes.CourseController.list()).flashing("success" -> "Course Monitoring Report has been created", "link" -> "/report/%s".format(cmrId))
      }
    }
  }

  def delete(cmrId: Int) = AsyncStack(AuthorityKey -> authority("delete")) { implicit request =>
    cmrDAO.removeCMRById(cmrId).map(rowRemoved =>
      Ok(rowRemoved.toString)
    )
  }

  def submit(cmrId: Int) = AsyncStack(AuthorityKey -> authority("submit")) { implicit request =>
    val userLogin = loggedIn
    userLogin.roleId match {
      case "CL" => for {
        cmr <- cmrDAO.findCMRById(cmrId)
        course <- courseDAO.findById(cmr.head.courseId)
        faculty <- facultyDAO.findById(course.head.facultyId)
        cmUser <- userDAO.findUserById(course.head.cmId)
        email <- emailUtil.send(emailUtil.buildEmail(cmrId, "submitted", course.head.title, course.head.courseId,
          faculty.head.name, Seq(cmUser.head.email), "course leader " + userLogin.firstName+ " " +userLogin.lastName))
      } yield ()
      case "CM" => for {
        cmr <- cmrDAO.findCMRById(cmrId)
        course <- courseDAO.findById(cmr.head.courseId)
        faculty <- facultyDAO.findById(course.head.facultyId)
        dltUser <- userDAO.findUserById(faculty.head.dltId)
        pvcUser <- userDAO.findUserById(faculty.head.pvcId)
        email <- emailUtil.send(emailUtil.buildEmail(cmrId, "approved", course.head.title, course.head.courseId,
          faculty.head.name, Seq(dltUser.head.email, pvcUser.head.email), "course moderator " +userLogin.firstName+ " " +userLogin.lastName))
      } yield ()
    }

    cmrDAO.updateStatusCMR(cmrId,userLogin.roleId, userLogin.userId).map { rowUpdated =>
      Ok(rowUpdated.toString)
    }
  }

  def list = AsyncStack(AuthorityKey -> authority("list")) { implicit request =>
    //TODO: CL => View CMRs what have COURSE belong them AND have status == Commented
    //TODO: CM => View CMRs what have COURSE belong them AND have status == Commented
    //TODO: DLT & PVC => View all CMRs what have FACULTY belong them
    val userLogin = loggedIn
    cmrDAO.findByUser(userLogin.roleId, userLogin.userId).map(cmrs =>
      Ok(views.html.reportes(cmrs))
    )
  }
}
