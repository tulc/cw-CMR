package dao

import javax.inject.{Inject, Singleton}

import models.GradeDistribution
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by chinhnk on 2/15/16.
  */
@Singleton
class GradeDistributionDAO @Inject()(protected val dbConfigProvider:DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with AssessmentMethodComponent{
  import driver.api._

  class GradeDistributions(tag: Tag) extends Table[GradeDistribution](tag,"GradeDistribution"){
    def cmrId = column[Int]("CMRId",O.PrimaryKey)
    def assessmentMethodId = column[Int]("AssessmentMethodId",O.PrimaryKey)
    def distributionType = column[String]("DistributionType",O.PrimaryKey)
    def value = column[Option[Int]]("Value")

    def * = (cmrId,assessmentMethodId,distributionType,value) <> ((GradeDistribution.apply _).tupled, GradeDistribution.unapply _)

    def assessmentMethod = foreignKey("AssessmentMethodId",assessmentMethodId,assessmentMethods)(_.assessmentMethodId)
  }

  private lazy val assessmentMethods = TableQuery[AssessmentMethods]
  private lazy val gradeDistributions = TableQuery[GradeDistributions]

  def findByCMRId(cmrId: Int) : Future[Seq[GradeDistribution]] = db.run(
    gradeDistributions.filter(_.cmrId === cmrId).result
  )

}
