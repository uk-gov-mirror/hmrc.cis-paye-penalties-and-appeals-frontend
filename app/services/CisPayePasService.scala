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

package services


import connectors.CisPayePasConnector
import models.*
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.appealablePenalties.{AppealablePenaltiesViewModel, AppealablePenaltiesViewModelRow}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CisPayePasService @Inject()(backendConnector: CisPayePasConnector)(implicit ec: ExecutionContext) extends Logging {

  // CIS
  def createCisAppeal(penaltyId: Long)
                     (implicit hc: HeaderCarrier): Future[CreateAppealResponse] = {
    logger.debug("[CisPayePasService][createCisAppeal] Creating Cis Appeal")
    val createCisAppealRequest = CreateAppealRequest(penaltyId)
    backendConnector.createCisAppeal(createCisAppealRequest)
  }

  def getCisAppealablePenalties(getCisAppealablePenaltiesRequest: GetAppealablePenaltiesRequest)
                               (implicit hc: HeaderCarrier): Future[AppealablePenaltiesViewModel] = {
    logger.debug("[CisPayePasService][getCisAppealablePenalties] Getting appealable penalties")
    for {
      appealablePenalties <- backendConnector.getCisAppealablePenalties(getCisAppealablePenaltiesRequest)
    } yield AppealablePenaltiesViewModel(
      total = appealablePenalties.total,
      rows  = appealablePenalties.appealablePenalties.map { penalty =>
        AppealablePenaltiesViewModelRow(
          uniqueId         = penalty.uniqueId,
          issueDate        = penalty.issueDate,
          taxPeriodEndDate = penalty.taxPeriodEndDate,
          typeOfPenalty    = penalty.penaltyType,
          amount           = penalty.penaltyAmount
        )
      }
    )
  }

  def getCisAppealablePenalty(getCisAppealablePenaltyRequest: GetAppealablePenaltyRequest)
                     (implicit hc: HeaderCarrier): Future[GetAppealablePenaltyResponse] = {
    logger.debug("[CisPayePasService][getCisAppealablePenaly] Getting Cis Appealable Penalty")
    backendConnector.getCisAppealablePenalty(getCisAppealablePenaltyRequest)
  }

  def getCisTaxpayer(getCisTaxpayerRequest: GetCisTaxpayerRequest)
                    (implicit hc: HeaderCarrier): Future[GetCisTaxpayerResponse] = {
    logger.debug("[CisPayePasService][getCisTaxpayer] Getting Cis Taxpayer")
    backendConnector.getCisTaxpayer(getCisTaxpayerRequest)
  }

  def getCisPenaltyAllowed(getPenaltyAllowedRequest: GetPenaltyAllowedRequest)
                             (implicit hc: HeaderCarrier): Future[GetPenaltyAllowedResponse] = {
    logger.debug("[CisPayePasService][getCisPenaltyAllowed] Getting Cis Penalty Allowed")
    backendConnector.getCisPenaltyAllowed(getPenaltyAllowedRequest)
  }

  // PAYE
  def createPayeAppeal(penaltyId: Long)
                     (implicit hc: HeaderCarrier): Future[CreateAppealResponse] = {
    logger.debug("[CisPayePasService][createPayeAppeal] Creating Paye Appeal")
    val createPayeAppealRequest = CreateAppealRequest(penaltyId)
    backendConnector.createPayeAppeal(createPayeAppealRequest)
  }

  def getPayeAppealablePenalties(request: GetAppealablePenaltiesRequest)
                               (implicit hc: HeaderCarrier): Future[AppealablePenaltiesViewModel] = {
    logger.debug("[CisPayePasService][getPayeAppealablePenalties] Getting appealable penalties")
    for {
      penalties <- backendConnector.getPayeAppealablePenalties(request)
    } yield AppealablePenaltiesViewModel(
      total = penalties.total,
      rows = penalties.appealablePenalties.map { penalty =>
        AppealablePenaltiesViewModelRow(
          uniqueId = penalty.uniqueId,
          issueDate = penalty.issueDate,
          taxPeriodEndDate = penalty.taxPeriodEndDate,
          typeOfPenalty = penalty.penaltyType,
          amount = penalty.penaltyAmount
        )
      }
    )
  }

  def getPayeAppealablePenalty(getPayeAppealablePenaltyRequest: GetAppealablePenaltyRequest)
                             (implicit hc: HeaderCarrier): Future[GetAppealablePenaltyResponse] = {
    logger.debug("[CisPayePasService][getPayeAppealablePenaly] Getting Paye Appealable Penalty")
    backendConnector.getPayeAppealablePenalty(getPayeAppealablePenaltyRequest)
  }


  def getPayePenaltyAllowed(getPenaltyAllowedRequest: GetPenaltyAllowedRequest)
                          (implicit hc: HeaderCarrier): Future[GetPenaltyAllowedResponse] = {
    logger.debug("[CisPayePasService][getPayePenaltyAllowed] Getting Paye Penalty Allowed")
    backendConnector.getPayePenaltyAllowed(getPenaltyAllowedRequest)
  }
}
