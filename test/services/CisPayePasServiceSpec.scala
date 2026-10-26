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

import base.SpecBase
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import connectors.CisPayePasConnector
import models.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import viewmodels.appealablePenalties.{AppealablePenaltiesViewModel, AppealablePenaltiesViewModelRow}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class CisPayePasServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val request: FakeRequest[_] = FakeRequest()

  private val mockBackendConnector = mock[CisPayePasConnector]

  private val service = new CisPayePasService(mockBackendConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockBackendConnector)
  }

  "CisPayePasService" - {

    // CIS
    "createCisAppeal" - {

      val createCisAppealRequest = CreateAppealRequest(1L)
      val createCisAppealResponse = CreateAppealResponse(true)

      "must return CreateAppealResponse when penaltyId provided" in {
        when(mockBackendConnector.createCisAppeal(eqTo(createCisAppealRequest))(any()))
          .thenReturn(Future.successful(createCisAppealResponse))

        val result = service.createCisAppeal(createCisAppealRequest.penaltyId).futureValue

        result mustBe createCisAppealResponse
        result.created mustBe true
        verify(mockBackendConnector, times(1)).createCisAppeal(eqTo(createCisAppealRequest))(any())
      }

      "must propagate connector failure" in {
        when(mockBackendConnector.createCisAppeal(eqTo(createCisAppealRequest))(any()))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        whenReady(service.createCisAppeal(createCisAppealRequest.penaltyId).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }
        verify(mockBackendConnector, times(1)).createCisAppeal(eqTo(createCisAppealRequest))(any())
      }
    }

    "getCisAppealablePenalties" - {

      val testRequest = GetAppealablePenaltiesRequest(
        hmrcOfficeNumber = "123",
        schemeReferenceNumber = "AB456",
        startRow = 1L,
        endRow = 10L
      )

      val testResponse = GetAppealablePenaltiesResponse(
        total = 2,
        appealablePenalties = List(
          GetAppealablePenaltyResponse(
            uniqueId = Some(10001),
            issueDate = Some(LocalDate.of(2023, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
            penaltyType = Some("CIS03"),
            penaltyAmount = Some(BigDecimal(999.23))
          )
        )
      )

      val expectedResponse = AppealablePenaltiesViewModel(
        total = 2,
        rows = List(
          AppealablePenaltiesViewModelRow(
            uniqueId = Some(10001),
            issueDate = Some(LocalDate.of(2023, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
            typeOfPenalty = Some("CIS03"),
            amount = Some(BigDecimal(999.23))
          )
        )
      )

      "must return AppealablePenaltiesViewModel when successful" in {
        when(mockBackendConnector.getCisAppealablePenalties(eqTo(testRequest))(any[HeaderCarrier]))
          .thenReturn(Future.successful(testResponse))

        val result = service.getCisAppealablePenalties(testRequest).futureValue

        result mustBe expectedResponse
        verify(mockBackendConnector, times(1)).getCisAppealablePenalties(eqTo(testRequest))(any[HeaderCarrier])
      }

      "must propagate connector failure" in {
        when(mockBackendConnector.getCisAppealablePenalties(eqTo(testRequest))(any[HeaderCarrier]))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        whenReady(service.getCisAppealablePenalties(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }
        verify(mockBackendConnector, times(1)).getCisAppealablePenalties(eqTo(testRequest))(any[HeaderCarrier])
      }
    }

    "getCisAppealablePenalty" - {

      val getCisAppealablePenaltyRequest = GetAppealablePenaltyRequest(
        penaltyId = 1L,
        hmrcOfficeNumber = "840123",
        schemeReferenceNumber = "098345"
      )

      val getCisAppealablePenaltyResponse = GetAppealablePenaltyResponse(
        uniqueId = None,
        issueDate = Some(LocalDate.of(2023, 1, 1)),
        taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
        penaltyType = Some("CIS003"),
        penaltyAmount = Some(BigDecimal("999.23"))
      )

      "must return GetCisAppealablePenaltyResponse when request provided" in {
        when(mockBackendConnector.getCisAppealablePenalty(eqTo(getCisAppealablePenaltyRequest))(any()))
          .thenReturn(Future.successful(getCisAppealablePenaltyResponse))

        val result = service.getCisAppealablePenalty(getCisAppealablePenaltyRequest).futureValue

        result mustBe getCisAppealablePenaltyResponse
        result.issueDate mustBe Some(LocalDate.of(2023, 1, 1))
        verify(mockBackendConnector, times(1)).getCisAppealablePenalty(eqTo(getCisAppealablePenaltyRequest))(any())
      }

      "must propagate connector failure" in {
        when(mockBackendConnector.getCisAppealablePenalty(eqTo(getCisAppealablePenaltyRequest))(any()))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        whenReady(service.getCisAppealablePenalty(getCisAppealablePenaltyRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }
        verify(mockBackendConnector, times(1)).getCisAppealablePenalty(eqTo(getCisAppealablePenaltyRequest))(any())
      }
    }

    "getCisTaxpayer" - {

      val getCisTaxpayerRequest = GetCisTaxpayerRequest(
        hmrcOfficeNumber = "123",
        schemeReferenceNumber = "AB456"
      )

      def taxpayerResponse(uniqueId: String, taxOfficeRef: String): GetCisTaxpayerResponse =
        GetCisTaxpayerResponse(
          uniqueId = uniqueId,
          taxOfficeNumber = "123",
          taxOfficeRef = taxOfficeRef,
          aoDistrict = Some("123"),
          aoPayType = Some("P"),
          aoCheckCode = Some("X"),
          aoReference = Some("00123456"),
          validBusinessAddr = None,
          correlation = None,
          ggAgentId = None,
          employerName1 = Some("ABC"),
          employerName2 = None,
          agentOwnRef = None,
          schemeName = Some("ABC Builders Ltd"),
          utr = Some("1234567890"),
          enrolledSig = None
        )

      val getCisTaxpayerResponse = taxpayerResponse("1000123", "AB456")

      "must return GetCisTaxpayerResponse when request provided" in {
        when(mockBackendConnector.getCisTaxpayer(eqTo(getCisTaxpayerRequest))(any()))
          .thenReturn(Future.successful(getCisTaxpayerResponse))

        val result = service.getCisTaxpayer(getCisTaxpayerRequest).futureValue

        result mustBe getCisTaxpayerResponse
        result.schemeName mustBe Some("ABC Builders Ltd")
        verify(mockBackendConnector, times(1)).getCisTaxpayer(eqTo(getCisTaxpayerRequest))(any())
      }

      "must propagate a 404 UpstreamErrorResponse unchanged" in {
        val notFound = UpstreamErrorResponse("CIS taxpayer not found", 404)
        when(mockBackendConnector.getCisTaxpayer(eqTo(getCisTaxpayerRequest))(any()))
          .thenReturn(Future.failed(notFound))

        whenReady(service.getCisTaxpayer(getCisTaxpayerRequest).failed) { exception =>
          exception mustBe notFound
          exception.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 404
        }
        verify(mockBackendConnector, times(1)).getCisTaxpayer(eqTo(getCisTaxpayerRequest))(any())
      }

      "must propagate connector failure" in {
        when(mockBackendConnector.getCisTaxpayer(eqTo(getCisTaxpayerRequest))(any()))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        whenReady(service.getCisTaxpayer(getCisTaxpayerRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }
        verify(mockBackendConnector, times(1)).getCisTaxpayer(eqTo(getCisTaxpayerRequest))(any())
      }

      "must handle consecutive requests independently" in {
        val secondRequest = GetCisTaxpayerRequest(hmrcOfficeNumber = "456", schemeReferenceNumber = "CD789")
        val secondResponse = taxpayerResponse("2000456", "CD789")

        when(mockBackendConnector.getCisTaxpayer(eqTo(getCisTaxpayerRequest))(any()))
          .thenReturn(Future.successful(getCisTaxpayerResponse))
        when(mockBackendConnector.getCisTaxpayer(eqTo(secondRequest))(any()))
          .thenReturn(Future.successful(secondResponse))

        service.getCisTaxpayer(getCisTaxpayerRequest).futureValue mustBe getCisTaxpayerResponse
        service.getCisTaxpayer(secondRequest).futureValue mustBe secondResponse

        verify(mockBackendConnector, times(1)).getCisTaxpayer(eqTo(getCisTaxpayerRequest))(any())
        verify(mockBackendConnector, times(1)).getCisTaxpayer(eqTo(secondRequest))(any())
      }
    }

    "getCisPenaltyAllowed" - {

      val testRequest = GetPenaltyAllowedRequest(
        districtNumber = 1L,
        employerReference = "HDZ",
        uniqueId = 1L
      )

      val testResponse = GetPenaltyAllowedResponse(
        allowed = true
      )

      "must return GetPenaltyAllowedResponse when successful" in {
        when(mockBackendConnector.getCisPenaltyAllowed(eqTo(testRequest))(any[HeaderCarrier]))
          .thenReturn(Future.successful(testResponse))

        val result = service.getCisPenaltyAllowed(testRequest).futureValue

        result mustBe testResponse
        verify(mockBackendConnector, times(1)).getCisPenaltyAllowed(eqTo(testRequest))(any[HeaderCarrier])
      }

      "must propagate connector failure" in {
        when(mockBackendConnector.getCisPenaltyAllowed(eqTo(testRequest))(any[HeaderCarrier]))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        whenReady(service.getCisPenaltyAllowed(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }
        verify(mockBackendConnector, times(1)).getCisPenaltyAllowed(eqTo(testRequest))(any[HeaderCarrier])
      }
    }
  }

  // PAYE
  "createPayeAppeal" - {

    val createPayeAppealRequest = CreateAppealRequest(1L)
    val createPayeAppealResponse = CreateAppealResponse(true)

    "must return CreateAppealResponse when penaltyId provided" in {
      when(mockBackendConnector.createPayeAppeal(eqTo(createPayeAppealRequest))(any()))
        .thenReturn(Future.successful(createPayeAppealResponse))

      val result = service.createPayeAppeal(createPayeAppealRequest.penaltyId).futureValue

      result mustBe createPayeAppealResponse
      result.created mustBe true
      verify(mockBackendConnector, times(1)).createPayeAppeal(eqTo(createPayeAppealRequest))(any())
    }

    "must propagate connector failure" in {
      when(mockBackendConnector.createPayeAppeal(eqTo(createPayeAppealRequest))(any()))
        .thenReturn(Future.failed(new RuntimeException("Connector failed")))

      whenReady(service.createPayeAppeal(createPayeAppealRequest.penaltyId).failed) { exception =>
        exception mustBe a[RuntimeException]
        exception.getMessage mustBe "Connector failed"
      }
      verify(mockBackendConnector, times(1)).createPayeAppeal(eqTo(createPayeAppealRequest))(any())
    }
  }

  "getPayeAppealablePenalties" - {

    val testRequest = GetAppealablePenaltiesRequest(
      hmrcOfficeNumber = "123",
      schemeReferenceNumber = "AB456",
      startRow = 1L,
      endRow = 10L
    )

    val testResponse = GetAppealablePenaltiesResponse(
      total = 2,
      appealablePenalties = List(
        GetAppealablePenaltyResponse(
          uniqueId = Some(10001),
          issueDate = Some(LocalDate.of(2023, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
          penaltyType = Some("CIS03"),
          penaltyAmount = Some(BigDecimal(999.23))
        )
      )
    )

    val expectedResponse = AppealablePenaltiesViewModel(
      total = 2,
      rows = List(
        AppealablePenaltiesViewModelRow(
          uniqueId = Some(10001),
          issueDate = Some(LocalDate.of(2023, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
          typeOfPenalty = Some("CIS03"),
          amount = Some(BigDecimal(999.23))
        )
      )
    )

    "must return AppealablePenaltiesViewModel when successful" in {
      when(mockBackendConnector.getPayeAppealablePenalties(eqTo(testRequest))(any[HeaderCarrier]))
        .thenReturn(Future.successful(testResponse))

      val result = service.getPayeAppealablePenalties(testRequest).futureValue

      result mustBe expectedResponse
      verify(mockBackendConnector, times(1)).getPayeAppealablePenalties(eqTo(testRequest))(any[HeaderCarrier])
    }

    "must propagate connector failure" in {
      when(mockBackendConnector.getPayeAppealablePenalties(eqTo(testRequest))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("Connector failed")))

      whenReady(service.getPayeAppealablePenalties(testRequest).failed) { exception =>
        exception mustBe a[RuntimeException]
        exception.getMessage mustBe "Connector failed"
      }
      verify(mockBackendConnector, times(1)).getPayeAppealablePenalties(eqTo(testRequest))(any[HeaderCarrier])
    }
  }

  "getPayeAppealablePenalty" - {

    val getPayeAppealablePenaltyRequest = GetAppealablePenaltyRequest(
      penaltyId = 1L,
      hmrcOfficeNumber = "840123",
      schemeReferenceNumber = "098345"
    )

    val getPayeAppealablePenaltyResponse = GetAppealablePenaltyResponse(
      uniqueId = None,
      issueDate = Some(LocalDate.of(2023, 1, 1)),
      taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
      penaltyType = Some("CIS003"),
      penaltyAmount = Some(BigDecimal("999.23"))
    )

    "must return GetPayeAppealablePenaltyResponse when request provided" in {
      when(mockBackendConnector.getPayeAppealablePenalty(eqTo(getPayeAppealablePenaltyRequest))(any()))
        .thenReturn(Future.successful(getPayeAppealablePenaltyResponse))

      val result = service.getPayeAppealablePenalty(getPayeAppealablePenaltyRequest).futureValue

      result mustBe getPayeAppealablePenaltyResponse
      result.issueDate mustBe Some(LocalDate.of(2023, 1, 1))
      verify(mockBackendConnector, times(1)).getPayeAppealablePenalty(eqTo(getPayeAppealablePenaltyRequest))(any())
    }

    "must propagate connector failure" in {
      when(mockBackendConnector.getPayeAppealablePenalty(eqTo(getPayeAppealablePenaltyRequest))(any()))
        .thenReturn(Future.failed(new RuntimeException("Connector failed")))

      whenReady(service.getPayeAppealablePenalty(getPayeAppealablePenaltyRequest).failed) { exception =>
        exception mustBe a[RuntimeException]
        exception.getMessage mustBe "Connector failed"
      }
      verify(mockBackendConnector, times(1)).getPayeAppealablePenalty(eqTo(getPayeAppealablePenaltyRequest))(any())
    }
  }

  "getPayePenaltyAllowed" - {

    val testRequest = GetPenaltyAllowedRequest(
      districtNumber = 1L,
      employerReference = "HDZ",
      uniqueId = 1L
    )

    val testResponse = GetPenaltyAllowedResponse(
      allowed = true
    )

    "must return GetPenaltyAllowedResponse when successful" in {
      when(mockBackendConnector.getPayePenaltyAllowed(eqTo(testRequest))(any[HeaderCarrier]))
        .thenReturn(Future.successful(testResponse))

      val result = service.getPayePenaltyAllowed(testRequest).futureValue

      result mustBe testResponse
      verify(mockBackendConnector, times(1)).getPayePenaltyAllowed(eqTo(testRequest))(any[HeaderCarrier])
    }

    "must propagate connector failure" in {
      when(mockBackendConnector.getPayePenaltyAllowed(eqTo(testRequest))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("Connector failed")))

      whenReady(service.getPayePenaltyAllowed(testRequest).failed) { exception =>
        exception mustBe a[RuntimeException]
        exception.getMessage mustBe "Connector failed"
      }
      verify(mockBackendConnector, times(1)).getPayePenaltyAllowed(eqTo(testRequest))(any[HeaderCarrier])
    }
  }
}
