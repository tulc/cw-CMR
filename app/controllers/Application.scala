package controllers

import javax.inject.{Singleton, Inject}

import akka.actor.ActorSystem
import dao._
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import models.Score
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import util.EmailUtil

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.Random

@Singleton()
class Application @Inject()(val userDAO: UserDAO, val messagesApi: MessagesApi, roleDAO: RoleDAO, emailUtil: EmailUtil,
                            system: ActorSystem, cmrDAO: CMRDAO, academicSeasonDAO: AcademicSeasonDAO, scoreDAO: ScoreDAO,
                            utilStatisticReportDAO: UtilStatisticReportDAO, infoCourseEachAcademicSeasonDAO: InfoCourseEachAcademicSeasonDAO)
  extends Controller with LoginLogout with AuthConfigImpl with I18nSupport with AuthElement {

  val loginForm = Form(
    mapping("email" -> nonEmptyText, "password" -> nonEmptyText
    )(userDAO.authenticate)(_.map(u => (u.email, "")))
      .verifying("The email and password you entered don't match. Please try again", result => result.isDefined)
  )

  def authenticate = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.login(formWithErrors))),
      user => gotoLoginSucceeded(user.get.userId.get)
    )
  }

  def index = AsyncStack(AuthorityKey -> roleDAO.authority("index")) { implicit request =>
    val userLogin = loggedIn
    val fromDate: Option[String] = request.getQueryString("fromDate")
    val toDate: Option[String] = request.getQueryString("toDate")
    (fromDate, toDate) match {
      case (Some(from), Some(to)) => for {
        percentComplete <- utilStatisticReportDAO.percentageCompletedCMRs
        percentResponse <- utilStatisticReportDAO.percentageResponse
        courseWithoutCLCM <- utilStatisticReportDAO.courseWithoutCLCM(from, to)
        courseWithoutCMR <- utilStatisticReportDAO.courseWithoutCMR(from, to)
        cmrWithoutResponse <- utilStatisticReportDAO.cmrWithoutResponse(from, to)
      } yield Ok(views.html.index(percentComplete, percentResponse, Some(courseWithoutCLCM), Some(courseWithoutCMR), Some(cmrWithoutResponse), userLogin))
      case _ => utilStatisticReportDAO.percentageCompletedCMRs.zip(utilStatisticReportDAO.percentageResponse).map { case (percentCompeted, percentResponse) =>
        Ok(views.html.index(percentCompeted, percentResponse, None, None, None, userLogin))
      }
    }
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded
  }

  def testTool = AsyncStack(AuthorityKey -> roleDAO.authority("testTool.list")) { implicit request =>
    val userLogin = loggedIn
    infoCourseEachAcademicSeasonDAO.findAllAvaiable.map(pageData => Ok(views.html.testTool(pageData, userLogin)))
  }

  def testGenerator = AsyncStack(AuthorityKey -> roleDAO.authority("testTool.list")) { implicit request =>
    val assessments = request.body.asFormUrlEncoded.get("assessments")
    val courseId = request.body.asFormUrlEncoded.get("courseAca").mkString.split('-').apply(0).trim()
    val academicId = request.body.asFormUrlEncoded.get("courseAca").mkString.split('-').apply(1).trim().toInt
    val numberGenerate: Int = request.body.asFormUrlEncoded.get("numberGenerate").mkString.toInt
    val r = new Random()
    val productOfScore: Seq[Score] = for {
      x <- assessments
      y <- 1 to numberGenerate
    } yield (new Score(null, r.nextInt(100), x.toInt, courseId, academicId))
    scoreDAO.insert(productOfScore).zip(infoCourseEachAcademicSeasonDAO.updateStudentCount(courseId, academicId, numberGenerate)).map { case (scoreInsert, countInsert) =>
      Redirect(routes.Application.testTool()).flashing("success" -> "%s scores of %s students have been inserted".format(scoreInsert, numberGenerate))
    }
  }

  system.scheduler.scheduleOnce(1.day) {
    for {
      _ <- utilStatisticReportDAO.deleteCMRValidate
      validateCMR <- utilStatisticReportDAO.jobScanValidateCMR
      _ <- Future.successful{validateCMR.map{x =>
        emailUtil.send(emailUtil.buildEmailDaily(x._1,x._2,x._3,x._4,x._5,Seq(x._6),x._7 + " " + x._8,x._9))
      }}
    } yield ()
  }
}
