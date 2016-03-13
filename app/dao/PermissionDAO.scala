package dao

import javax.inject.{Inject, Singleton}

import models.Permission
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by chinhnk on 3/14/16.
  */

trait PermissionComponent { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._

  class Permissions(tag: Tag) extends Table[Permission](tag, "Permission") {
    def permissionId = column[Int]("PermissionId", O.PrimaryKey, O.AutoInc)
    def path = column[String]("Path")
    def name = column[String]("Name")
    def * = (permissionId, path, name) <>((Permission.apply _).tupled, Permission.unapply _)
  }
}

@Singleton
class PermissionDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with PermissionComponent {
  import driver.api._

  private lazy val permissions = TableQuery[Permissions]

  def findAll : Future[Seq[Permission]] = db.run(permissions.result)
}