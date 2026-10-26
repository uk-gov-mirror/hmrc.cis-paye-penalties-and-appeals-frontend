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

import base.SpecBase
import models.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class CisPayePasConnectorSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val request: FakeRequest[_] = FakeRequest()

  private val testBackendUrl = "http://localhost:9001"

  "CisPayePasConnector" - {

    // CIS
    "createCisAppeal" - {

      "must return CreateAppealResponse when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateAppealRequest(penaltyId = 1)
        val expectedResult = CreateAppealResponse(true)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateAppealResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        val result = connector.createCisAppeal(testRequest).futureValue

        result mustBe expectedResult
        result.created mustBe true
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateAppealRequest(penaltyId = 1)

        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateAppealResponse]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.createCisAppeal(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateAppealRequest(penaltyId = 1)
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateAppealResponse]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.createCisAppeal(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val testRequest = CreateAppealRequest(penaltyId = 1)
        val expectedResult = CreateAppealResponse(created = true)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateAppealResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        connector.createCisAppeal(testRequest)(customHc).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "getCisAppealablePenalties" - {

      val testRequest = GetAppealablePenaltiesRequest(
        hmrcOfficeNumber = "123",
        schemeReferenceNumber = "AB456",
        startRow = 1L,
        endRow = 10L
      )

      val expectedResponse = GetAppealablePenaltiesResponse(
        total = 2,
        appealablePenalties = List(
          GetAppealablePenaltyResponse(
            uniqueId = Some(10001),
            issueDate = Some(LocalDate.of(2023, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
            penaltyType = Some("CIS03"),
            penaltyAmount = Some(BigDecimal(999.23))
          ),
          GetAppealablePenaltyResponse(
            uniqueId = Some(10002),
            issueDate = Some(LocalDate.of(2023, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
            penaltyType = Some("CIS03"),
            penaltyAmount = Some(BigDecimal(999.23))
          )
        )
      )

      "must return GetCisAppealablePenaltiesResponse when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltiesResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResponse)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        val result = connector.getCisAppealablePenalties(testRequest).futureValue

        result mustBe expectedResponse
        result mustBe a[GetAppealablePenaltiesResponse]
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltiesResponse]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.getCisAppealablePenalties(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltiesResponse]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.getCisAppealablePenalties(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltiesResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResponse)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        connector.getCisAppealablePenalties(testRequest)(customHc).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "getCisAppealablePenalty" - {

      val testRequest = GetAppealablePenaltyRequest(
        penaltyId = 1L,
        hmrcOfficeNumber = "840123",
        schemeReferenceNumber = "098345"
      )

      "must return GetCisAppealablePenaltyResponse when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val expectedResult = GetAppealablePenaltyResponse(
          uniqueId = None,
          issueDate = Some(LocalDate.of(2023, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
          penaltyType = Some("CIS003"),
          penaltyAmount = Some(BigDecimal("999.23"))
        )

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltyResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        val result = connector.getCisAppealablePenalty(testRequest).futureValue

        result mustBe expectedResult
        result.issueDate mustBe Some(LocalDate.of(2023, 1, 1))
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltyResponse]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.getCisAppealablePenalty(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltyResponse]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.getCisAppealablePenalty(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val expectedResult = GetAppealablePenaltyResponse(
          uniqueId = None,
          issueDate = Some(LocalDate.of(2023, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
          penaltyType = Some("CIS003"),
          penaltyAmount = Some(BigDecimal("999.23"))
        )

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltyResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        connector.getCisAppealablePenalty(testRequest)(customHc).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "getCisTaxpayer" - {

      val testRequest = GetCisTaxpayerRequest(
        hmrcOfficeNumber = "123",
        schemeReferenceNumber = "AB456"
      )

      val expectedResult = GetCisTaxpayerResponse(
        uniqueId = "1000123",
        taxOfficeNumber = "123",
        taxOfficeRef = "AB456",
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

      "must return GetCisTaxpayerResponse when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetCisTaxpayerResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        val result = connector.getCisTaxpayer(testRequest).futureValue

        result mustBe expectedResult
        result.schemeName mustBe Some("ABC Builders Ltd")
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("CIS taxpayer not found", 404)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetCisTaxpayerResponse]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.getCisTaxpayer(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetCisTaxpayerResponse]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.getCisTaxpayer(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetCisTaxpayerResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        connector.getCisTaxpayer(testRequest)(customHc).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "getCisPenaltyAllowed" - {

      val testRequest = GetPenaltyAllowedRequest(
        districtNumber = 1L,
        employerReference = "HDZ",
        uniqueId = 1L
      )

      val expectedResponse = GetPenaltyAllowedResponse(
        allowed = true
      )

      "must return GetPenaltyAllowedResponse when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetPenaltyAllowedResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResponse)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        val result = connector.getCisPenaltyAllowed(testRequest).futureValue

        result mustBe expectedResponse
        result mustBe a[GetPenaltyAllowedResponse]
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetPenaltyAllowedResponse]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.getCisPenaltyAllowed(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetPenaltyAllowedResponse]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.getCisPenaltyAllowed(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetPenaltyAllowedResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResponse)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        connector.getCisPenaltyAllowed(testRequest)(customHc).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    // PAYE
    "createPayeAppeal" - {

      "must return CreateAppealResponse when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateAppealRequest(penaltyId = 1)
        val expectedResult = CreateAppealResponse(true)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateAppealResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        val result = connector.createPayeAppeal(testRequest).futureValue

        result mustBe expectedResult
        result.created mustBe true
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateAppealRequest(penaltyId = 1)

        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateAppealResponse]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.createPayeAppeal(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateAppealRequest(penaltyId = 1)
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateAppealResponse]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.createPayeAppeal(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val testRequest = CreateAppealRequest(penaltyId = 1)
        val expectedResult = CreateAppealResponse(created = true)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateAppealResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        connector.createPayeAppeal(testRequest)(customHc).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "getPayeAppealablePenalties" - {

      val testRequest = GetAppealablePenaltiesRequest(
        hmrcOfficeNumber = "123",
        schemeReferenceNumber = "AB456",
        startRow = 1L,
        endRow = 10L
      )

      val expectedResponse = GetAppealablePenaltiesResponse(
        total = 2,
        appealablePenalties = List(
          GetAppealablePenaltyResponse(
            uniqueId = Some(10001),
            issueDate = Some(LocalDate.of(2023, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
            penaltyType = Some("PAYE3"),
            penaltyAmount = Some(BigDecimal(999.23))
          ),
          GetAppealablePenaltyResponse(
            uniqueId = Some(10002),
            issueDate = Some(LocalDate.of(2023, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
            penaltyType = Some("PAYE3"),
            penaltyAmount = Some(BigDecimal(999.23))
          )
        )
      )

      "must return GetAppealablePenaltiesResponse when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltiesResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResponse)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        val result = connector.getPayeAppealablePenalties(testRequest).futureValue

        result mustBe expectedResponse
        result mustBe a[GetAppealablePenaltiesResponse]
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltiesResponse]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.getPayeAppealablePenalties(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltiesResponse]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.getPayeAppealablePenalties(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltiesResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResponse)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        connector.getPayeAppealablePenalties(testRequest)(customHc).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }

    "getPayeAppealablePenalty" - {

      val testRequest = GetAppealablePenaltyRequest(
        penaltyId = 1L,
        hmrcOfficeNumber = "840123",
        schemeReferenceNumber = "098345"
      )

      "must return GetPayeAppealablePenaltyResponse when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val expectedResult = GetAppealablePenaltyResponse(
          uniqueId = None,
          issueDate = Some(LocalDate.of(2023, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
          penaltyType = Some("PAYE3"),
          penaltyAmount = Some(BigDecimal("999.23"))
        )

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltyResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        val result = connector.getPayeAppealablePenalty(testRequest).futureValue

        result mustBe expectedResult
        result.issueDate mustBe Some(LocalDate.of(2023, 1, 1))
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]

        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltyResponse]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.getPayeAppealablePenalty(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltyResponse]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.getPayeAppealablePenalty(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val expectedResult = GetAppealablePenaltyResponse(
          uniqueId = None,
          issueDate = Some(LocalDate.of(2023, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
          penaltyType = Some("PAYE3"),
          penaltyAmount = Some(BigDecimal("999.23"))
        )

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetAppealablePenaltyResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        connector.getPayeAppealablePenalty(testRequest)(customHc).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }
  }
  "getPayePenaltyAllowed" - {

    val testRequest = GetPenaltyAllowedRequest(
      districtNumber = 1L,
      employerReference = "HDZ",
      uniqueId = 1L
    )

    val expectedResponse = GetPenaltyAllowedResponse(
      allowed = true
    )

    "must return GetPenaltyAllowedResponse when request is successful" in {
      val mockHttpClient = mock[HttpClientV2]
      val mockConfig = mock[ServicesConfig]
      val mockRequestBuilder = mock[RequestBuilder]

      when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
      when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetPenaltyAllowedResponse]](any(), any()))
        .thenReturn(Future.successful(Right(expectedResponse)))

      val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
      val result = connector.getPayePenaltyAllowed(testRequest).futureValue

      result mustBe expectedResponse
      result mustBe a[GetPenaltyAllowedResponse]
    }

    "must handle Left response with UpstreamErrorResponse" in {
      val mockHttpClient = mock[HttpClientV2]
      val mockConfig = mock[ServicesConfig]
      val mockRequestBuilder = mock[RequestBuilder]

      val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

      when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
      when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetPenaltyAllowedResponse]](any(), any()))
        .thenReturn(Future.successful(Left(upstreamError)))

      val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

      whenReady(connector.getPayePenaltyAllowed(testRequest).failed) { exception =>
        exception mustBe upstreamError
      }
    }

    "must handle general exceptions" in {
      val mockHttpClient = mock[HttpClientV2]
      val mockConfig = mock[ServicesConfig]
      val mockRequestBuilder = mock[RequestBuilder]
      val runtimeException = new RuntimeException("Connection failed")

      when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
      when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetPenaltyAllowedResponse]](any(), any()))
        .thenReturn(Future.failed(runtimeException))

      val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

      whenReady(connector.getPayePenaltyAllowed(testRequest).failed) { exception =>
        exception mustBe a[RuntimeException]
        exception.getMessage mustBe "Connection failed"
      }
    }

    "must pass implicit HeaderCarrier to http client" in {
      val mockHttpClient = mock[HttpClientV2]
      val mockConfig = mock[ServicesConfig]
      val mockRequestBuilder = mock[RequestBuilder]
      val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))

      when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
      when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, GetPenaltyAllowedResponse]](any(), any()))
        .thenReturn(Future.successful(Right(expectedResponse)))

      val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
      connector.getPayePenaltyAllowed(testRequest)(customHc).futureValue

      verify(mockHttpClient, times(1)).post(any())(any())
    }
  }
}
