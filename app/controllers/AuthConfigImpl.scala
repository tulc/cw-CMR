package controllers

import javax.inject.Inject

import dao.{RoleDAO, UserDAO}
import jp.t2v.lab.play2.auth.AuthConfig
import models.Role._
import play.api.mvc.{Result, RequestHeader}
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}

/**
  * Created by chinhnk on 2/20/16.
  */
trait AuthConfigImpl extends AuthConfig {
  @Inject val userDAO: UserDAO

  type Id = Int
  type User = models.User
  type Authority = User => Future[Boolean]

  val idTag: ClassTag[Id] = classTag[Id]
  val sessionTimeoutInSeconds: Int = 3600

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = userDAO.findUserById(id)

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index()))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.login()))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.login()))

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Forbidden("No permission"))
  }

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = authority(user)
    /*(user.roleId, authority) match {
      case (1, _) => true
      case (2, PVC) => true
      case (3, DLT) => true
      case (4, CM) => true
      case (5, CL) => true
      case (6, Guest) => true
      case _ => false
    }*/
//  }
}