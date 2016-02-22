package dao

import javax.inject.{Singleton, Inject}

import models.{Course, Faculty}
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by chinhnk on 2/12/16.
  */

trait FacultiesComponent{ self : HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._

  class Faculties(tag:Tag) extends Table[Faculty](tag, "Faculty"){

    def facultyId = column[Int]("FacultyId",O.PrimaryKey, O.AutoInc)
    def name = column[String]("Name")
    def pvcId = column[Int]("PVCId")
    def dltId = column[Int]("DLTId")
    def isActive = column[Char]("IsActive")
    def * = (facultyId,name,pvcId,dltId,isActive) <> ((Faculty.apply _).tupled, Faculty.unapply _)
  }
}

@Singleton
class FacultyDAO @Inject()(protected val dbConfigProvider:DatabaseConfigProvider) extends FacultiesComponent with HasDatabaseConfigProvider[JdbcProfile]{
  import driver.api._

  private lazy val faculties = TableQuery[Faculties]

  def findById(id: Int): Future[Option[Faculty]] = db.run(faculties.filter(_.facultyId === id).result.headOption)
}
