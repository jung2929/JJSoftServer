package controllers

import common.sqlConverter._
import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._
import scalikejdbc._

@Singleton
class basic @Inject() extends Controller {

  /*object UserInfo {
    implicit val residentWrites = Json.writes[UserInfo]
  }
  case class UserInfo (userId: String, userPassword: String)*/

  def login(requestUserId : String,
            requestUserPassword : String) = Action {
    val isUserValidation: Boolean = sqlExist(sql"""SELECT "USER_ID", "USER_PASSWORD"
                                FROM "USER_INFO"
                                WHERE "USER_ID" = $requestUserId
                                AND "USER_PASSWORD" = $requestUserPassword
                            """)

    val resultMap: Map[String, Boolean] = {
      Map[String, Boolean]("RESULT" -> isUserValidation)
    }

    isUserValidation match {
      case true => Ok(JsonResult(Json.toJson(resultMap)))
      case false => Ok(JsonResult(Json.toJson(resultMap), errorMsg = "로그인정보가 잘못됬습니다."))
    }

  }


  implicit val RegisterDataConverter = Json.reads[RegisterData]

  case class RegisterData(USER_ID: String,
                          USER_PASSWORD: String,
                          USER_NM: String,
                          MOBLPHON: String,
                          EMAIL: String){
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
             INSERT INTO "USER_INFO"("USER_ID", "USER_PASSWORD", "USER_NM", "MOBLPHON", "EMAIL", "USE_AT", "REG_DATE", "LOG_TM")
             VALUES (${data.USER_ID}, ${data.USER_PASSWORD}, ${data.USER_NM}, ${data.MOBLPHON}, ${data.EMAIL}, '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """
            .update().apply()
        }
        Ok
    }
  }


  def retrieve(requestUserId : String) = Action {
    val jsonResult: JsonResult = sqlToJsonOutput(
      sql"""
                                               |SELECT SUM(A.INCOME_PRICE) - SUM(A.SPEND_PRICE) AS "TOTAL_PRICE"
                                               |FROM (
                                               |        SELECT
                                               |                SUM("INPUT_PC") AS INCOME_PRICE,
                                               |                0               AS SPEND_PRICE
                                               |        FROM "INCME_EXPNDTR_INPUT"
                                               |        WHERE "USER_ID" = $requestUserId
                                               |              AND "USE_AT" = '1'
                                               |              AND "INPUT_DIVISION" = '001'
                                               |        UNION ALL
                                               |        SELECT
                                               |                0               AS INCOME_PRICE,
                                               |                SUM("INPUT_PC") AS SPEND_PRICE
                                               |        FROM "INCME_EXPNDTR_INPUT"
                                               |        WHERE "USER_ID" = $requestUserId
                                               |              AND "USE_AT" = '1'
                                               |              AND "INPUT_DIVISION" = '002'
                                               |        ) A;
                                               """)
    Ok(jsonResult)
  }
}