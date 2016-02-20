package dao

import javax.inject.{Singleton, Inject}

import models.Role
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by chinhnk on 2/20/16.
  */
@Singleton
class RoleDAO @Inject()(protected val dbConfigProvider:DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with AssessmentMethodComponent{
  import driver.api._

  class Roles(tag: Tag) extends Table[Role](tag,"GradeStatistic"){
    def roleId = column[Int]("RoleId",O.PrimaryKey,O.AutoInc)
    def shortName = column[String]("ShortName")
    def name = column[String]("Name")
    def description = column[String]("Description")
    def isActive = column[Char]("isActive")
    def * = (roleId,shortName,name,description,isActive) <> ((Role.apply _).tupled, Role.unapply _)
  }

  private lazy val roles = TableQuery[Roles]

  def list: Future[Seq[Role]] = db.run(roles.result)
  def findById(id: Int) : Future[Option[Role]] =
    db.run(roles.filter(_.roleId === id).filter(_.isActive === '1').result.headOption)
}
