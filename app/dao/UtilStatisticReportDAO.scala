package dao

import javax.inject.{Singleton, Inject}

import models.{PercentageResponseCMR, PercentageCompletedCMR}
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by chinhnk on 3/11/16.
  */

@Singleton
class UtilStatisticReportDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  def percentageCompletedCMRs: Future[Seq[PercentageCompletedCMR]] = {
    val query =
      sql"""SELECT AcademicSeason.Name, Faculty.Name,
              ISNULL(ROUND(CAST(COUNT(completeCMR.CMRId) AS FLOAT) / CAST(NULLIF(COUNT(allCMR.CMRId), 0) AS FLOAT), 2),
                     0) AS PercentageCMRCompleted
            FROM Faculty
              LEFT JOIN Course ON Faculty.FacultyId = Course.FacultyId
              LEFT JOIN InfoCourseEachAcademicSeason ON InfoCourseEachAcademicSeason.CourseId = Course.CourseId
              LEFT JOIN AcademicSeason ON InfoCourseEachAcademicSeason.AcademicSeasonId = AcademicSeason.AcademicSeasonId
              LEFT JOIN (SELECT CMR.CMRId, CMR.CourseId, CMR.AcademicSeasonId
                         FROM CMR
                         WHERE CMR.Status = 'Commented') completeCMR
                ON completeCMR.CourseId = InfoCourseEachAcademicSeason.CourseId AND
                   completeCMR.AcademicSeasonId = InfoCourseEachAcademicSeason.AcademicSeasonId
              LEFT JOIN (SELECT CMR.CMRId, CMR.CourseId, CMR.AcademicSeasonId
                         FROM CMR) allCMR
                ON allCMR.CourseId = InfoCourseEachAcademicSeason.CourseId AND
                   allCMR.AcademicSeasonId = InfoCourseEachAcademicSeason.AcademicSeasonId
            GROUP BY AcademicSeason.name, Faculty.Name, AcademicSeason.EndDate
            ORDER BY AcademicSeason.EndDate""".as[PercentageCompletedCMR]
    db.run(query)
  }

  def percentageResponse : Future[Seq[PercentageResponseCMR]] = {
    val query = sql"""SELECT Faculty.Name,
                        ISNULL(ROUND(CAST(COUNT(completeCMR.CMRId) AS FLOAT) / CAST(NULLIF(COUNT(allCMR.CMRId), 0) AS FLOAT), 2),
                               0) AS PercentageCMRCompleted
                      FROM Faculty
                        LEFT JOIN Course ON Faculty.FacultyId = Course.FacultyId
                        LEFT JOIN (SELECT CMR.CMRId, CMR.CourseId, CMR.AcademicSeasonId
                                   FROM CMR
                                   WHERE CMR.Status = 'Commented') completeCMR
                          ON completeCMR.CourseId = Course.CourseId
                        LEFT JOIN (SELECT CMR.CMRId, CMR.CourseId, CMR.AcademicSeasonId
                                   FROM CMR) allCMR
                          ON allCMR.CourseId = Course.CourseId
                      GROUP BY Faculty.Name""".as[PercentageResponseCMR]
    db.run(query)
  }
}
