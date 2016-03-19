package dao

import java.sql.Date
import java.util
import javax.inject.{Inject, Singleton}

import models.{Course, AcademicSeason, InfoCourseEachAcademicSeason}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by chinhnk on 3/8/16.
  */
trait InfoCourseEachAcademicSeasonComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  class InfoCourseEachAcademicSeasons(tag: Tag) extends Table[InfoCourseEachAcademicSeason](tag, "InfoCourseEachAcademicSeason") {
    def courseId = column[String]("CourseId")

    def academicSeasonId = column[Int]("AcademicSeasonId")

    def studentNumber = column[Int]("StudentNumber")

    def clId = column[Int]("CLId")

    def cmId = column[Int]("CMId")

    def * = (courseId, academicSeasonId, studentNumber, clId.?, cmId.?) <>((InfoCourseEachAcademicSeason.apply _).tupled, InfoCourseEachAcademicSeason.unapply _)
  }

}

@Singleton
class InfoCourseEachAcademicSeasonDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
  with InfoCourseEachAcademicSeasonComponent with AcademicSeasonComponent with CoursesComponent {

  import driver.api._

  private lazy val infoCourseEachAcademicSeasons = TableQuery[InfoCourseEachAcademicSeasons]
  private lazy val courses = TableQuery[Courses]
  private lazy val academicSeasons = TableQuery[AcademicSeasons]

  def findByPrimaryKey(courseId: String, academicId: Int): Future[Option[InfoCourseEachAcademicSeason]] =
    db.run(infoCourseEachAcademicSeasons.filter(info => info.academicSeasonId === academicId && info.courseId === courseId).result.headOption)

  def insert(infoCourseEachAcademicSeason: InfoCourseEachAcademicSeason): Future[Int] = {
    db.run(this.infoCourseEachAcademicSeasons += infoCourseEachAcademicSeason)
  }

  def findAllAvaiable: Future[Seq[(InfoCourseEachAcademicSeason, Option[Course], Option[AcademicSeason])]] = {
    val query = for {
      ((info, courses), academics) <- infoCourseEachAcademicSeasons
        .joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(academicSeasons.filter(_.endDate > new Date(new util.Date().getTime))).on(_._1.academicSeasonId === _.academicSeasonId)
    } yield (info, courses, academics)
    db.run(query.result)
  }

  def updateStudentCount(courseId: String, academicId: Int, studentCount: Int): Future[Int] = {
    for {
      current <- findByPrimaryKey(courseId, academicId)
      number <- current.map { currentInfo =>
        db.run(infoCourseEachAcademicSeasons.filter(i => i.courseId === courseId && i.academicSeasonId === academicId).map(_.studentNumber).update(currentInfo.studentNumber + studentCount))
      }.getOrElse(Future.successful(0))
    } yield (number)
  }
}