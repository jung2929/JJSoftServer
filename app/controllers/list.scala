package controllers

import common.sqlConverter._
import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._
import scalikejdbc._

/**
  * Created by jungwh on 2016-10-19.
  */

@Singleton
class list @Inject() extends Controller {
  object ListInfo {
    implicit val residentWrites = Json.writes[ListInfo]
  }
  case class ListInfo (inputDate: String,
                       inputPc: String,
                       inputIem: String,
                       inputCatagory: String,
                       inputMemo: String)

  def retrieve(requestInputDateFrom : String,
            requestInputDateTo : String,
            requestUserId : String) = Action {
    val jsonResult: JsonResult = sqlToJsonOutput(sql"""
                                               SELECT "INPUT_DATE", "INPUT_PC", "INPUT_IEM", "INPUT_CATAGORY", "INPUT_MEMO"
                                               FROM "INCME_EXPNDTR_INPUT"
                                               WHERE "INPUT_DATE" BETWEEN $requestInputDateFrom AND $requestInputDateTo
                                               AND "USER_ID" = $requestUserId
                                               AND "USE_AT" = '1'
                                               """)
    Ok(jsonResult)
  }
}
