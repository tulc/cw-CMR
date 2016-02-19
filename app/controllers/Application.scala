package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext


class Application extends Controller  {

  def index = Action { request =>
    Ok(views.html.index("Ok"))
  }


}
