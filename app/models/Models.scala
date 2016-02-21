package models

import java.sql.Date

import dao.RoleDAO
import play.api.libs.json.Json

/**
  * Created by chinhnk on 2/12/16.
  */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class User(userId: Int, firstName: String, lastName: String,
                email: String, password: String, createDate: Date, isActive: Char, roleId: Int)

case class Role(roleId: Int, shortName: String, name: String, description: String, isActive: Char)

case class Faculty(facultyId: Int, name: String, pvcId: Int, dltId: Int, isActive: Char)

case class Course(courseId: String, title: String, academicSession: String, studentNumber: Int,
                  createDate: Date, startDate: Date, endDate: Date, facultyId: Int, clId: Int, cmId: Int)

case class CMR(cmrId: Int, status: String, createDate: Date, userCreateId: Int, courseId: String,
               userApprovedId: Option[Int], approvedDate: Option[Date], comment: Option[String],
               userCommentedId: Option[Int], commentedDate: Option[Date])

case class GradeStatistic(cmrId: Int, statisticType: String, assessmentMethodId: Int, value: Option[Float])

case class GradeDistribution(cmrId: Int, assessmentMethodId: Int, distributionType: String, value: Option[Int])

case class AssessmentMethod(assessmentMethodId: Int, priority: Int, name: String, description: String, isActive: Char)

object Role {

  sealed trait Role

  case object Administrator extends Role
  case object PVC extends Role
  case object DLT extends Role
  case object CM extends Role
  case object CL extends Role
  case object Guest extends Role

  def valueOf(id: Int): Role = id match {
    case 1 => Administrator
    case 2 => PVC
    case 3 => DLT
    case 4 => CM
    case 5 => CL
    case 6 => Guest
    case _ => throw new IllegalArgumentException()
  }
}