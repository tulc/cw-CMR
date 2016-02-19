package dao

import java.sql.Date
import javax.inject.{Inject, Singleton}

import models.CMR
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * Created by chinhnk on 2/14/16.
  */
@Singleton()
class CMRDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
  with UsersComponent with CoursesComponent with FacultiesComponent {

  import driver.api._

  class CMRs(tag: Tag) extends Table[CMR](tag, "CMR") {
    def cmrId = column[Int]("CMRId", O.PrimaryKey, O.AutoInc)
    def status = column[String]("Status")
    def createDate = column[Date]("CreateDate")
    def userCreateId = column[Int]("UserCreateId")
    def courseId = column[String]("CourseId")
    def userApprovedId = column[Option[Int]]("UserApprovedId")
    def approvedDate = column[Option[Date]]("ApprovedDate")
    def comment = column[Option[String]]("Comment")
    def userCommentedId = column[Option[Int]]("Comment")
    def commentedDate = column[Option[Date]]("CommentedDate")

    def * = (cmrId,status,createDate,userCreateId,courseId,userApprovedId,
      approvedDate,comment,userCommentedId,commentedDate) <> ((CMR.apply _).tupled, CMR.unapply _)

    def course = foreignKey("Course", courseId, courses)(_.courseId)
    def user = foreignKey("User", userCreateId, users)(_.userId)
  }

  private lazy val cmrs = TableQuery[CMRs]
  private lazy val courses = TableQuery[Courses]
  private lazy val users = TableQuery[Users]

  def insertAndReturnCMRId(courseId:String, userId:Int): Future[Int] ={
    db.run(sqlu"EXECUTE usp_createCMR @courseId = $courseId, @userId = $userId")
    db.run(sql"SELECT cmrId FROM CMR WHERE courseId = $courseId AND userCreateId = $userId".as[Int]).map(x => x.head)
  }

  def findCMRByCourseId(courseId: String): Future[Seq[CMR]] = db.run(cmrs.filter(_.courseId === courseId).result)

  def findCMRById(id: Int) : Future[Seq[CMR]] = db.run(cmrs.filter(_.cmrId === id).result)
}
