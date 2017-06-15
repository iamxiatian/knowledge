package xiatian.knowledge.api

import java.text.SimpleDateFormat
import java.util.Date

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.joda.time.DateTime
import spray.json._
import xiatian.knowledge.models.Publication

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  def jsonResultOk(data: JsValue) = Map[String, JsValue](
    "status" -> JsString("OK"),
    "data" -> data).toJson.prettyPrint

  def jsonResultOk(data: String) = Map[String, JsValue](
    "status" -> JsString("OK"),
    "data" -> JsString(data)).toJson.prettyPrint

  def jsonResultOk(data: Vector[JsValue]) = Map[String, JsValue](
    "status" -> JsString("OK"),
    "data" -> JsArray(data)).toJson.prettyPrint

  def jsonResultError(message: String) = Map(
    "status" -> "ERROR",
    "message" -> message).toJson.prettyPrint


  implicit object DateJsonFormat extends RootJsonFormat[Date] {
    private val df: SimpleDateFormat =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    override def write(obj: Date) = JsString(df.format(obj))

    override def read(json: JsValue): Date = json match {
      case JsString(s) => df.parse(s)
      case _ =>
        throw new DeserializationException(s"$json is not a valid date format")
    }
  }

  implicit def dateToJoda(d: Date) = new DateTime(d)

  implicit def publicationJsonFormat: RootJsonFormat[Publication]
  = jsonFormat15(Publication.apply)
}