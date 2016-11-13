package controllers

import common.sqlConverter._
import javax.inject._

import play.api.libs.json.Json
import play.api.mvc._
import scalikejdbc._

/**
  * Created by jungwh on 2016-11-12.
  */

@Singleton
class chart @Inject() extends Controller {
  def barChartRetrieve(requestUserId : String,
                       requestMonth : String,
                       requestCategory : String) = Action {
    val jsonResult: JsonResult = sqlToJsonOutput(sql"""
                                                    |SELECT substring("INPUT_DATE" from 1 for 6) AS "INPUT_DATE", SUM("INPUT_PC") AS "SUM_INPUT_PC"
                                                    |FROM "INCME_EXPNDTR_INPUT"
                                                    |WHERE substring("INPUT_DATE" from 1 for 4) = to_char(CURRENT_DATE , 'YYYY')
                                                    |AND cast(cast(substring("INPUT_DATE" from 5 for 2) AS INTEGER) AS TEXT) = ($requestMonth)
                                                    |AND "INPUT_DIVISION" = '002'
                                                    |AND "USER_ID" = ($requestUserId)
                                                    |AND "INPUT_CATEGORY" = ($requestCategory)
                                                    |GROUP BY substring("INPUT_DATE" from 1 for 6)
                                                    |
                                                    |UNION ALL
                                                    |
                                                    |SELECT substring("INPUT_DATE" from 1 for 6) AS "INPUT_DATE", SUM("INPUT_PC") AS "SUM_INPUT_PC"
                                                    |FROM "INCME_EXPNDTR_INPUT"
                                                    |WHERE substring("INPUT_DATE" from 1 for 4) = cast((cast(to_char(CURRENT_DATE , 'YYYY') AS INTEGER) - 1) AS TEXT)
                                                    |AND cast(cast(substring("INPUT_DATE" from 5 for 2) AS INTEGER) AS TEXT) = ($requestMonth)
                                                    |AND "INPUT_DIVISION" = '002'
                                                    |AND "USER_ID" = ($requestUserId)
                                                    |AND "INPUT_CATEGORY" = ($requestCategory)
                                                    |GROUP BY substring("INPUT_DATE" from 1 for 6)
                                               """)
    Ok(jsonResult)
  }

  def PieChartRetrieve(requestUserId : String,
                       requestMonth : String) = Action {
    val jsonResult: JsonResult = sqlToJsonOutput(sql"""
                                                    |SELECT substring("INPUT_DATE" from 1 for 6) AS "INPUT_DATE", "INPUT_CATEGORY", SUM("INPUT_PC") AS "SUM_INPUT_PC"
                                                    |FROM "INCME_EXPNDTR_INPUT"
                                                    |WHERE substring("INPUT_DATE" from 1 for 4) = to_char(CURRENT_DATE , 'YYYY')
                                                    |AND cast(cast(substring("INPUT_DATE" from 5 for 2) AS INTEGER) AS TEXT) = ($requestMonth)
                                                    |AND "INPUT_DIVISION" = '002'
                                                    |AND "USER_ID" = ($requestUserId)
                                                    |GROUP BY substring("INPUT_DATE" from 1 for 6), "INPUT_CATEGORY"
                                               """)
    Ok(jsonResult)
  }

  def LineChartRetrieve(requestUserId : String) = Action {
    val jsonResult: JsonResult = sqlToJsonOutput(sql"""
                                                    |SELECT substring(A."INPUT_DATE" from 1 for 6) AS "INPUT_DATE", B."BUDGET", SUM(A."INPUT_PC") AS "SUM_INPUT_PC"
                                                    |FROM "INCME_EXPNDTR_INPUT" A
                                                    |LEFT JOIN "BUDGET_INFO" B
                                                    |  ON substring(A."INPUT_DATE" from 1 for 6) = B."DATE_YYYYMM"
                                                    |  AND A."USER_ID" = B."USER_ID"
                                                    |WHERE substring(A."INPUT_DATE" from 1 for 4) = to_char(CURRENT_DATE , 'YYYY')
                                                    |AND cast(cast(substring(A."INPUT_DATE" from 5 for 2) AS INTEGER) AS TEXT) = cast(cast(to_char(CURRENT_DATE , 'MM') AS INTEGER) - 2 AS TEXT)
                                                    |AND A."INPUT_DIVISION" = '002'
                                                    |AND A."USER_ID" = ($requestUserId)
                                                    |GROUP BY substring(A."INPUT_DATE" from 1 for 6), B."BUDGET"
                                                    |
                                                    |UNION ALL
                                                    |
                                                    |SELECT substring(A."INPUT_DATE" from 1 for 6) AS "INPUT_DATE", B."BUDGET", SUM(A."INPUT_PC") AS "SUM_INPUT_PC"
                                                    |FROM "INCME_EXPNDTR_INPUT" A
                                                    |LEFT JOIN "BUDGET_INFO" B
                                                    |  ON substring(A."INPUT_DATE" from 1 for 6) = B."DATE_YYYYMM"
                                                    |  AND A."USER_ID" = B."USER_ID"
                                                    |WHERE substring(A."INPUT_DATE" from 1 for 4) = to_char(CURRENT_DATE , 'YYYY')
                                                    |AND cast(cast(substring(A."INPUT_DATE" from 5 for 2) AS INTEGER) AS TEXT) = cast(cast(to_char(CURRENT_DATE , 'MM') AS INTEGER) - 1 AS TEXT)
                                                    |AND A."INPUT_DIVISION" = '002'
                                                    |AND A."USER_ID" = ($requestUserId)
                                                    |GROUP BY substring(A."INPUT_DATE" from 1 for 6), B."BUDGET"
                                                    |
                                                    |UNION ALL
                                                    |
                                                    |SELECT substring(A."INPUT_DATE" from 1 for 6) AS "INPUT_DATE", B."BUDGET", SUM(A."INPUT_PC") AS "SUM_INPUT_PC"
                                                    |FROM "INCME_EXPNDTR_INPUT" A
                                                    |LEFT JOIN "BUDGET_INFO" B
                                                    |  ON substring(A."INPUT_DATE" from 1 for 6) = B."DATE_YYYYMM"
                                                    |  AND A."USER_ID" = B."USER_ID"
                                                    |WHERE substring(A."INPUT_DATE" from 1 for 4) = to_char(CURRENT_DATE , 'YYYY')
                                                    |AND cast(cast(substring(A."INPUT_DATE" from 5 for 2) AS INTEGER) AS TEXT) = to_char(CURRENT_DATE , 'MM')
                                                    |AND A."INPUT_DIVISION" = '002'
                                                    |AND A."USER_ID" = ($requestUserId)
                                                    |GROUP BY substring(A."INPUT_DATE" from 1 for 6), B."BUDGET"
                                               """)
    Ok(jsonResult)
  }
}