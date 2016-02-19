package dao

import javax.inject.{Singleton, Inject}

import models.{User, Faculty, Course}
import java.sql.Date
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * Created by chinhnk on 2/12/16.
  */

trait CoursesComponent { self: HasDatabaseConfigProvider[JdbcProfile] with FacultiesComponent with UsersComponent =>
  import driver.api._

  class Courses(tag: Tag) extends Table[Course](tag, "Course") {
    def courseId = column[String]("CourseId", O.PrimaryKey)
    def title = column[String]("Title")
    def academicSession = column[String]("AcademicSession")
    def studentNumber = column[Int]("StudentNumber")
    def createDate = column[Date]("CreateDate")
    def startDate = column[Date]("StartDate")
    def endDate = column[Date]("EndDate")
    def facultyId = column[Int]("FacultyId")
    def clId = column[Int]("CLId")
    def cmId = column[Int]("CMId")

    def * = (courseId,title , academicSession, studentNumber, createDate, startDate, endDate, facultyId, clId, cmId) <>((Course.apply _).tupled, Course.unapply _)

    def faculty = foreignKey("Faculty", facultyId, faculties)(_.facultyId)
    def userCL = foreignKey("User", clId, users)(_.userId)
    def userCM = foreignKey("User", cmId, users)(_.userId)
  }

  private lazy val faculties = TableQuery[Faculties]
  private lazy val users = TableQuery[Users]

}

@Singleton
class CourseDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
  with FacultiesComponent with UsersComponent with CoursesComponent{
  import driver.api._

  private lazy val courses = TableQuery[Courses]
  private lazy val faculties = TableQuery[Faculties]
  private lazy val users = TableQuery[Users]

  def findByCLId(id: Int): Future[Seq[(Course,Faculty,User)]] = {
    val query = (for (course <- courses if course.clId === id;
      faculty <- faculties if faculty.facultyId === course.facultyId;
      courseModerator <- users if courseModerator.userId === course.cmId
    ) yield (course,faculty,courseModerator))
    db.run(query.sortBy(_._1.endDate.desc).result)
  }

  def findById(id:String): Future[Option[Course]] = db.run(courses.filter(_.courseId === id).result.headOption)
}
