package util

import javax.inject.{Singleton, Inject}

import dao.{UserDAO, CourseDAO, FacultyDAO, CMRDAO}
import models.User
import play.api.libs.mailer.{Email, MailerClient}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by chinhnk on 2/10/16.
  */
@Singleton
class EmailUtil @Inject()(mailerClient: MailerClient, cmrDAO: CMRDAO, courseDAO: CourseDAO,
                          facultyDAO: FacultyDAO, userDAO: UserDAO) {
  def send(email: Email): Future[String] = {
    Future{
      mailerClient.send(email)
    }
  }

  def buildEmailNotifyNewAction(cmrId: Int, status: String, title: String,
                 courseId: String, faculty: String, toEmail: Seq[String], userName: String): Email = {
    val subject = s"""CMR Center notification: CMR no.${cmrId} has been ${status}"""
    val from = "Course Monitoring Report Center<info@coursemonitoringreport.com>"
    val text = Some(
      s"""CMR center inform that:
          |Course ${title} - ${courseId} of ${faculty} have been ${status} by ${userName}.
          |
          |* The CMR must be commented on within 14 days.
          |Please check more detail in:
          |http://localhost:9000/report/${cmrId}
          |""".stripMargin)
    Email(subject, from, toEmail, text)
  }

}
