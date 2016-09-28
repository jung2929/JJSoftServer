package controllers

import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import scalikejdbc._

@Singleton
class HomeController @Inject() extends Controller {

  def index = Action {
    val userId: List[String] = DB readOnly { implicit session =>
      sql"select USER_ID from USER_INFO"
        .map(rs => rs.string("USER_ID"))
        .list()
        .apply()
    }

    val a = Json.toJson(userId)

    Ok(a)
  }

}
