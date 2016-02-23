package dao

import javax.inject.{Singleton, Inject}

import models.{User, Role, Permission, RolePermission}
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

/**
  * Created by chinhnk on 2/20/16.
  */
@Singleton
class RoleDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  class Roles(tag: Tag) extends Table[Role](tag, "Role") {
    def roleId = column[String]("RoleId", O.PrimaryKey)
    def name = column[String]("Name")
    def description = column[String]("Description")
    def isActive = column[Char]("isActive")
    def * = (roleId, name, description, isActive) <>((Role.apply _).tupled, Role.unapply _)
  }

  class Permissions(tag: Tag) extends Table[Permission](tag, "Permission") {
    def permissionId = column[Int]("PermissionId", O.PrimaryKey, O.AutoInc)
    def name = column[String]("Name")
    def path = column[String]("Path")
    def * = (permissionId, name, path) <>((Permission.apply _).tupled, Permission.unapply _)
  }

  class RolesPermissions(tag: Tag) extends Table[RolePermission](tag, "Role_Permission") {
    def roleId = column[String]("RoleId")
    def permissionId = column[Int]("PermissionId")
    def * = (roleId, permissionId) <>((RolePermission.apply _).tupled, RolePermission.unapply _)
  }

  private lazy val roles = TableQuery[Roles]
  private lazy val permissions = TableQuery[Permissions]
  private lazy val rolesPermissions = TableQuery[RolesPermissions]

  def list: Future[Seq[Role]] = db.run(roles.result)

  def findById(id: String): Future[Option[Role]] =
    db.run(roles.filter(_.roleId === id).filter(_.isActive === '1').result.headOption)

  def authority(path: String)(user: User): Future[Boolean] = {
    val query = for {
      (permission, rolePermission) <- permissions.join(rolesPermissions.filter(_.roleId === user.roleId)).on(_.permissionId === _.permissionId)
    } yield (permission)
    val listPermission = db.run(query.result)
    listPermission.map { p =>
      p.exists(v => v.path == path)
    }
  }
}