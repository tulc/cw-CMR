package dao

import javax.inject.{Singleton, Inject}

import models.{User, Faculty, Course,InfoCourseEachAcademicSeason,AcademicSeason}
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by chinhnk on 2/12/16.
  */

trait CoursesComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._

  class Courses(tag: Tag) extends Table[Course](tag, "Course") {
    def courseId = column[String]("CourseId", O.PrimaryKey)
    def title = column[String]("Title")
    def facultyId = column[Int]("FacultyId")

    def * = (courseId,title, facultyId) <>((Course.apply _).tupled, Course.unapply _)
  }
}
//TODO need fix
@Singleton
class CourseDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
  with FacultiesComponent with UsersComponent with CoursesComponent with InfoCourseEachAcademicSeasonComponent with AcademicSeasonComponent{
  import driver.api._

  private lazy val courses = TableQuery[Courses]
  private lazy val faculties = TableQuery[Faculties]
  private lazy val users = TableQuery[Users]
  private lazy val infoCourseEachAcademicSeasons = TableQuery[InfoCourseEachAcademicSeasons]
  private lazy val academicSeasons = TableQuery[AcademicSeasons]

  def findById(id:String): Future[Option[Course]] = db.run(courses.filter(_.courseId === id).result.headOption)

  def findByUserRole(roleId: String, userId: Int): Future[Seq[(Course, Option[Faculty],
    Option[InfoCourseEachAcademicSeason], Option[AcademicSeason], Option[User], Option[User])]] = {
    val clQuery = for {
      (((((course, faculty), info), academicSeason),userCL), userCM) <- courses
        .joinLeft(faculties).on(_.facultyId === _.facultyId)
        .joinLeft(infoCourseEachAcademicSeasons.filter(_.clId === userId)).on(_._1.courseId === _.courseId)
        .joinLeft(academicSeasons).on(_._2.map(_.academicSeasonId) === _.academicSeasonId)
        .joinLeft(users).on(_._1._2.map(_.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(_.cmId) === _.userId)
    } yield (course,faculty,info, academicSeason, userCL, userCM)
    val cmQuery = for {
      (((((course, faculty), info), academicSeason),userCL), userCM) <- courses
        .joinLeft(faculties).on(_.facultyId === _.facultyId)
        .joinLeft(infoCourseEachAcademicSeasons.filter(_.cmId === userId)).on(_._1.courseId === _.courseId)
        .joinLeft(academicSeasons).on(_._2.map(_.academicSeasonId) === _.academicSeasonId)
        .joinLeft(users).on(_._1._2.map(_.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(_.cmId) === _.userId)
    } yield (course,faculty,info, academicSeason, userCL, userCM)
    val dltQuery = for {
      (((((course, faculty), info), academicSeason),userCL), userCM) <- courses
        .joinLeft(faculties.filter(_.dltId === userId)).on(_.facultyId === _.facultyId)
        .joinLeft(infoCourseEachAcademicSeasons).on(_._1.courseId === _.courseId)
        .joinLeft(academicSeasons).on(_._2.map(_.academicSeasonId) === _.academicSeasonId)
        .joinLeft(users).on(_._1._2.map(_.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(_.cmId) === _.userId)
    } yield (course,faculty,info, academicSeason, userCL, userCM)
    val pvcQuery = for {
      (((((course, faculty), info), academicSeason),userCL), userCM) <- courses
        .joinLeft(faculties.filter(_.pvcId === userId)).on(_.facultyId === _.facultyId)
        .joinLeft(infoCourseEachAcademicSeasons).on(_._1.courseId === _.courseId)
        .joinLeft(academicSeasons).on(_._2.map(_.academicSeasonId) === _.academicSeasonId)
        .joinLeft(users).on(_._1._2.map(_.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(_.cmId) === _.userId)
    } yield (course,faculty,info, academicSeason, userCL, userCM)
    val admQuery = for {
      (((((course, faculty), info), academicSeason),userCL), userCM) <- courses
        .joinLeft(faculties).on(_.facultyId === _.facultyId)
        .joinLeft(infoCourseEachAcademicSeasons).on(_._1.courseId === _.courseId)
        .joinLeft(academicSeasons).on(_._2.map(_.academicSeasonId) === _.academicSeasonId)
        .joinLeft(users).on(_._1._2.map(_.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(_.cmId) === _.userId)
    } yield (course,faculty,info, academicSeason, userCL, userCM)
    roleId match {
      case ("CL") => db.run(clQuery.sortBy(_._4.map(_.endDate).desc).result)
      case ("CM") => db.run(cmQuery.sortBy(_._4.map(_.endDate).desc).result)
      case ("DLT") => db.run(dltQuery.sortBy(_._4.map(_.endDate).desc).result)
      case ("PVC") => db.run(pvcQuery.sortBy(_._4.map(_.endDate).desc).result)
      case ("ADM") => db.run(admQuery.sortBy(_._4.map(_.endDate).desc).result)
    }
  }

  def findByAcademicYear(academic: Int): Future[Seq[Course]] = {
    //TODO:Do this
    db.run(courses.result)
  }

  def insert(course: Course) : Future[Int] = {
    db.run(this.courses += course)
  }
}
