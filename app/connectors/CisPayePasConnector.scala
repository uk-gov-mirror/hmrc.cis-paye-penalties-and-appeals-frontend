/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import models.*
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpReads.Implicits.*
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CisPayePasConnector @Inject()(http: HttpClientV2,
                                    config: ServicesConfig)
                                   (implicit ec: ExecutionContext) extends Logging {

  private val base = config.baseUrl("cis-paye-penalties-and-appeals") + "/cis-paye-penalties-and-appeals"

  // CIS
  def createCisAppeal(createCisAppealRequest: CreateAppealRequest)(implicit hc: HeaderCarrier): Future[CreateAppealResponse] = {
    http.post(url"$base/cis/appeal")
      .withBody(Json.toJson(createCisAppealRequest))
      .execute[Either[UpstreamErrorResponse, CreateAppealResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][createCisAppeal] Penalty Id: ${createCisAppealRequest.penaltyId}, response: $resp")
          logger.info(s"[CisPayePasConnector][createCisAppeal] Create success with penalty Id: ${createCisAppealRequest.penaltyId}")
          Future.successful(
            resp)
        case Left(error) =>
          logResponse(error, "[CisPayePasConnector][createCisAppeal]")
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[CisPayePasConnector][createCisAppeal]")
      }
  }

  def getCisAppealablePenalties(request: GetAppealablePenaltiesRequest)(implicit hc: HeaderCarrier): Future[GetAppealablePenaltiesResponse] = {
    http.post(url"$base/cis/penalties")
      .withBody(Json.toJson(request))
      .execute[Either[UpstreamErrorResponse, GetAppealablePenaltiesResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][getCisAppealablePenalties] request: ${request}, response: $resp")
          logger.info(s"[CisPayePasConnector][getCisAppealablePenalties] Get success with request: ${request}")
          Future.successful(resp)
        case Left(error) =>
          logResponse(error, "[CisPayePasConnector][getCisAppealablePenalties]")
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[CisPayePasConnector][getCisAppealablePenalties]")
      }
  }

  def getCisAppealablePenalty(getCisAppealablePenaltyRequest: GetAppealablePenaltyRequest)
                             (implicit hc: HeaderCarrier): Future[GetAppealablePenaltyResponse] = {
    http.post(url"$base/cis/penalty")
      .withBody(Json.toJson(getCisAppealablePenaltyRequest))
      .execute[Either[UpstreamErrorResponse, GetAppealablePenaltyResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][getCisAppealablePenalty] request: $getCisAppealablePenaltyRequest, response: $resp")
          logger.info(s"[CisPayePasConnector][getCisAppealablePenalty] Get success with request: $getCisAppealablePenaltyRequest")
          Future.successful(
            resp)
        case Left(error) =>
          logResponse(error, "[CisPayePasConnector][getCisAppealablePenalty]")
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[CisPayePasConnector][getCisAppealablePenalty]")
      }
  }

  def getCisTaxpayer(getCisTaxpayerRequest: GetCisTaxpayerRequest)
                    (implicit hc: HeaderCarrier): Future[GetCisTaxpayerResponse] = {
    http.post(url"$base/cis/taxpayer")
      .withBody(Json.toJson(getCisTaxpayerRequest))
      .execute[Either[UpstreamErrorResponse, GetCisTaxpayerResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][getCisTaxpayer] request: $getCisTaxpayerRequest, response: $resp")
          logger.info(s"[CisPayePasConnector][getCisTaxpayer] Get success with request: $getCisTaxpayerRequest")
          Future.successful(resp)
        case Left(error) =>
          logResponse(error, "[CisPayePasConnector][getCisTaxpayer]")
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[CisPayePasConnector][getCisTaxpayer]")
      }
  }

  def getCisPenaltyAllowed(getPenaltyAllowedRequest: GetPenaltyAllowedRequest)
                             (implicit hc: HeaderCarrier): Future[GetPenaltyAllowedResponse] = {
    http.post(url"$base/cis/penalties/access-allowed")
      .withBody(Json.toJson(getPenaltyAllowedRequest))
      .execute[Either[UpstreamErrorResponse, GetPenaltyAllowedResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][getCisPenaltyAllowed] request: $getPenaltyAllowedRequest, response: $resp")
          logger.info(s"[CisPayePasConnector][getCisPenaltyAllowed] Get success with request: $getPenaltyAllowedRequest")
          Future.successful(
            resp)
        case Left(error) =>
          logResponse(error, "[CisPayePasConnector][getCisPenaltyAllowed]")
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[CisPayePasConnector][getCisPenaltyAllowed]")
      }
  }

  // PAYE
  def createPayeAppeal(createPayeAppealRequest: CreateAppealRequest)(implicit hc: HeaderCarrier): Future[CreateAppealResponse] = {
    http.post(url"$base/paye/appeal")
      .withBody(Json.toJson(createPayeAppealRequest))
      .execute[Either[UpstreamErrorResponse, CreateAppealResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][createPayeAppeal] Penalty Id: ${createPayeAppealRequest.penaltyId}, response: $resp")
          logger.info(s"[CisPayePasConnector][createPayeAppeal] Create success with penalty Id: ${createPayeAppealRequest.penaltyId}")
          Future.successful(
            resp)
        case Left(error) =>
          logResponse(error, "[CisPayePasConnector][createPayeAppeal]")
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[CisPayePasConnector][createPayeAppeal]")
      }
  }

  def getPayeAppealablePenalties(request: GetAppealablePenaltiesRequest)(implicit hc: HeaderCarrier): Future[GetAppealablePenaltiesResponse] = {
    http.post(url"$base/paye/penalties")
      .withBody(Json.toJson(request))
      .execute[Either[UpstreamErrorResponse, GetAppealablePenaltiesResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][getPayeAppealablePenalties] request: ${request}, response: $resp")
          logger.info(s"[CisPayePasConnector][getPayeAppealablePenalties] Get success with request: ${request}")
          Future.successful(resp)
        case Left(error) =>
          logResponse(error, "[CisPayePasConnector][getPayeAppealablePenalties]")
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[CisPayePasConnector][getPayeAppealablePenalties]")
      }
  }

  def getPayeAppealablePenalty(getPayeAppealablePenaltyRequest: GetAppealablePenaltyRequest)
                             (implicit hc: HeaderCarrier): Future[GetAppealablePenaltyResponse] = {
    http.post(url"$base/paye/penalty")
      .withBody(Json.toJson(getPayeAppealablePenaltyRequest))
      .execute[Either[UpstreamErrorResponse, GetAppealablePenaltyResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][getPayeAppealablePenalty] request: $getPayeAppealablePenaltyRequest, response: $resp")
          logger.info(s"[CisPayePasConnector][getPayeAppealablePenalty] Get success with request: $getPayeAppealablePenaltyRequest")
          Future.successful(
            resp)
        case Left(error) =>
          logResponse(error, "[CisPayePasConnector][getPayeAppealablePenalty]")
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[CisPayePasConnector][getPayeAppealablePenalty]")
      }
  }

  def getPayePenaltyAllowed(getPenaltyAllowedRequest: GetPenaltyAllowedRequest)
                          (implicit hc: HeaderCarrier): Future[GetPenaltyAllowedResponse] = {
    http.post(url"$base/paye/penalties/access-allowed")
      .withBody(Json.toJson(getPenaltyAllowedRequest))
      .execute[Either[UpstreamErrorResponse, GetPenaltyAllowedResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][getPayePenaltyAllowed] request: $getPenaltyAllowedRequest, response: $resp")
          logger.info(s"[CisPayePasConnector][getPayePenaltyAllowed] Get success with request: $getPenaltyAllowedRequest")
          Future.successful(
            resp)
        case Left(error) =>
          logResponse(error, "[CisPayePasConnector][getPayePenaltyAllowed]")
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[CisPayePasConnector][getPayePenaltyAllowed]")
      }
  }

  private def logResponse(e: Throwable, method: String): Throwable = {
    logger.error(s"[$method] Error occurred: ${e.getMessage}", e)
    e
  }
}
