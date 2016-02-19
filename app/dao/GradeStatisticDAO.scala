package dao

import javax.inject.{Inject, Singleton}

import models.GradeStatistic
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by chinhnk on 2/15/16.
  */
@Singleton
class GradeStatisticDAO @Inject()(protected val dbConfigProvider:DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with AssessmentMethodComponent{
  import driver.api._

  class GradeStatistics(tag: Tag) extends Table[GradeStatistic](tag,"GradeStatistic"){
    def cmrId = column[Int]("CMRId",O.PrimaryKey)
    def statisticType = column[String]("StatisticType",O.PrimaryKey)
    def assessmentMethodId = column[Int]("AssessmentMethodId",O.PrimaryKey)
    def value = column[Option[Float]]("Value")

    def * = (cmrId,statisticType,assessmentMethodId,value) <> ((GradeStatistic.apply _).tupled, GradeStatistic.unapply _)

    def assessmentMethod= foreignKey("AssessmentMethodId",assessmentMethodId,assessmentMethods)(_.assessmentMethodId)
  }

  private lazy val assessmentMethods = TableQuery[AssessmentMethods]
  private lazy val gradeStatistics = TableQuery[GradeStatistics]

  def findByCMRId(cmrId: Int): Future[Seq[GradeStatistic]] =
    db.run(gradeStatistics.filter(_.cmrId === cmrId).result)
}
