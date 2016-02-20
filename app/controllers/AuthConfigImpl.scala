package controllers

import javax.inject.Inject

import dao.{RoleDAO, UserDAO}
import jp.t2v.lab.play2.auth.AuthConfig
import models.Role
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
  type Authority = Role

  val idTag: ClassTag[Id] = classTag[Id]
  val sessionTimeoutInSeconds: Int = 3600

  def resolveUser(id: Id)(implicit ctx: ExecutionContext) : Future[Option[User]] = userDAO.findUserById(id)

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.index()))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.login()))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.Application.login()))

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Forbidden("no permission"))
  }

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future.successful {
    user.roleId match {
      case (1) => true
      case (2) => true
      case (3) => true
      case (4) => true
      case (5) => true
      case (6) => true
      case _ => false
    }
  }
}