package controllers

import javax.inject.{Singleton, Inject}

import akka.actor.ActorSystem
import dao._
import jp.t2v.lab.play2.auth.{AuthElement, LoginLogout}
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

@Singleton()
class Application @Inject()(val userDAO: UserDAO, val messagesApi: MessagesApi, roleDAO: RoleDAO,
                            system: ActorSystem, cmrDAO: CMRDAO, academicSeasonDAO: AcademicSeasonDAO, utilStatisticReportDAO: UtilStatisticReportDAO)
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
      case (Some(from),Some(to)) => for {
          percentComplete <- utilStatisticReportDAO.percentageCompletedCMRs
          percentResponse <- utilStatisticReportDAO.percentageResponse
          courseWithoutCLCM <- utilStatisticReportDAO.courseWithoutCLCM(from,to)
          courseWithoutCMR <- utilStatisticReportDAO.courseWithoutCMR(from,to)
          cmrWithoutResponse <- utilStatisticReportDAO.cmrWithoutResponse(from,to)
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

  //  system.scheduler.scheduleOnce(10.seconds){
  //    for {
  //      userDLT <- cmrDAO.findUserDLTByStatus("Approved")
  //
  //    } yield ()
  //  }
}
