package dao

import java.sql.Date
import java.util.Calendar
import javax.inject.{Inject, Singleton}

import models.{CMR, Course, Faculty, User, InfoCourseEachAcademicSeason, AcademicSeason}
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
  * Created by chinhnk on 2/14/16.
  */

trait CMRComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  class CMRs(tag: Tag) extends Table[CMR](tag, "CMR") {
    def cmrId = column[Int]("CMRId", O.PrimaryKey, O.AutoInc)
    def status = column[String]("Status")
    def userCreateId = column[Int]("UserCreateId")
    def courseId = column[String]("CourseId")
    def academicSeasonId = column[Int]("AcademicSeasonId")
    def createdDate = column[Date]("CreatedDate")
    def submittedDate = column[Option[Date]]("SubmittedDate")
    def userApprovedId = column[Option[Int]]("UserApprovedId")
    def approvedDate = column[Option[Date]]("ApprovedDate")
    def comment = column[Option[String]]("Comment")
    def userCommentedId = column[Option[Int]]("UserCommentedId")
    def commentedDate = column[Option[Date]]("CommentedDate")

    def * = (cmrId, status, userCreateId, courseId, academicSeasonId, createdDate, submittedDate, userApprovedId,
      approvedDate, comment, userCommentedId, commentedDate) <>((CMR.apply _).tupled, CMR.unapply _)
  }

}

