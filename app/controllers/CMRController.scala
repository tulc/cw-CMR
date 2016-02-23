package controllers

import javax.inject.Inject

import dao._
import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc.Controller
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import util.EmailUtil

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._


/**
  * Created by chinhnk on 2/19/16.
  */
class CMRController @Inject()(cmrDAO: CMRDAO, courseDAO: CourseDAO, val userDAO: UserDAO, roleDAO: RoleDAO,
                              gradeStatisticDAO: GradeStatisticDAO, gradeDistributionDAO: GradeDistributionDAO,
                              assessmentMethodDAO: AssessmentMethodDAO, emailUtil: EmailUtil, facultyDAO: FacultyDAO)
  extends Controller with AuthConfigImpl with AuthElement {

  def get(cmrId: Int) = AsyncStack(AuthorityKey -> roleDAO.authority("cmr-report.get")) { implicit request =>
    val userLogin = loggedIn
    val cmrPage = for {
      cmr <- cmrDAO.findCMRById(cmrId)
      course <- courseDAO.findById(cmr.head.courseId)
      clUser <- userDAO.findUserById(course.head.clId)
      statics <- gradeStatisticDAO.findByCMRId(cmr.head.cmrId)
      distributions <- gradeDistributionDAO.findByCMRId(cmr.head.cmrId)
      assessments <- assessmentMethodDAO.findAll
    } yield (cmr, course, clUser, statics, distributions, assessments)
    cmrPage.map(page =>
      Ok(views.html.reportDetail(page, userLogin))
    )
  }

  def add(courseId: String) = AsyncStack(AuthorityKey -> roleDAO.authority("cmr-report.add")) { implicit request =>
    val userLogin = loggedIn
    val checkExist = for {
      course <- courseDAO.findById(courseId)
      cmr <- cmrDAO.findCMRByCourseId(courseId)
    } yield (course, cmr)
    checkExist.map { case (course, cmr) =>
      if (course.isEmpty) {
        Redirect(routes.CourseController.list()).flashing("error" -> "Course Monitoring Report created failed. %s not exist".format(courseId))
      } else if (cmr.nonEmpty) {
        Redirect(routes.CourseController.list()).flashing("info" -> "Can not create Course Monitoring Report. Because CMR has been created", "link" -> routes.CMRController.get(cmr.head.cmrId).toString)
      } else {
        Await.result(cmrDAO.insertCMR(courseId, userLogin.userId), Duration(2, SECONDS))
        val cmrId = Await.result(cmrDAO.findMaxId(courseId, userLogin.userId), Duration(2, SECONDS))
        Redirect(routes.CourseController.list()).flashing("success" -> "Course Monitoring Report has been created", "link" -> routes.CMRController.get(cmrId).toString)
      }
    }
  }

  def delete(cmrId: Int) = AsyncStack(AuthorityKey -> roleDAO.authority("cmr-report.delete")) { implicit request =>
    cmrDAO.removeCMRById(cmrId).map(rowRemoved =>
      Ok(rowRemoved.toString)
    )
  }

  def submit(cmrId: Int) = AsyncStack(AuthorityKey -> roleDAO.authority("cmr-report.submit")) { implicit request =>
    val userLogin = loggedIn
    //TODO: Refactor here solution is : using form
    userLogin.roleId match {
      case "CL" => for {
        cmr <- cmrDAO.findCMRById(cmrId)
        course <- courseDAO.findById(cmr.head.courseId)
        faculty <- facultyDAO.findById(course.head.facultyId)
        cmUser <- userDAO.findUserById(course.head.cmId)
        email <- emailUtil.send(emailUtil.buildEmail(cmrId, "submitted", course.head.title, course.head.courseId,
          faculty.head.name, Seq(cmUser.head.email), "Course Leader " + userLogin.firstName + " " + userLogin.lastName))
      } yield ()
      case "CM" => for {
        cmr <- cmrDAO.findCMRById(cmrId)
        course <- courseDAO.findById(cmr.head.courseId)
        faculty <- facultyDAO.findById(course.head.facultyId)
        dltUser <- userDAO.findUserById(faculty.head.dltId)
        pvcUser <- userDAO.findUserById(faculty.head.pvcId)
        email <- emailUtil.send(emailUtil.buildEmail(cmrId, "approved", course.head.title, course.head.courseId,
          faculty.head.name, Seq(dltUser.head.email, pvcUser.head.email), "Course Moderator " + userLogin.firstName + " " + userLogin.lastName))
      } yield ()
      case "DLT" => for {
        cmr <- cmrDAO.findCMRById(cmrId)
        course <- courseDAO.findById(cmr.head.courseId)
        faculty <- facultyDAO.findById(course.head.facultyId)
        clUser <- userDAO.findUserById(course.head.clId)
        cmUser <- userDAO.findUserById(course.head.cmId)
        dltUser <- userDAO.findUserById(faculty.head.dltId)
        pvcUser <- userDAO.findUserById(faculty.head.pvcId)
        email <- emailUtil.send(emailUtil.buildEmail(cmrId, "commented", course.head.title, course.head.courseId,
          faculty.head.name, Seq(clUser.head.email, cmUser.head.email, dltUser.head.email, pvcUser.head.email), "Director of Learning and Quality " + userLogin.firstName + " " + userLogin.lastName))
      } yield ()
    }
    //TODO: Pass param comment string
    cmrDAO.updateStatusCMR(cmrId, userLogin.roleId, userLogin.userId).map { rowUpdated =>
      Ok(rowUpdated.toString)
    }
  }

  def list = AsyncStack(AuthorityKey -> roleDAO.authority("cmr-report.list")) { implicit request =>
    val userLogin = loggedIn
    cmrDAO.findByUser(userLogin.roleId, userLogin.userId).map(cmrs =>
      Ok(views.html.reportes(cmrs,userLogin))
    )
  }
}
