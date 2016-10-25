package controllers

import common.sqlConverter._
import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._
import scalikejdbc._


/**
  * Created by jungwh on 2016-10-25.
  */
@Singleton
class income @Inject() extends Controller {
  implicit val RegisterDataConverter = Json.reads[RegisterData]

  case class RegisterData(INPUT_DATE: String,
                          INPUT_PC: Int,
                          INPUT_IEM: String,
                          INPUT_CATEGORY: String,
                          INPUT_MEMO: String,
                          USER_ID: String){
    def validateInsert(): Option[JsonResult] = {
      /*val programLevel: Int = PROGRAM_GB match {
        case "메뉴" => 0
        case "프로그램" => 1
      }
      val isProgramIdExists = CodeSearch.programIdExists(PROGRAM_ID, programLevel)
      val isProgramNameEmpty = PROGRAM_NAME match {
        case "" => false
        case _ => true
      }*/

      //(isProgramIdExists, isProgramNameEmpty) match {
      (false, true) match {
        case (true,false) => Some(JsonResult(errorMsg = "이미 존재하는 프로그램ID입니다."))
        case (true,true) => Some(JsonResult(errorMsg = "이미 존재하는 프로그램ID이며, 프로그램명이 비었습니다."))
        case (false, false) => Some(JsonResult(errorMsg = "프로그램명이 비었습니다."))
        case (false, true) => None
      }
    }
  }

  def register() = Action { implicit req =>
    val body = req.body.asJson.get
    val data = body.as[RegisterData]
    val validationResult = data.validateInsert().headOption
    validationResult match {
      case Some(t) => BadRequest(t)
      case None =>

        DB localTx { implicit session =>
          sql"""
             INSERT INTO public."INCME_EXPNDTR_INPUT"("INPUT_DATE", "INPUT_PC", "INPUT_IEM", "INPUT_CATEGORY", "INPUT_MEMO", "USER_ID", "USE_AT", "REG_DATE")
             VALUES (${data.INPUT_DATE}, ${data.INPUT_PC}, ${data.INPUT_IEM}, ${data.INPUT_CATEGORY}, ${data.INPUT_MEMO}, ${data.USER_ID}, '1', CURRENT_TIMESTAMP)
            """
            .update().apply()
        }
        Ok
    }
  }
}
