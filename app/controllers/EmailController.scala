package controllers

import javax.inject.Inject

import play.api.mvc.Controller
import util.EmailUtil

/**
  * Created by chinhnk on 2/11/16.
  */
class EmailController @Inject()(emailUtil: EmailUtil) extends Controller{
//    def sendEmailExample = Action {
//      val user = new User(1,"Chinh","Nguyen","nguyenkienchinh91@gmail.com","1",new Date(),true)
//      val id = emailUtil.sendEmail("Testing mail send",user,"type1")
//      Ok(views.html.index(s"Email $id sent!"))
//    }
}
