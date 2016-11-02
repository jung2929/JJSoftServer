package common

import play.api.libs.json._
import scalikejdbc._
import java.sql._

import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import play.api.mvc.Codec


/**
  * Created by jungwh on 2016-10-03.
  */
object sqlConverter {
  def toMap(rs: WrappedResultSet): Map[String, Any] = {
    (1 to rs.metaData.getColumnCount).foldLeft(Map[String, Any]()) { (result, i) =>
      val label = rs.metaData.getColumnLabel(i)
      val a2: Option[Map[String, Any]] = Some(rs.any(i)).map(nullableValue => result + (label -> nullableValue))
      a2.getOrElse(result)
    }
  }


    implicit val AnyMapConverter = new Writes[Map[String, Any]] {
      def writes(target: Map[String, Any]): JsObject = {
        target.foldLeft(JsObject(Seq()))((result, pair) =>
          pair._2 match {
            case value: String => result ++ Json.obj(pair._1 -> JsString(value))
            case value: Boolean => result ++ Json.obj(pair._1 -> JsBoolean(value))
            case value: Int => result ++ Json.obj(pair._1 -> JsNumber(value))
            case value: Long => result ++ Json.obj(pair._1 -> JsNumber(value))
            case value: Double => result ++ Json.obj(pair._1 -> JsNumber(value))
            case value: java.math.BigDecimal => result ++ Json.obj(pair._1 -> JsNumber(value))
            case value: java.sql.Timestamp => result ++ Json.obj(pair._1 -> Json.toJson(value))
            case null => result ++ Json.obj(pair._1 -> JsNull)
          }
        )
      }
    }

    private def sqlToJson(sql: SQL[Nothing, NoExtractor]): JsValue = {
      val mapList = DB readOnly (implicit session => sql.stripMargin.fetchSize(1000).map(toMap(_)).list().apply())
      Json.toJson(mapList)
    }

    def sqlToJsonOutput(sql: SQL[Nothing, NoExtractor]): JsonResult = {
      val sqlJson = sqlToJson(sql)
      JsonResult(sqlJson)
    }

    implicit val resultJsonConverter = new Writes[JsonResult] {
      def writes(t: JsonResult) = {
        Json.obj("data" -> t.data, "errorMsg" -> t.errorMsg)
      }
    }

    case class JsonResult(data: JsValue = Json.toJson(""), errorMsg: String = "", statusCode: Int = 500)

    object JsonResult {
      implicit def writeableOf_JsonResult(implicit codec: Codec): Writeable[JsonResult] = {
        Writeable(jsonResult => codec.encode(Json.toJson(jsonResult).toString))
      }

      implicit def contentTypeOf_JsonResult(implicit codec: Codec): ContentTypeOf[JsonResult] = {
        ContentTypeOf[JsonResult](Some(ContentTypes.JSON))
      }
    }

    def sqlExist(sql: SQL[Nothing, NoExtractor])(implicit session: DBSession = ReadOnlyAutoSession): Boolean = {
      val result: Option[String] = sql.stripMargin.map(_.string(1)).first().apply()
      result match {
        case Some(t) => true
        case None => false
      }
    }
  }