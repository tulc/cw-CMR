package dao

import javax.inject.{Singleton, Inject}

import models._
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
              ISNULL(ROUND(CAST(COUNT(completeCMR.CMRId) AS FLOAT) / CAST(NULLIF(COUNT(allCMR.CMRId), 0) AS FLOAT), 2) * 100,
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
                        ISNULL(ROUND(CAST(COUNT(completeCMR.CMRId) AS FLOAT) / CAST(NULLIF(COUNT(allCMR.CMRId), 0) AS FLOAT), 2) * 100,
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

  def courseWithoutCLCM(fromDate:String, toDate:String) : Future[Seq[CourseWithoutCLCM]] = {
    val query = sql"""SELECT AcademicSeason.Name, Course.CourseId, Course.Title, AcademicSeason.StartDate,
                    AcademicSeason.EndDate, InfoCourseEachAcademicSeason.StudentNumber,
                    InfoCourseEachAcademicSeason.CLId, InfoCourseEachAcademicSeason.CMId
                  FROM Course
                    JOIN InfoCourseEachAcademicSeason ON Course.CourseId = InfoCourseEachAcademicSeason.CourseId
                    JOIN AcademicSeason ON AcademicSeason.AcademicSeasonID = InfoCourseEachAcademicSeason.AcademicSeasonID
                  WHERE InfoCourseEachAcademicSeason.CLId IS NULL OR InfoCourseEachAcademicSeason.CMId IS NULL
                                                                     AND AcademicSeason.StartDate >= ${fromDate} AND
                                                                     AcademicSeason.EndDate <= ${toDate} ORDER BY academicseason.enddate""".as[CourseWithoutCLCM]
    db.run(query)
  }

  def courseWithoutCMR(fromDate:String, toDate: String) : Future[Seq[CourseWithoutCMR]] = {
   val query = sql"""SELECT academicseason.name, course.courseid, course.title, faculty.name,
                      academicseason.startdate, academicseason.enddate, cmr.status
                    FROM faculty
                      LEFT JOIN course ON faculty.facultyid = course.facultyid
                      LEFT JOIN InfoCourseEachAcademicSeason ON course.courseid = InfoCourseEachAcademicSeason.courseid
                      LEFT JOIN academicseason ON academicseason.academicseasonid = InfoCourseEachAcademicSeason.academicseasonid
                      LEFT JOIN cmr ON cmr.courseId = InfoCourseEachAcademicSeason.courseId AND
                                       cmr.academicseasonid = InfoCourseEachAcademicSeason.academicseasonid
                    WHERE cmr.status IS NULL AND academicseason.startDate >= ${fromDate} AND academicseason.enddate <= ${toDate}
                    ORDER BY academicseason.enddate""".as[CourseWithoutCMR]
    db.run(query)
  }

  def cmrWithoutResponse(fromDate: String, toDate:String): Future[Seq[CMRWithoutResponse]] = {
    val query = sql"""SELECT academicseason.name,  course.title, cmr.status,
                  cmr.createddate, cmr.submitteddate, cmr.approveddate, cmr.cmrId
                      FROM cmr
                        JOIN Course ON cmr.courseId = course.courseid
                        JOIN academicseason ON cmr.academicseasonid = academicseason.academicseasonid
                      WHERE cmr.status != 'Commented' AND cmr.submitteddate >= ${fromDate} AND cmr.submitteddate <= ${toDate}
                      ORDER BY cmr.submitteddate""".as[CMRWithoutResponse]
    db.run(query)
  }
}
