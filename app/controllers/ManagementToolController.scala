package controllers

import javax.inject.Inject

import dao._
import jp.t2v.lab.play2.auth.AuthElement
import models.{InfoCourseEachAcademicSeason, AcademicSeason, Course}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.Controller
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * Created by chinhnk on 3/8/16.
  */
class ManagementToolController @Inject()(courseDAO: CourseDAO, val userDAO: UserDAO, roleDAO: RoleDAO, facultyDAO: FacultyDAO,
                                         val messagesApi: MessagesApi, academicSeasonDAO: AcademicSeasonDAO,
                                         infoCourseEachAcademicSeasonDAO: InfoCourseEachAcademicSeasonDAO)
  extends Controller with I18nSupport with AuthConfigImpl with AuthElement{

  val courseForm = Form(
    mapping(
      "courseId" -> nonEmptyText,
      "title" -> nonEmptyText,
      "facultyId" -> number
    )(Course.apply)(Course.unapply)
  )

  val academicSeasonForm = Form(
    mapping(
      "academicSeasonId" -> optional(number),
      "name" -> nonEmptyText,
      "startDate" -> sqlDate("yyyy-MM-dd"),
      "endDate" -> sqlDate("yyyy-MM-dd")
    )(AcademicSeason.apply)(AcademicSeason.unapply)
  )

  val infoCourseEachAcademicSeasonForm = Form(
    mapping(
      "courseId" -> nonEmptyText,
      "academicSeasonId" -> number,
      "studentNumber" -> number(0,120),
      "clId" -> optional(number),
      "cmId" -> optional(number)
    )(InfoCourseEachAcademicSeason.apply)(InfoCourseEachAcademicSeason.unapply)
  )

  private def loadPageData = {
    for {
      faculty <- facultyDAO.findAll
      course <- courseDAO.findAll
      academic <- academicSeasonDAO.findAll
      userCL <- userDAO.findByRole("CL")
      userCM <- userDAO.findByRole("CM")
    } yield (faculty,course,academic, userCL, userCM)
  }

  def courseCreate = AsyncStack(AuthorityKey -> roleDAO.authority("management.course.create")) { implicit request =>
    val userLogin = loggedIn

    loadPageData.map(data =>
      Ok(views.html.managementTools(courseForm, academicSeasonForm, infoCourseEachAcademicSeasonForm, data ,userLogin))
    )
  }
  //TODO: Find the way better than that
  def saveCourse = AsyncStack(AuthorityKey -> roleDAO.authority("management.course.save")) { implicit request =>
    val userLogin = loggedIn

    courseForm.bindFromRequest.fold(
      formWithError => {
        loadPageData.map(data => BadRequest(views.html.managementTools(formWithError, academicSeasonForm, infoCourseEachAcademicSeasonForm, data ,userLogin)))
      },
      course => {
        courseDAO.findById(course.courseId).map{ isCourseExist =>
          if(isCourseExist.isEmpty){
            courseDAO.insert(course)
            Redirect(routes.ManagementToolController.courseCreate()).flashing("success" -> "Course %s has been created".format(course.courseId))
          }else{
            Redirect(routes.ManagementToolController.courseCreate()).flashing("error" -> "CourseId %s is existed".format(course.courseId))
          }
        }
      }
    )
  }

  def saveAcademicSeasons = AsyncStack(AuthorityKey -> roleDAO.authority("management.academic.save")) { implicit request =>
    val userLogin = loggedIn

    academicSeasonForm.bindFromRequest.fold(
      formWithError => {
        loadPageData.map(data => BadRequest(views.html.managementTools(courseForm, formWithError, infoCourseEachAcademicSeasonForm, data ,userLogin)))
      },
      academicSeason => {
        academicSeasonDAO.insert(academicSeason)
        Future.successful{Redirect(routes.ManagementToolController.courseCreate()).flashing("success" -> "Academic seasons %s has been created".format(academicSeason.name))}
      }
    )
  }

  def saveInfoCourseEachAcademicSeasons = AsyncStack(AuthorityKey -> roleDAO.authority("management.assign-course-academic.save")) { implicit request =>
    val userLogin = loggedIn
    infoCourseEachAcademicSeasonForm.bindFromRequest.fold(
      formWithError => {
        loadPageData.map(data => BadRequest(views.html.managementTools(courseForm, academicSeasonForm, formWithError, data ,userLogin)))
      },
      info => {
        infoCourseEachAcademicSeasonDAO.findByPrimaryKey(info.courseId,info.academicSeasonId).map(checkInfoExist =>
          if(checkInfoExist.isEmpty){
            infoCourseEachAcademicSeasonDAO.insert(info)
            Redirect(routes.ManagementToolController.courseCreate()).flashing("success" -> "Assigned success")
          }else Redirect(routes.ManagementToolController.courseCreate()).flashing("error" -> "Assigned error. Because assigning between course and academic year has been available. Please check carefully")
        )
      }
    )
  }
}