package util

import javax.inject.Inject

import models.User
import play.api.libs.mailer.{Email, MailerClient}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by chinhnk on 2/10/16.
  */

class EmailUtil @Inject()(mailerClient: MailerClient) {
  def sendEmail(subject: String, userReciver: User, cmrId:Int): Future[String] = {
    Future{
      val email = Email(
        subject,
        "Course Monitoring Report Center<info@coursemonitoringreport.com>",
        Seq("nguyenkienchinh91@gmail.com"),
        bodyText = Some(
          s"""CMR center inform that have a new submitted report.
             |
             |
             |Open in CMR to view detail:
             |http://localhost:9000/report/${cmrId}
             |""".stripMargin)
      )
      mailerClient.send(email)
    }
  }
}
