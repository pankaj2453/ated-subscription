/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.services

import java.util.UUID

import connectors.{ETMPConnector, GovernmentGatewayAdminConnector}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.SubscribeService
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.logging.SessionId

import scala.concurrent.Future

class SubscribeServiceSpec extends PlaySpec with OneServerPerSuite with MockitoSugar with BeforeAndAfterEach {

  val mockEtmpConnector = mock[ETMPConnector]
  val mockggAdminConnector = mock[GovernmentGatewayAdminConnector]

  object TestSubscribeServiceSpec extends SubscribeService {
    override val ggAdminConnector = mockggAdminConnector
    override val etmpConnector = mockEtmpConnector
  }


  override def beforeEach(): Unit = {
    reset(mockEtmpConnector)
  }

  "SubscribeService" must {

    val inputJson = Json.parse(
      """
        |{
        |  "safeId": "XE0001234567890",
        |  "address": {
        |    "addressLine1": "100 SuttonStreet",
        |    "addressLine2": "Wokingham",
        |    "postalCode": "AB12CD",
        |    "countryCode": "GB"
        |  },
        |  "contactDetails": {
        |    "telephone": "01332752856",
        |    "mobile": "07782565326",
        |    "fax": "01332754256",
        |    "email": "aa@aa.com"
        |  }
        |}
      """.stripMargin
    )

    val successResponse = Json.parse(
      """
        |{
        |  "processingDate": "2001-12-17T09:30:47Z",
        |  "atedRefNumber": "ABCDEabcde12345",
        |  "formBundleNumber": "123456789012345"
        |}
      """.stripMargin
    )

    val failureResponse = Json.parse(
      """
        |{
        |  "Reason": "Your submission contains one or more errors."
        |}
      """.stripMargin
    )

    implicit val hc = new HeaderCarrier(sessionId = Some(SessionId(s"session-${UUID.randomUUID}")))

    "use the correct connectors" in {
      SubscribeService.ggAdminConnector must be(GovernmentGatewayAdminConnector)
      SubscribeService.etmpConnector must be(ETMPConnector)
    }

    "subscribe when we are passed valid json" in {

      when(mockEtmpConnector.subscribeAted(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, Some(successResponse))))
      when(mockggAdminConnector.addKnownFacts(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, Some(successResponse))))
      val result = TestSubscribeServiceSpec.subscribe(inputJson)
      val response = await(result)
      response.status must be(OK)
      response.json must be(successResponse)
    }

    "subscribe when we are passed valid json with no ated ref" in {

      val successResponseNoAted = Json.parse( """{"processingDate": "2001-12-17T09:30:47Z", "formBundleNumber": "123456789012345"}""")
      when(mockEtmpConnector.subscribeAted(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, Some(successResponseNoAted))))
      when(mockggAdminConnector.addKnownFacts(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, Some(successResponse))))
      val result = TestSubscribeServiceSpec.subscribe(inputJson)
      val response = await(result)
      response.status must be(OK)
      response.json must be(successResponseNoAted)
    }


    "respond with BadRequest, when subscription request fails with a Bad request" in {
      when(mockEtmpConnector.subscribeAted(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, Some(failureResponse))))
      val result = TestSubscribeServiceSpec.subscribe(inputJson)
      val response = await(result)
      response.status must be(BAD_REQUEST)
      response.json must be(failureResponse)
    }

    "respond with an OK, when subscription works but gg admin request fails with a Bad request" in {
      when(mockEtmpConnector.subscribeAted(Matchers.any())(Matchers.any())).thenReturn(Future.successful(HttpResponse(OK, Some(successResponse))))
      when(mockggAdminConnector.addKnownFacts(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, Some(failureResponse))))
      val result = TestSubscribeServiceSpec.subscribe(inputJson)
      val response = await(result)
      response.status must be(OK)
      response.json must be(successResponse)
    }
  }
}