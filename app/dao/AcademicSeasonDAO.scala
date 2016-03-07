package dao

import java.sql.Date
import javax.inject.{Inject, Singleton}

import models.AcademicSeason
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by chinhnk on 3/8/16.
  */

trait AcademicSeasonComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._
  class AcademicSeasons(tag: Tag) extends Table[AcademicSeason](tag, "AcademicSeason") {
    def academicSeasonId = column[Int]("AcademicSeasonId", O.PrimaryKey, O.AutoInc)
    def name = column[String]("Name")
    def startDate = column[Date]("StartDate")
    def endDate = column[Date]("EndDate")

    def * = (academicSeasonId,name,startDate,endDate) <> ((AcademicSeason.apply _).tupled, AcademicSeason.unapply _)
  }
}

@Singleton
class AcademicSeasonDAO @Inject()(protected val dbConfigProvider:DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
  with AcademicSeasonComponent {
  import driver.api._

  private lazy val academicSeasons = TableQuery[AcademicSeasons]

  def findById(id: Int): Future[Option[AcademicSeason]] =
    db.run(academicSeasons.filter(_.academicSeasonId === id).result.headOption)
}
