package dao

import javax.inject.{Singleton, Inject}

import models._
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

    def * = (courseId, title, facultyId) <>((Course.apply _).tupled, Course.unapply _)
  }
}

@Singleton
class CourseDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
  with FacultiesComponent with CMRComponent with UsersComponent with CoursesComponent
  with InfoCourseEachAcademicSeasonComponent with AcademicSeasonComponent {
  import driver.api._

  private lazy val courses = TableQuery[Courses]
  private lazy val faculties = TableQuery[Faculties]
  private lazy val users = TableQuery[Users]
  private lazy val infoCourseEachAcademicSeasons = TableQuery[InfoCourseEachAcademicSeasons]
  private lazy val academicSeasons = TableQuery[AcademicSeasons]
  private lazy val cmrs = TableQuery[CMRs]

  def findAll: Future[Seq[Course]] = db.run(courses.result)

  def findById(id: String): Future[Option[Course]] = db.run(courses.filter(_.courseId === id).result.headOption)

  def findByUserRole(roleId: String, userId: Int): Future[Seq[(InfoCourseEachAcademicSeason, Option[Course],
    Option[Faculty], Option[AcademicSeason], Option[User], Option[User], Option[Int])]] = {
    val clQuery = for {
      ((((((info, course), faculty), academic), userCL), userCM), cmr) <- infoCourseEachAcademicSeasons.filter(_.clId === userId)
        .joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(faculties).on(_._2.map(_.facultyId) === _.facultyId)
        .joinLeft(academicSeasons).on(_._1._1.academicSeasonId === _.academicSeasonId)
        .joinLeft(users).on(_._1._1._1.clId === _.userId)
        .joinLeft(users).on(_._1._1._1._1.cmId === _.userId)
        .joinLeft(cmrs).on((left, cmr) => (left._1._1._1._1._1.courseId === cmr.courseId) && (left._1._1._1._1._1.academicSeasonId === cmr.academicSeasonId))
    } yield (info, course, faculty, academic, userCL, userCM, cmr.map(_.cmrId))
    val cmQuery = for {
      ((((((info, course), faculty), academic), userCL), userCM), cmr) <- infoCourseEachAcademicSeasons.filter(_.cmId === userId)
        .joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(faculties).on(_._2.map(_.facultyId) === _.facultyId)
        .joinLeft(academicSeasons).on(_._1._1.academicSeasonId === _.academicSeasonId)
        .joinLeft(users).on(_._1._1._1.clId === _.userId)
        .joinLeft(users).on(_._1._1._1._1.cmId === _.userId)
        .joinLeft(cmrs).on((left, cmr) => (left._1._1._1._1._1.courseId === cmr.courseId) && (left._1._1._1._1._1.academicSeasonId === cmr.academicSeasonId))
    } yield (info, course, faculty, academic, userCL, userCM, cmr.map(_.cmrId))
    val dltQuery = for {
      ((((((info, course), faculty), academic), userCL), userCM),cmr) <- infoCourseEachAcademicSeasons
        .joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(faculties.filter(_.dltId === userId)).on(_._2.map(_.facultyId) === _.facultyId)
        .joinLeft(academicSeasons).on(_._1._1.academicSeasonId === _.academicSeasonId)
        .joinLeft(users).on(_._1._1._1.clId === _.userId)
        .joinLeft(users).on(_._1._1._1._1.cmId === _.userId)
        .joinLeft(cmrs).on((left, cmr) => (left._1._1._1._1._1.courseId === cmr.courseId) && (left._1._1._1._1._1.academicSeasonId === cmr.academicSeasonId))
    } yield (info, course, faculty, academic, userCL, userCM, cmr.map(_.cmrId))
    val pvcQuery = for {
      ((((((info, course), faculty), academic), userCL), userCM), cmr) <- infoCourseEachAcademicSeasons
        .joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(faculties.filter(_.pvcId === userId)).on(_._2.map(_.facultyId) === _.facultyId)
        .joinLeft(academicSeasons).on(_._1._1.academicSeasonId === _.academicSeasonId)
        .joinLeft(users).on(_._1._1._1.clId === _.userId)
        .joinLeft(users).on(_._1._1._1._1.cmId === _.userId)
        .joinLeft(cmrs).on((left, cmr) => (left._1._1._1._1._1.courseId === cmr.courseId) && (left._1._1._1._1._1.academicSeasonId === cmr.academicSeasonId))
    } yield (info, course, faculty, academic, userCL, userCM, cmr.map(_.cmrId))
    val admQuery = for {
      ((((((info, course), faculty), academic), userCL), userCM), cmr) <- infoCourseEachAcademicSeasons
        .joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(faculties).on(_._2.map(_.facultyId) === _.facultyId)
        .joinLeft(academicSeasons).on(_._1._1.academicSeasonId === _.academicSeasonId)
        .joinLeft(users).on(_._1._1._1.clId === _.userId)
        .joinLeft(users).on(_._1._1._1._1.cmId === _.userId)
        .joinLeft(cmrs).on((left, cmr) => (left._1._1._1._1._1.courseId === cmr.courseId) && (left._1._1._1._1._1.academicSeasonId === cmr.academicSeasonId))
    } yield (info, course, faculty, academic, userCL, userCM, cmr.map(_.cmrId))
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

  def insert(course: Course): Future[Int] = {
    db.run(this.courses += course)
  }
}
