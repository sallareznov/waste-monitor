package models

import play.api.libs.json.{JsObject, Json}

case class RouteInformations(description: String, route: String, verb: String, headers: Option[String], urlParameters: Option[JsObject], body: Option[JsObject])

object RouteInformations {

  implicit val routeJSONInformationsReads = Json.reads[RouteInformations]
  implicit val routeJSONInformationsWrites = Json.writes[RouteInformations]
  implicit val routeJSONInformationsFormat = Json.format[RouteInformations]

}