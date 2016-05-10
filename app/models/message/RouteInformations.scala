package models.message

import play.api.libs.json.{JsValue, Json}

/**
  * Informations about a route
  * @param description the description
  * @param route the path
  * @param verb the verb
  * @param headers the headers
  * @param urlParameters the parameters in the path
  * @param body the body
  */
case class RouteInformations(description: String, route: String, verb: String, headers: Map[String, String], urlParameters: JsValue, queryParameters: JsValue, body: JsValue, returnCodes: JsValue)

object RouteInformations {

  implicit val routeJSONInformationsReads = Json.reads[RouteInformations]
  implicit val routeJSONInformationsWrites = Json.writes[RouteInformations]
  implicit val routeJSONInformationsFormat = Json.format[RouteInformations]

}