package controllers

import javax.inject.Inject

import dao.{FacultyDAO, RoleDAO, UserDAO, CourseDAO}
import jp.t2v.lab.play2.auth.AuthElement
import models.Course
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import java.sql.Date

/**
  * Created by chinhnk on 2/19/16.
  */
class CourseController @Inject()(courseDAO: CourseDAO, val userDAO: UserDAO, roleDAO: RoleDAO, facultyDAO: FacultyDAO,
                                 val messagesApi: MessagesApi) extends Controller with I18nSupport
  with AuthConfigImpl with AuthElement{

  val courseForm = Form(
    mapping(
      "courseId" -> nonEmptyText,
      "title" -> nonEmptyText,
      "academicYear" -> number(1900,3000),
      "studentNumber" -> number(0,120),
      "createDate" -> default(sqlDate("yyyy-MM-dd"), new Date((new java.util.Date).getTime)),
      "startDate" -> sqlDate("yyyy-MM-dd"),
      "endDate" -> sqlDate("yyyy-MM-dd"),
      "facultyId" -> number,
      "clId" -> number,
      "cmId" -> number
    )(Course.apply)(Course.unapply)
  )

  //TODO: Need to search and paging and order by
  def list = AsyncStack(AuthorityKey -> roleDAO.authority("courses.list")) { implicit request =>
    val userLogin = loggedIn
    courseDAO.findByUserRole(userLogin.roleId,userLogin.userId).map { courses =>
      Ok(views.html.courses(courses, userLogin))
    }
  }

  def create = AsyncStack(AuthorityKey -> roleDAO.authority("courses.create")) { implicit request =>
    val userLogin = loggedIn
    val createCourseData = for{
      faculties <- facultyDAO.findAll
      userCM <- userDAO.findByRole("CL")
      userCL <- userDAO.findByRole("CM")
    } yield (faculties, userCM, userCL)
    createCourseData.map(data =>
      Ok(views.html.createCourse(courseForm, data ,userLogin))
    )
  }

  def save = AsyncStack(AuthorityKey -> roleDAO.authority("courses.save")) { implicit request =>
    val userLogin = loggedIn
    val createCourseData = for{
      faculties <- facultyDAO.findAll
      userCL <- userDAO.findByRole("CL")
      userCM <- userDAO.findByRole("CM")
    } yield (faculties, userCM, userCL)

    courseForm.bindFromRequest.fold(
      formWithError => {
        createCourseData.map(data => BadRequest(views.html.createCourse(formWithError, data ,userLogin)))
      },
      course => {
        courseDAO.findById(course.courseId).map{ isCourseExist =>
          if(isCourseExist.isEmpty){
            courseDAO.insert(course)
            Redirect(routes.CourseController.create()).flashing("success" -> "Course %s has been created".format(course.courseId))
          }else{
            Redirect(routes.CourseController.create()).flashing("error" -> "CourseId %s is existed".format(course.courseId))
          }
        }
      }
    )
  }
}
