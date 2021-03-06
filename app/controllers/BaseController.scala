package controllers

import elastic.ElasticClient
import exceptions.MissingRequiredParamException
import models.CerebroRequest
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.Future
import scala.util.control.NonFatal

trait BaseController extends Controller {

  val client: ElasticClient = ElasticClient

  protected val logger = Logger("elastic")

  type RequestProcessor = (CerebroRequest, ElasticClient) => Future[Result]

  final def process(processor: RequestProcessor) = Action.async(parse.json) { request =>
    try {
      processor(CerebroRequest(request.body), client)
    } catch {
      case e: MissingRequiredParamException =>
        Future.successful(Status(400)(Json.obj("error" -> e.getMessage))) // FIXME: proper error handling
      case NonFatal(e) =>
        Future.successful(Status(500)(Json.obj("error" -> "Error"))) // FIXME: proper error handling
    }
  }

}
