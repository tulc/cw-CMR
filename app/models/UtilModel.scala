package models

import play.api.libs.json.Json
import slick.jdbc.GetResult

/**
  * Created by chinhnk on 3/12/16.
  */
case class PercentageCompletedCMR(academicSeasonName: String, facultyName: String, percent: Float)
case class PercentageResponseCMR(facultyName: String, percent: Float)

object PercentageCompletedCMR{
  implicit val percentageCompletedCMRResult = GetResult(r => PercentageCompletedCMR(r.<<,r.<<,r.<<))
}
object PercentageResponseCMR{
  implicit val percentageResponseCMRResult = GetResult(r => PercentageResponseCMR(r.<<,r.<<))
}

