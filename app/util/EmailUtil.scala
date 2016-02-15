package util

import javax.inject.Inject

import models.User
import play.api.libs.mailer.{Email, MailerClient}

/**
  * Created by chinhnk on 2/10/16.
  */

class EmailUtil @Inject()(mailerClient: MailerClient) {
  def sendEmail(subject: String, user: User, mailType: String): String = {
    val email = Email(
      subject,
      "Course Monitoring Report <info@coursemonitoringreport.com>",
      Seq(user.email),
      bodyHtml = Some(pickMail(user,mailType))
    )
    mailerClient.send(email)
  }

  def pickMail(user: User, mailType: String): String = {
    s"""<html><body><p>This is mail template</p><p><b>Hello ${user.firstName} ${user.lastName}</b></p></body></html>"""
  }
}
