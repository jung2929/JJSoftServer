package controllers

import common.sqlConverter._
import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._
import scalikejdbc._

/**
  * Created by jungwh on 2016-11-06.
  */

@Singleton
class category @Inject extends Controller {
  def retrieve(requestUserId : String,
               requestInputDivision : String) = Action {
    val jsonResult: JsonResult = sqlToJsonOutput(sql"""
                                                    |SELECT "CL_CODE_NM"
                                                    |FROM "CMMN_CL_CODE"
                                                    |WHERE "USER_ID" = $requestUserId
                                                    |AND "INPUT_DIVISION" = $requestInputDivision
                                                    |AND "USE_AT" = '1'
                                               """)
    Ok(jsonResult)
  }

  implicit val RegisterDataConverter = Json.reads[RegisterData]

  case class RegisterData(USER_ID: String,
                          INPUT_DIVISION: String,
                          CL_CODE_NM: String
                          ){
    def validateInsert(): Option[JsonResult] = {
      val isClCodeNameExists = sqlExist(sql"""
                                           SELECT "CL_CODE"
                                           FROM "CMMN_CL_CODE"
                                           WHERE "INPUT_DIVISION" = ${INPUT_DIVISION}
                                           AND "USER_ID"= ${USER_ID}
                                           AND "CL_CODE_NM"=${CL_CODE_NM}
                                           """)

      (isClCodeNameExists) match {
        case (true) => Some(JsonResult(errorMsg = "이미 존재하는 카테고리명 입니다."))
        case (false) => None
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
             INSERT INTO "CMMN_CL_CODE"("INPUT_DIVISION", "CL_CODE", "CL_CODE_NM", "USE_AT", "CODE_DC", "REG_DATE", "USER_ID")
             SELECT ${data.INPUT_DIVISION}, TRIM(to_char(CAST(MAX("CL_CODE") AS NUMERIC(5,0)) + 1, '000')), ${data.CL_CODE_NM}, '1', NULL, CURRENT_TIMESTAMP , ${data.USER_ID}
             FROM "CMMN_CL_CODE"
             WHERE "INPUT_DIVISION" = ${data.INPUT_DIVISION}
             AND "USER_ID"= ${data.USER_ID}
            """
            .update().apply()
        }
        Ok
    }
  }

  implicit val DeleteDataConverter = Json.reads[DeleteData]
  case class DeleteData(USER_ID: String,
                          INPUT_DIVISION: String,
                          CL_CODE_NM: String
                         )

  def delete() = Action(parse.json) { req =>
    val data = req.body.as[DeleteData]

    DB localTx { implicit session =>
      sql"""
           DELETE FROM "CMMN_CL_CODE"
           WHERE "INPUT_DIVISION" = ${data.INPUT_DIVISION}
           AND "USER_ID" = ${data.USER_ID}
           AND "CL_CODE_NM" = ${data.CL_CODE_NM}
         """
        .update().apply()
    }
    Ok
  }
}
