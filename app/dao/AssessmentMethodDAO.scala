package dao

import javax.inject.{Inject, Singleton}

import models.AssessmentMethod
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

/**
  * Created by chinhnk on 2/15/16.
  */

trait AssessmentMethodComponent {self: HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._

  class AssessmentMethods(tag:Tag) extends Table[AssessmentMethod](tag,"AssessmentMethod"){
    def assessmentMethodId = column[Int]("AssessmentMethodId",O.PrimaryKey,O.AutoInc)
    def priority = column[Int]("Priority")
    def name = column[String]("Name")
    def description = column[String]("Description")
    def isActive = column[Char]("isActive")

    def * = (assessmentMethodId,priority,name,description,isActive) <> ((AssessmentMethod.apply _).tupled, AssessmentMethod.unapply _)
  }
}

@Singleton
class AssessmentMethodDAO @Inject()(protected val dbConfigProvider:DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
  with AssessmentMethodComponent {
  import driver.api._

  private lazy val assessmentMethods = TableQuery[AssessmentMethods]

}
