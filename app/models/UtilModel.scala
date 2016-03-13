package models

import java.sql.Date

import play.api.libs.json.Json
import slick.jdbc.GetResult

/**
  * Created by chinhnk on 3/12/16.
  */
case class PercentageCompletedCMR(academicSeasonName: String, facultyName: String, percent: Float)

case class PercentageResponseCMR(facultyName: String, percent: Float)

case class CourseWithoutCLCM(academicSeasonName: String, courseId: String, title: String, startDate: Date,
                             endDate: Date, studentNumber: Int, clId: Option[Int], cmId: Option[Int])

case class CourseWithoutCMR(academicSeasonName: String, courseId: String, title: String, facultyName: String, startDate: Date,
                            endDate: Date, status: Option[String])

case class CMRWithoutResponse(academicSeasonName: String, title: String, status: String,
                              createdDate: Date, submittedDate: Date, approvedDate: Date, cmrId: Int)

object PercentageCompletedCMR {
  implicit val percentageCompletedCMRResult = GetResult(r => PercentageCompletedCMR(r.<<, r.<<, r.<<))
}

object PercentageResponseCMR {

  implicit val percentageResponseCMRResult = GetResult(r => PercentageResponseCMR(r.<<, r.<<))
}

object CourseWithoutCLCM {
  implicit val CourseWithoutCLCMResult = GetResult(r => CourseWithoutCLCM(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))
}

object CourseWithoutCMR {
  implicit val CourseWithoutCLCMResult = GetResult(r => CourseWithoutCMR(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))
}

object CMRWithoutResponse {
  implicit val CMRWithoutResponseResult = GetResult(r => CMRWithoutResponse( r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))
}