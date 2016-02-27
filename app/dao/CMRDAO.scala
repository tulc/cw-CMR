package dao

import java.sql.Date
import java.util.Calendar
import javax.inject.{Inject, Singleton}

import models.{CMR, Course,Faculty,User}
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
    def userCreateId = column[Int]("UserCreateId")
    def courseId = column[String]("CourseId")
    def createdDate = column[Date]("CreatedDate")
    def submittedDate = column[Option[Date]]("SubmittedDate")
    def userApprovedId = column[Option[Int]]("UserApprovedId")
    def approvedDate = column[Option[Date]]("ApprovedDate")
    def comment = column[Option[String]]("Comment")
    def userCommentedId = column[Option[Int]]("Comment")
    def commentedDate = column[Option[Date]]("CommentedDate")

    def * = (cmrId,status,userCreateId,courseId, createdDate,submittedDate,userApprovedId,
      approvedDate,comment,userCommentedId,commentedDate) <> ((CMR.apply _).tupled, CMR.unapply _)
  }

  private lazy val cmrs = TableQuery[CMRs]
  private lazy val courses = TableQuery[Courses]
  private lazy val users = TableQuery[Users]
  private lazy val faculties = TableQuery[Faculties]

  def insertCMR(courseId:String, userId:Int): Future[Int] ={
    db.run(sqlu"EXECUTE usp_createCMR @courseId = $courseId, @userId = $userId")
  }

  def findCMRByCourseId(courseId: String): Future[Seq[CMR]] = db.run(cmrs.filter(_.courseId === courseId).result)

  def findCMRById(id: Int) : Future[Seq[CMR]] = db.run(cmrs.filter(_.cmrId === id).result)

  def removeCMRById(id: Int) : Future[Int] = db.run(cmrs.filter(_.cmrId === id).delete)

  def findMaxId(courseId:String,userCreateId: Int) : Future[Int] = db.run(
    cmrs.filter(_.courseId===courseId).filter(_.userCreateId===userCreateId).result).map(x => x.head.cmrId)

  def updateStatusCMR(id: Int, userRole: String, userId: Int) : Future[Int] = {
    val date = new Date(Calendar.getInstance().getTime.getTime)
    userRole match {
      case "CL" => db.run(cmrs.filter(_.cmrId === id).map(cmr => (cmr.status, cmr.submittedDate)).update(("Submitted", Some(date))))
      case "CM" => db.run(cmrs.filter(_.cmrId === id).map(cmr => (cmr.status, cmr.userApprovedId, cmr.approvedDate)).update(("Approved", Some(userId), Some(date))))
      case "DLT" => db.run(cmrs.filter(_.cmrId === id).map(cmr => (cmr.status, cmr.userCommentedId, cmr.commentedDate)).update(("Commented", Some(userId), Some(date))))
    }
  }

  def findByUser(userRole: String, userId: Int) : Future[Seq[(CMR,Option[Course], Option[Faculty], Option[User], Option[User])]] = {
    val clQuery = for {
      ((((cmrs, courses),faculties),clUser),cmUser) <- cmrs.joinLeft(courses.filter(_.clId === userId)).on(_.courseId === _.courseId)
        .joinLeft(faculties).on(_._2.map(c => c.facultyId) === _.facultyId)
        .joinLeft(users).on(_._1._2.map(c => c.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(c => c.cmId) === _.userId)
    } yield (cmrs, courses, faculties, clUser, cmUser)
    val cmQuery = for{
      ((((cmrs, courses),faculties),clUser),cmUser) <- cmrs.filterNot(_.status === "Created").joinLeft(courses.filter(_.cmId === userId)).on(_.courseId === _.courseId)
        .joinLeft(faculties).on(_._2.map(c => c.facultyId) === _.facultyId)
        .joinLeft(users).on(_._1._2.map(c => c.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(c => c.cmId) === _.userId)
    } yield (cmrs, courses, faculties, clUser, cmUser)
    val dltQuery = for{
      ((((cmrs, courses),faculties),clUser),cmUser) <- cmrs.filterNot(_.status === "Created").joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(faculties.filter(_.dltId === userId)).on(_._2.map(c => c.facultyId) === _.facultyId)
        .joinLeft(users).on(_._1._2.map(c => c.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(c => c.cmId) === _.userId)
    } yield (cmrs, courses, faculties, clUser, cmUser)
    val pvcQuery = for{
      ((((cmrs, courses),faculties),clUser),cmUser) <- cmrs.filterNot(_.status === "Created").joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(faculties.filter(_.pvcId === userId)).on(_._2.map(c => c.facultyId) === _.facultyId)
        .joinLeft(users).on(_._1._2.map(c => c.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(c => c.cmId) === _.userId)
    } yield (cmrs, courses, faculties, clUser, cmUser)
    userRole match {
      case "CL" => db.run(clQuery.result)
      case "CM" => db.run(cmQuery.result)
      case "DLT" => db.run(dltQuery.result)
      case "PVC" => db.run(pvcQuery.result)
    }
  }

  def findUserDLTByStatus(status: String) : Future[Seq[Option[User]]] = {
    val query = for {
      (((cmrs ,courses), faculties), dltUser) <- cmrs.filter(_.status === status)
        .joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(faculties).on(_._2.map(c => c.facultyId) === _.facultyId)
        .joinLeft(users).on(_._2.map(f => f.dltId) === _.userId)
    }yield (dltUser)
    db.run(query.result)
  }
}
