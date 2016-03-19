package models

import java.sql.Date

/**
  * Created by chinhnk on 2/12/16.
  */
//case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
//  lazy val prev = Option(page - 1).filter(_ >= 0)
//  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
//}

case class User(userId: Option[Int], firstName: String, lastName: String,
                email: String, password: String, createDate: Option[Date], isActive: Char, roleId: String)

case class Role(roleId: String, name: String, description: String, isActive: Char)

case class Faculty(facultyId: Int, name: String, pvcId: Int, dltId: Int, isActive: Char)

case class Course(courseId: String, title: String, facultyId: Int)

case class AcademicSeason(academicSeasonId: Option[Int], name: String, startDate: Date, endDate: Date)

case class InfoCourseEachAcademicSeason(courseId: String, academicSeasonId: Int, studentNumber: Int, clId: Option[Int], cmId: Option[Int])

case class CMR(cmrId: Int, status: String, userCreateId: Int, courseId: String, academicSeasonId: Int, createdDate: Date, submittedDate: Option[Date],
               userApprovedId: Option[Int], approvedDate: Option[Date], comment: Option[String],
               userCommentedId: Option[Int], commentedDate: Option[Date])

case class GradeStatistic(cmrId: Int, statisticType: String, assessmentMethodId: Int, value: Option[Float])

case class GradeDistribution(cmrId: Int, assessmentMethodId: Int, distributionType: String, value: Option[Int])

case class AssessmentMethod(assessmentMethodId: Int, priority: Int, name: String, description: String, isActive: Char)

case class Permission(permissionId: Int, path: String, name: String)

case class RolePermission(roleId: String, permissionId: Int)

case class Score(scoreId: Option[Int], value: Double, assessmentMethodId: Int, courseId: String, academicSeasonId: Int)