@Singleton()
class CMRDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
  with UsersComponent with CoursesComponent with CMRComponent with FacultiesComponent
  with InfoCourseEachAcademicSeasonComponent with AcademicSeasonComponent {

  import driver.api._

  private lazy val cmrs = TableQuery[CMRs]
  private lazy val courses = TableQuery[Courses]
  private lazy val users = TableQuery[Users]
  private lazy val faculties = TableQuery[Faculties]
  private lazy val infoCourseEachAcademicSeasons = TableQuery[InfoCourseEachAcademicSeasons]
  private lazy val academicSeasons = TableQuery[AcademicSeasons]

  def insertCMR(courseId: String, userId: Int, academicSeasonId: Int): Future[Int] = {
    db.run(sqlu"EXECUTE usp_createCMR @courseId = $courseId, @academicSeasonId = $academicSeasonId, @userId = $userId")
  }

  def findCMRByCourseAcademicSeasonId(courseId: String, academicSeasonId: Int): Future[Seq[CMR]] =
    db.run(cmrs.filter(cmr => cmr.courseId === courseId && cmr.academicSeasonId === academicSeasonId).result)

  def findCMRById(id: Int): Future[Option[CMR]] = db.run(cmrs.filter(_.cmrId === id).result.headOption)

  def removeCMRById(id: Int, user: User, comment: String): Future[Int] = {
    for {
      currentCMR <- findCMRById(id)
      number <- currentCMR.map{ current =>
        (current, user.roleId) match {
          case (c, "CL") => db.run(cmrs.filter(_.cmrId === id).delete)
          case (c, "CM") => db.run(cmrs.filter(_.cmrId === id).map(cmr => (cmr.status, cmr.comment)).update("Created", Some(current.comment.getOrElse("")+ comment)))
          case (c, "DLT") => db.run(cmrs.filter(_.cmrId === id).map(cmr => (cmr.status, cmr.comment)).update("Created", Some(current.comment.getOrElse("")+ comment)))
        }
      }.getOrElse(Future.successful(0))
    } yield (number)
  }

  def findMaxId(courseId: String, userCreateId: Int, academicSeasonId: Int): Future[Int] = {
    db.run(cmrs.filter(cmr =>
      cmr.courseId === courseId && cmr.userCreateId === userCreateId && cmr.academicSeasonId === academicSeasonId)
      .result).map(x => x.head.cmrId)
  }

  def updateStatusCMR(id: Int, comment: String, userRole: String, userId: Int): Future[Int] = {
    val date = new Date(Calendar.getInstance().getTime.getTime)
    for {
      currentCMR <- findCMRById(id)
      number <- currentCMR.map{ current =>
        (current, userRole) match {
          case (c, "CL") => db.run(cmrs.filter(_.cmrId === id).map(cmr => (cmr.status, cmr.submittedDate, cmr.comment)).update(("Submitted", Some(date), Some(current.comment.getOrElse("")+ comment))))
          case (c, "CM") => db.run(cmrs.filter(_.cmrId === id).map(cmr => (cmr.status, cmr.userApprovedId, cmr.approvedDate, cmr.comment)).update(("Approved", Some(userId), Some(date), Some(current.comment.getOrElse("")+ comment))))
          case (c, "DLT") => db.run(cmrs.filter(_.cmrId === id).map(cmr => (cmr.status, cmr.userCommentedId, cmr.commentedDate, cmr.comment)).update(("Commented", Some(userId), Some(date), Some(current.comment.getOrElse("")+ comment))))
        }
      }.getOrElse(Future.successful(0))
    } yield (number)
  }

  def findByUser(userRole: String, userId: Int):
  Future[Seq[(CMR, Option[String], Option[String], Option[String], Option[User], Option[User])]] = {

    val clQuery = for {
      ((((((cmrs, courses), faculties), info), academicSeason), clUser), cmUser) <- cmrs
        .joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(faculties).on(_._2.map(_.facultyId) === _.facultyId)
        .joinLeft(infoCourseEachAcademicSeasons.filter(_.clId === userId)).on(_._1._1.courseId === _.courseId)
        .joinLeft(academicSeasons).on(_._1._1._1.academicSeasonId === _.academicSeasonId)
        .joinLeft(users).on(_._1._2.map(_.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(_.cmId) === _.userId)
    } yield (cmrs, courses.map(_.title), faculties.map(_.name), academicSeason.map(_.name), clUser, cmUser)
    val cmQuery = for {
      ((((((cmrs, courses), faculties), info), academicSeason), clUser), cmUser) <- cmrs.filterNot(_.status === "Created")
        .joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(faculties).on(_._2.map(_.facultyId) === _.facultyId)
        .joinLeft(infoCourseEachAcademicSeasons.filter(_.cmId === userId)).on(_._1._1.courseId === _.courseId)
        .joinLeft(academicSeasons).on(_._1._1._1.academicSeasonId === _.academicSeasonId)
        .joinLeft(users).on(_._1._2.map(_.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(_.cmId) === _.userId)
    } yield (cmrs, courses.map(_.title), faculties.map(_.name), academicSeason.map(_.name), clUser, cmUser)
    val dltQuery = for {
      ((((((cmrs, courses), faculties), info), academicSeason), clUser), cmUser) <- cmrs.filterNot(_.status === "Created")
        .joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(faculties.filter(_.dltId === userId)).on(_._2.map(_.facultyId) === _.facultyId)
        .joinLeft(infoCourseEachAcademicSeasons).on(_._1._1.courseId === _.courseId)
        .joinLeft(academicSeasons).on(_._1._1._1.academicSeasonId === _.academicSeasonId)
        .joinLeft(users).on(_._1._2.map(_.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(_.cmId) === _.userId)
    } yield (cmrs, courses.map(_.title), faculties.map(_.name), academicSeason.map(_.name), clUser, cmUser)
    val pvcQuery = for {
      ((((((cmrs, courses), faculties), info), academicSeason), clUser), cmUser) <- cmrs.filterNot(_.status === "Created")
        .joinLeft(courses).on(_.courseId === _.courseId)
        .joinLeft(faculties.filter(_.pvcId === userId)).on(_._2.map(_.facultyId) === _.facultyId)
        .joinLeft(infoCourseEachAcademicSeasons).on(_._1._1.courseId === _.courseId)
        .joinLeft(academicSeasons).on(_._1._1._1.academicSeasonId === _.academicSeasonId)
        .joinLeft(users).on(_._1._2.map(_.clId) === _.userId)
        .joinLeft(users).on(_._1._1._2.map(_.cmId) === _.userId)
    } yield (cmrs, courses.map(_.title), faculties.map(_.name), academicSeason.map(_.name), clUser, cmUser)
    userRole match {
      case "CL" => db.run(clQuery.sortBy(_._1.createdDate.desc).result)
      case "CM" => db.run(cmQuery.sortBy(_._1.submittedDate.desc).result)
      case "DLT" => db.run(dltQuery.sortBy(_._1.approvedDate.desc).result)
      case "PVC" => db.run(pvcQuery.sortBy(_._1.approvedDate.desc).result)
    }
  }

}
