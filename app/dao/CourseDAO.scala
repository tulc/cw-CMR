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

trait CoursesComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
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
  }
}

@Singleton
class CourseDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
  with FacultiesComponent with UsersComponent with CoursesComponent{
  import driver.api._

  private lazy val courses = TableQuery[Courses]
  private lazy val faculties = TableQuery[Faculties]
  private lazy val users = TableQuery[Users]

  def findById(id:String): Future[Option[Course]] = db.run(courses.filter(_.courseId === id).result.headOption)

  def findByUserRole(roleId: String, userId: Int): Future[Seq[(Course,Option[Faculty],Option[User],Option[User])]] = {
    val clQuery = for {
      (((course,faculty),userCM), userCL) <- courses.filter(_.clId === userId)
        .joinLeft(faculties).on(_.facultyId === _.facultyId)
        .joinLeft(users).on(_._1.cmId === _.userId)
        .joinLeft(users).on(_._1._1.clId === _.userId)
    } yield (course,faculty, userCM, userCL)
    val cmQuery = for {
      (((course,faculty),userCM), userCL) <- courses.filter(_.cmId === userId).sortBy(_.endDate.desc)
        .joinLeft(faculties).on(_.facultyId === _.facultyId)
        .joinLeft(users).on(_._1.cmId === _.userId)
        .joinLeft(users).on(_._1._1.clId === _.userId)
    } yield (course,faculty, userCM, userCL)
    val dltQuery = for {
      (((course,faculty),userCM), userCL) <- courses
        .joinLeft(faculties.filter(_.dltId === userId)).on(_.facultyId === _.facultyId)
        .joinLeft(users).on(_._1.cmId === _.userId)
        .joinLeft(users).on(_._1._1.clId === _.userId)
    } yield (course,faculty, userCM, userCL)
    val pvcQuery = for {
      (((course,faculty),userCM), userCL) <- courses
        .joinLeft(faculties.filter(_.pvcId === userId)).on(_.facultyId === _.facultyId)
        .joinLeft(users).on(_._1.cmId === _.userId)
        .joinLeft(users).on(_._1._1.clId === _.userId)
    } yield (course,faculty, userCM, userCL)
    roleId match {
      case ("CL") => db.run(clQuery.sortBy(_._1.endDate.desc).result)
      case ("CM") => db.run(cmQuery.sortBy(_._1.endDate.desc).result)
      case ("DLT") => db.run(dltQuery.sortBy(_._1.endDate.desc).result)
      case ("PVC") => db.run(pvcQuery.sortBy(_._1.endDate.desc).result)
    }
  }
}
