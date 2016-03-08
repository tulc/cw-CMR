package dao

import javax.inject.{Inject, Singleton}

import models.InfoCourseEachAcademicSeason
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by chinhnk on 3/8/16.
  */
trait InfoCourseEachAcademicSeasonComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._

  class InfoCourseEachAcademicSeasons(tag: Tag) extends Table[InfoCourseEachAcademicSeason](tag, "InfoCourseEachAcademicSeason") {
    def courseId = column[String]("CourseId")
    def academicSeasonId = column[Int]("AcademicSeasonId")
    def studentNumber = column[Int]("StudentNumber")
    def clId = column[Int]("CLId")
    def cmId = column[Int]("CMId")

    def * = (courseId,academicSeasonId,studentNumber,clId.?,cmId.?) <> ((InfoCourseEachAcademicSeason.apply _).tupled, InfoCourseEachAcademicSeason.unapply _)
  }
}
@Singleton
class InfoCourseEachAcademicSeasonDAO @Inject()(protected val dbConfigProvider:DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
  with InfoCourseEachAcademicSeasonComponent{
  import driver.api._

  private lazy val infoCourseEachAcademicSeasons = TableQuery[InfoCourseEachAcademicSeasons]

  def findByPrimaryKey(courseId: String, academicId: Int): Future[Option[InfoCourseEachAcademicSeason]] =
    db.run(infoCourseEachAcademicSeasons.filter(info => info.academicSeasonId === academicId && info.courseId === courseId).result.headOption)

  def insert(infoCourseEachAcademicSeason: InfoCourseEachAcademicSeason): Future[Int] = {
    db.run(this.infoCourseEachAcademicSeasons += infoCourseEachAcademicSeason)
  }
}