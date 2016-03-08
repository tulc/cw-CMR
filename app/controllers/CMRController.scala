package controllers

import javax.inject.Inject

import dao._
import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc.Controller
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import util.EmailUtil

import scala.concurrent.Await
import scala.concurrent.duration._


/**
  * Created by chinhnk on 2/19/16.
  */
class CMRController @Inject()(cmrDAO: CMRDAO, courseDAO: CourseDAO, val userDAO: UserDAO, roleDAO: RoleDAO,
                              gradeStatisticDAO: GradeStatisticDAO, gradeDistributionDAO: GradeDistributionDAO,
                              assessmentMethodDAO: AssessmentMethodDAO, emailUtil: EmailUtil, facultyDAO: FacultyDAO,
                              academicSeasonDAO: AcademicSeasonDAO, infoCourseEachAcademicSeasonDAO: InfoCourseEachAcademicSeasonDAO)
  extends Controller with AuthConfigImpl with AuthElement {

  def get(cmrId: Int) = AsyncStack(AuthorityKey -> roleDAO.authority("cmr-report.get")) { implicit request =>
    val userLogin = loggedIn
    val cmrPage = for {
      cmr <- cmrDAO.findCMRById(cmrId)
      course <- courseDAO.findById(cmr.head.courseId)
      academicSeason <- academicSeasonDAO.findById(cmr.head.academicSeasonId)
      info <- infoCourseEachAcademicSeasonDAO.findByPrimaryKey(cmr.head.courseId, cmr.head.academicSeasonId)
      clUser <- userDAO.findUserById(info.head.clId.get)
      statics <- gradeStatisticDAO.findByCMRId(cmr.head.cmrId)
      distributions <- gradeDistributionDAO.findByCMRId(cmr.head.cmrId)
      assessments <- assessmentMethodDAO.findAll
    } yield (cmr, course, academicSeason, info, clUser, statics, distributions, assessments)
    cmrPage.map(page =>
      Ok(views.html.reportDetail(page, userLogin))
    )
  }

  def add(courseId: String, academicSeasonId: Int) = AsyncStack(AuthorityKey -> roleDAO.authority("cmr-report.add")) { implicit request =>
    val userLogin = loggedIn
    val checkExist = for {
      course <- courseDAO.findById(courseId)
      cmr <- cmrDAO.findCMRByCourseAcademicSeasonId(courseId, academicSeasonId)
    } yield (course, cmr)
    checkExist.map { case (course, cmr) =>
      if (course.isEmpty) {
        Redirect(routes.CourseController.list()).flashing("error" -> "Course Monitoring Report created failed. %s not exist".format(courseId))
      } else if (cmr.nonEmpty) {
        Redirect(routes.CourseController.list()).flashing("info" -> "Can not create Course Monitoring Report. Because CMR has been created", "link" -> routes.CMRController.get(cmr.head.cmrId).toString)
      } else {
        Await.result(cmrDAO.insertCMR(courseId, userLogin.userId.get, academicSeasonId), Duration(2, SECONDS))
        val cmrId = Await.result(cmrDAO.findMaxId(courseId, userLogin.userId.get, academicSeasonId), Duration(2, SECONDS))
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
        info <- infoCourseEachAcademicSeasonDAO.findByPrimaryKey(cmr.head.courseId, cmr.head.academicSeasonId)
        cmUser <- userDAO.findUserById(info.head.clId.get)
        email <- emailUtil.send(emailUtil.buildEmailNotifyNewAction(cmrId, "submitted", course.head.title, course.head.courseId,
          faculty.head.name, Seq(cmUser.head.email), "Course Leader " + userLogin.firstName + " " + userLogin.lastName))
      } yield ()
      case "CM" => for {
        cmr <- cmrDAO.findCMRById(cmrId)
        course <- courseDAO.findById(cmr.head.courseId)
        faculty <- facultyDAO.findById(course.head.facultyId)
        dltUser <- userDAO.findUserById(faculty.head.dltId)
        pvcUser <- userDAO.findUserById(faculty.head.pvcId)
        email <- emailUtil.send(emailUtil.buildEmailNotifyNewAction(cmrId, "approved", course.head.title, course.head.courseId,
          faculty.head.name, Seq(dltUser.head.email, pvcUser.head.email), "Course Moderator " + userLogin.firstName + " " + userLogin.lastName))
      } yield ()
      case "DLT" => for {
        cmr <- cmrDAO.findCMRById(cmrId)
        course <- courseDAO.findById(cmr.head.courseId)
        faculty <- facultyDAO.findById(course.head.facultyId)
        info <- infoCourseEachAcademicSeasonDAO.findByPrimaryKey(cmr.head.courseId, cmr.head.academicSeasonId)
        clUser <- userDAO.findUserById(info.head.clId.get)
        cmUser <- userDAO.findUserById(info.head.cmId.get)
        dltUser <- userDAO.findUserById(faculty.head.dltId)
        pvcUser <- userDAO.findUserById(faculty.head.pvcId)
        email <- emailUtil.send(emailUtil.buildEmailNotifyNewAction(cmrId, "commented", course.head.title, course.head.courseId,
          faculty.head.name, Seq(clUser.head.email, cmUser.head.email, dltUser.head.email, pvcUser.head.email), "Director of Learning and Quality " + userLogin.firstName + " " + userLogin.lastName))
      } yield ()
    }
    //TODO: Pass param comment string
    cmrDAO.updateStatusCMR(cmrId, userLogin.roleId, userLogin.userId.get).map { rowUpdated =>
      Ok(rowUpdated.toString)
    }
  }

  def list = AsyncStack(AuthorityKey -> roleDAO.authority("cmr-report.list")) { implicit request =>
    val userLogin = loggedIn
    cmrDAO.findByUser(userLogin.roleId, userLogin.userId.get).map(cmrs =>
      Ok(views.html.reportes(cmrs,userLogin))
    )
  }
}
