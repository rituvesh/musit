/*
 * MUSIT is a museum database to archive natural and cultural history data.
 * Copyright (C) 2016  MUSIT Norway, part of www.uio.no (University of Oslo)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package controllers

import no.uio.musit.security.BearerToken
import no.uio.musit.test.{FakeUsers, MusitSpecWithServerPerSuite}
import play.api.http.Status
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import play.api.libs.ws.WSResponse
import utils.testdata.ActorJsonGenerator._

import scala.concurrent.Future

class OrganisationAddressControllerIntegrationSpec extends MusitSpecWithServerPerSuite {

  val fakeToken = BearerToken(FakeUsers.testUserToken)

  def postOrganizationAddress(orgId: Int, json: JsValue): Future[WSResponse] = {
    wsUrl(s"/organisation/$orgId/address").withHeaders(fakeToken.asHeader).post(json)
  }

  "The OrganisationAddressController" must {

    "get by id" in {
      val res = wsUrl("/organisation/1/address/1")
        .withHeaders(fakeToken.asHeader)
        .get()
        .futureValue

      res.status mustBe Status.OK
      val addr = res.json
      (addr \ "id").as[Int] mustBe 1
      (addr \ "organisationId").as[Int] mustBe 1
    }
    "negative get by id" in {
      val res = wsUrl("/organisation/1/address/999")
        .withHeaders(fakeToken.asHeader)
        .get()
        .futureValue
      (res.json \ "message").as[String] mustBe "Did not find object with id: 999"
    }
    "get all addresses for an organisation" in {
      val res = wsUrl("/organisation/1/address")
        .withHeaders(fakeToken.asHeader)
        .get()
        .futureValue
      res.json.as[JsArray].value.length mustBe 1
    }
    "create address" in {
      val reqBody = orgAddressJson
      val res     = postOrganizationAddress(1, reqBody).futureValue
      res.status mustBe Status.CREATED

      (res.json \ "id").asOpt[Int] must not be None
      (res.json \ "organisationId").as[Int] mustBe 1
      (res.json \ "addressType").as[String] mustBe "TEST"
      (res.json \ "streetAddress").as[String] mustBe "Foo street 2"
    }

    "not create organisationAddress with illegal input" in {
      val res = postOrganizationAddress(1, orgAddressIllegalJson).futureValue
      res.status mustBe Status.BAD_REQUEST
    }

    "update address" in {
      val reqBody = orgAddressJson.as[JsObject] ++ Json.obj(
        "id"             -> 20,
        "organisationId" -> 1,
        "streetAddress"  -> "Foo street 3",
        "locality"       -> "Bar place",
        "postalCode"     -> "0001",
        "countryName"    -> "Norway",
        "latitude"       -> 70,
        "longitude"      -> 12
      )

      val res = wsUrl("/organisation/1/address/20")
        .withHeaders(fakeToken.asHeader)
        .put(reqBody)
        .futureValue
      (res.json \ "message").as[String] mustBe "Record was updated!"
    }

    "not update address with illegal id" in {
      val reqBody = orgAddressJson.as[JsObject] ++ Json.obj("id" -> 999)
      val res = wsUrl("/organisation/1/address/999")
        .withHeaders(fakeToken.asHeader)
        .put(reqBody)
        .futureValue
      res.status mustBe Status.OK
      (res.json \ "message").as[String] mustBe "No records were updated!"
    }

    "not update address with illegal json" in {
      val reqBody = Json.obj(
        "id"             -> 2,
        "organisationId" -> 1,
        "adresseType"    -> "TEST",
        "stretAddress"   -> "Foo street 3",
        "locality"       -> "Bar place",
        "postalCode"     -> "0001",
        "countryName"    -> "Norway",
        "latitude"       -> 0.0,
        "longitude"      -> 0.0
      )
      val res = wsUrl("/organisation/1/address/2")
        .withHeaders(fakeToken.asHeader)
        .put(reqBody)
        .futureValue
      res.status mustBe Status.BAD_REQUEST
    }

    "delete address" in {
      val res = wsUrl("/organisation/1/address/20")
        .withHeaders(fakeToken.asHeader)
        .delete()
        .futureValue
      (res.json \ "message").as[String] mustBe "Deleted 1 record(s)."
    }
  }

}
