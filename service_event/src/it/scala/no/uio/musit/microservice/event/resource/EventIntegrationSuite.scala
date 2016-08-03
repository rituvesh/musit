/*
 *   MUSIT is a cooperation between the university museums of Norway.
 *   Copyright (C) 2016  MUSIT Norway, part of www.uio.no (University of Oslo)
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License,
 *   or any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License along
 *   with this program; if not, write to the Free Software Foundation, Inc.,
 *   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package no.uio.musit.microservice.event.resource

import no.uio.musit.microservice.event.domain._
import no.uio.musit.microservice.event.service._
import no.uio.musit.microservices.common.PlayTestDefaults
import no.uio.musit.microservices.common.PlayTestDefaults._
import no.uio.musit.microservices.common.extensions.PlayExtensions._
import no.uio.musit.microservices.common.extensions.EitherExtensions._
import no.uio.musit.microservices.common.utils.Misc._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{OneServerPerSuite, PlaySpec}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.WS

/**
  * Created by jstabel on 6/10/16.
  */


class EventIntegrationSuite extends PlaySpec with OneServerPerSuite with ScalaFutures {
  val timeout = PlayTestDefaults.timeout
  override lazy val port: Int = 8080
  implicit override lazy val app = new GuiceApplicationBuilder().configure(PlayTestDefaults.inMemoryDatabaseConfig()).build()


  def createEvent(json: String) = {
    WS.url(s"http://localhost:$port/v1/event").postJsonString(json) |> waitFutureValue
  }


  def getEvent(id: Long) = {
    WS.url(s"http://localhost:$port/v1/event/$id").get |> waitFutureValue
  }

  def validateEvent[T <: Event](jsObject: JsValue) = {
    JsonEventHelpers.eventFromJson[T](jsObject).getOrFail
  }

  /*
  def getAndValidateEvent[T <: Event](id: Long) = {
    val resp = getEvent(id)
    validateEvent[T](resp)
  }
*/



  "EventIntegrationSuite " must {


    "post Move" in {

      val json =
        """
  {
   "eventType": "Move",
   "note": "Dette er et viktig notat for move!",
   "links": [{"rel": "actor", "href": "actor/12"}]}"""


      val response = createEvent(json)
      response.status mustBe 201
      val moveObject = validateEvent[Move](response.json) // .validate[Move].get

      val responseGet = getEvent(moveObject.id.get)
      responseGet.status mustBe 200
      println(responseGet.body)

    }

    "postWithWrongEvent" in {
      val json =
        """
  {
   "eventType": "hurra",
   "note": "Dette er IKKE viktig notat!"}"""

      val response = createEvent(json)
      response.status mustBe 400
    }

    "postWithControlEvent" in {
      val json =
        """
  {
   "eventType": "Control",
   "note": "Dette er et viktig notat for kontroll!",
   "links": [{"rel": "actor", "href": "actor/12"}]}"""

      val response = createEvent(json)
      response.status mustBe 201
      println(s"Create: ${response.body}")

      val myControlEvent = validateEvent[Control](response.json)
      myControlEvent.note mustBe Some("Dette er et viktig notat for kontroll!")
      val responseGet = getEvent(myControlEvent.id.get)
      responseGet.status mustBe 200
      println(s"Get: ${responseGet.body}")

    }
  }



  "post controlTemperature with ok = true" in {
    val json =
      """
  {
   "eventType": "ControlTemperature",
   "ok": true,
   "links": [{"rel": "actor", "href": "actor/12"}]}"""

    val response = createEvent(json)
    println(s"Create Control temperature: ${response.body}")
    response.status mustBe 201

    val myControlEvent = validateEvent[ControlTemperature](response.json)
    myControlEvent.ok mustBe true
    val responseGet = getEvent(myControlEvent.id.get)
    responseGet.status mustBe 200
    println(s"Get: ${responseGet.body}")

  }


  "post controlTemperature with ok = false" in {
    val json =
      """
  {
   "eventType": "ControlTemperature",
   "ok": false,
   "links": [{"rel": "actor", "href": "actor/12"}]}"""

    val response = createEvent(json)
    println(s"Create Control temperature: ${response.body}")
    response.status mustBe 201

    val myControlEvent = validateEvent[ControlTemperature](response.json)
    myControlEvent.ok mustBe false
    val responseGet = getEvent(myControlEvent.id.get)
    responseGet.status mustBe 200
    println(s"Get: ${responseGet.body}")

  }

  "post controlTemperature should fail if missing ok-value" in {
    val json =
      """
  {
   "eventType": "ControlTemperature",
   "links": [{"rel": "actor", "href": "actor/12"}]}"""

    val response = createEvent(json)
    println(s"Create Control temperature without ok should fail: ${response.body}")
    response.status mustBe 400
  }



  "post and get envRequirement" in {
    val json =
      """
  {
   "eventType": "EnvRequirement",
   "note": "Dette er et viktig notat for miljøkravene!",
   "temperature": 20,
   "temperatureInterval" : 5,
   "airHumidity": -20,
   "airHumidityInterval" : 4,
   "links": [{"rel": "actor", "href": "actor/12"}]}"""

    val response = createEvent(json)
    println(s"Create: ${response.body}")
    response.status mustBe 201

    val myEnvReqEvent = validateEvent[EnvRequirement](response.json)
    myEnvReqEvent.temperature mustBe Some(20)


    val responseGet = getEvent(myEnvReqEvent.id.get)
    responseGet.status mustBe 200
    println(s"Get: ${responseGet.body}")
  }

  "post and get Air envRequirement" in {
    val json =
      """
  {
   "eventType": "EnvRequirement",
   "note": "Dette er et viktig notat for miljøkravene!",
   "airHumidity": -20,
   "airHumidityInterval" : 5,
   "cleaning":"Ikke særlig rent",
   "links": [{"rel": "actor", "href": "actor/12"}]}"""

    val response = createEvent(json)
    println(s"Create: ${response.body}")
    response.status mustBe 201
    val myEnvReqEvent = validateEvent[EnvRequirement](response.json) //#OLD Event.format.reads(response.json).get.asInstanceOf[EnvRequirement]
    myEnvReqEvent.airHumidity mustBe Some(-20)
    myEnvReqEvent.envReqDto.cleaning mustBe Some("Ikke særlig rent")


    val responseGet = getEvent(myEnvReqEvent.id.get)
    responseGet.status mustBe 200
    println(s"Get: ${responseGet.body}")
  }



  "post and get ObservationTemperature" in {
    val json =
      """
  {
    "eventType": "observationTemperature",
    "from": -20,
    "to" : 5,
    "links": [{"rel": "actor", "href": "actor/12"}]}"""

    val response = createEvent(json)
    println(s"Create: ${response.body}")
    response.status mustBe 201
    val myEvent = validateEvent[ObservationTemperature](response.json)
    myEvent.from mustBe Some(-20)
    myEvent.to mustBe Some(5)


    val responseGet = getEvent(myEvent.id.get)
    responseGet.status mustBe 200
    println(s"Get: ${responseGet.body}")
  }


  "post and get complex Observation" in {
    val json =
      """
        {
          "eventType": "observation",
          "note": "tekst til observasjonene",
          "links": [{
            "rel": "actor",
            "href": "actor/12"
          }],
          "subEvents-parts": [{
            "eventType": "observationTemperature",
            "from": -30,
            "to": 25,
            "links": [{
              "rel": "actor",
              "href": "actor/12"
            }]
          }, {
            "eventType": "observationTemperature",
            "from": 20,
            "to": 50,
            "links": [{
              "rel": "actor",
              "href": "actor/12"
            }]},
        {
                    "eventType": "observationRelativeHumidity",
                    "from": 1,
                    "to": 2,
                    "links": [{
                      "rel": "actor",
                      "href": "actor/12"
                    }]
            }, {
                    "eventType": "observationInertAir",
                    "from": 0.1,
                    "to": 0.2,
                    "links": [{
                      "rel": "actor",
                      "href": "actor/12"
                    }]}
          ]
        }"""

    val myRawEvent = validateEvent[Observation](Json.parse(json))
    assert(myRawEvent.subObservations.length >= 2)
    val firstObsTempEvent = myRawEvent.subObservations(0).asInstanceOf[ObservationTemperature]
    firstObsTempEvent.from mustBe Some(-30)
    firstObsTempEvent.to mustBe Some(25)


    val response = createEvent(json)
    println(s"Create: ${response.body}")
    response.status mustBe 201
    val myEvent = validateEvent[Observation](response.json)
    assert(myEvent.subObservations.length >= 3)

    val firstObsEvent = myEvent.subObservations(0).asInstanceOf[ObservationTemperature]
    firstObsEvent.from mustBe Some(-30)
    firstObsEvent.to mustBe Some(25)


    val humEvent = myEvent.subObservations(2).asInstanceOf[ObservationRelativeHumidity]
    humEvent.from mustBe Some(1)
    humEvent.to mustBe Some(2)


    val airEvent = myEvent.subObservations(3).asInstanceOf[ObservationInertAir]
    airEvent.from mustBe Some(0.1)
    airEvent.to mustBe Some(0.2)

    val responseGet = getEvent(myEvent.id.get)
    responseGet.status mustBe 200
    println(s"Get: ${responseGet.body}")
  }



  "post wrong subEvents-relation should result in 400" in {
    val json =
      """
        {
          "eventType": "observation",
          "note": "tekst til observasjonene",

          "subEvents-this_relation_does_not_exist": [{
            "eventType": "observationTemperature",
            "from": -30,
            "to": 25
          }, {
            "eventType": "observationTemperature",
            "from": 20,
            "to": 50
          }]
        }"""
    val response = createEvent(json)
    println(s"Create: ${response.body}")
    response.status mustBe 400
    assert(response.body.contains("this_relation_does_not_exist"))
  }



  "post composite control" in {
    val json =
      """ {
    "eventType": "Control",
    "note": "tekst",
    "links": [{
    "rel": "actor",
    "href": "actor/12"
  }],
    "subEvents-parts": [{
    "eventType": "controlInertluft",
    "ok": true
  }, {
    "eventType": "controlTemperature",
    "ok": false,
    "subEvents-motivates": [{
      "eventType": "observationTemperature",
      "from": 20,
      "to": 50
    }]
  }]
  }
      """

    val response = createEvent(json)
    println(s"Create: ${response.body}")
    response.status mustBe 201

    val myEvent = validateEvent[Control](response.json)

    assert(myEvent.relatedSubEvents.length == 1)

    val parts = myEvent.subEventsWithRelation(EventRelations.relation_parts)
    assert(parts.isDefined)

    val specificControls = parts.get
    assert(specificControls.length >= 2)
    val okControl = specificControls(0).asInstanceOf[ControlInertluft]
    val notOkControl = specificControls(1).asInstanceOf[ControlTemperature]

    okControl.ok mustBe true
    notOkControl.ok mustBe false

    val motivatedObservations = notOkControl.subEventsWithRelation(EventRelations.relation_motivates)
    val partsObservations = notOkControl.subEventsWithRelation(EventRelations.relation_parts)

    assert(motivatedObservations.isDefined)
    assert(!partsObservations.isDefined)


    val ObsEvent = motivatedObservations.get(0).asInstanceOf[ObservationTemperature]
    ObsEvent.from mustBe Some(20)
    ObsEvent.to mustBe Some(50)

  }



  "post and get ObservationLys" in {
    val json =
      """
  {
    "eventType": "observationLys",
    "lysforhold": "merkelige forhold",
    "links": [{"rel": "actor", "href": "actor/12"}]}"""

    val response = createEvent(json)
    response.status mustBe 201
    val myEvent = validateEvent[ObservationLys](response.json)
    myEvent.lysforhold mustBe Some("merkelige forhold")


    val responseGet = getEvent(myEvent.id.get)
    responseGet.status mustBe 200
    val myEventGet = validateEvent[ObservationLys](responseGet.json)
    myEventGet.lysforhold mustBe Some("merkelige forhold")
  }


  "post and get ObservationSkadedyr" in {
    val json =
      """
  {
        	"eventType": "observationSkadedyr",
        	"identifikasjon": "skadedyr i veggene",
        	"note": "tekst til observationskadedyr",
        	"livssykluser": [{
        		"livssyklus": "Adult",
        		"antall": 3
        	}, {
        		"livssyklus": "Puppe",
        		"antall": 4
        	}, {
        		"livssyklus": "Puppeskinn",
        		"antall": 5
        	}, {
        		"livssyklus": "Larve",
        		"antall": 6
        	}, {
        		"livssyklus": "Egg",
        		"antall": 7
        	}]
        }"""

    val response = createEvent(json)
    response.status mustBe 201
    val myEvent = validateEvent[ObservationSkadedyr](response.json)
    myEvent.identifikasjon mustBe Some("skadedyr i veggene")


    val responseGet = getEvent(myEvent.id.get)
    responseGet.status mustBe 200
    val myEventGet = validateEvent[ObservationSkadedyr](responseGet.json)
    myEventGet.identifikasjon mustBe Some("skadedyr i veggene")

    myEventGet.livssykluser.length mustBe 5
    val livsSyklusFirst = myEventGet.livssykluser(0)
    livsSyklusFirst.livssyklus mustBe Some("Adult")
    livsSyklusFirst.antall mustBe Some(3)

    val livsSyklusLast = myEventGet.livssykluser(4)
    livsSyklusLast.livssyklus mustBe Some("Egg")
    livsSyklusLast.antall mustBe Some(7)
    livsSyklusLast.eventId mustBe None //We don't want these in the json output.

  }

  "redefining the same custom field should fail" in {
    intercept[AssertionError] {
      CustomFieldsSpec().defineRequiredBoolean("myBool").defineOptInt("myInt")
    }

    //Bool uses the long field, while string uses the string field, so it should be possible to define both a custom bool and a custom string
    CustomFieldsSpec().defineRequiredBoolean("myBool").defineOptString("myString")

    //And an int and a string
    CustomFieldsSpec().defineRequiredInt("myInt").defineOptString("myString")

  }




  "post controlRelativLuftfuktighet" in {
    val json =
      """ {
    "eventType": "controlRelativLuftfuktighet",
    "note": "tekst",
    "ok": true
  }
      """
    val response = createEvent(json)
    response.status mustBe 201

    val myEvent = validateEvent[ControlRelativLuftfuktighet](response.json)
    myEvent.ok mustBe true
  }

  "post ControlLysforhold" in {
    val json =
      """ {
    "eventType": "controlLysforhold",
    "note": "tekst",
    "ok": false
  }
      """
    val response = createEvent(json)
    response.status mustBe 201

    val myEvent = validateEvent[ControlLysforhold](response.json)
    myEvent.ok mustBe false
  }

  "post ControlRenhold" in {
    val json =
      """ {
    "eventType": "controlRenhold",
    "note": "tekst",
    "ok": true
  }
      """
    val response = createEvent(json)
    response.status mustBe 201

    val myEvent = validateEvent[ControlRenhold](response.json)
    myEvent.ok mustBe true
  }


  "post ControlGass" in {
    val json =
      """ {
    "eventType": "controlGass",
    "note": "tekst",
    "ok": false
  }
      """
    val response = createEvent(json)
    response.status mustBe 201

    val myEvent = validateEvent[ControlGass](response.json)
    myEvent.ok mustBe false
  }


  "post ControlMugg" in {
    val json =
      """ {
    "eventType": "controlMugg",
    "note": "tekst",
    "ok": true
  }
      """
    val response = createEvent(json)
    response.status mustBe 201

    val myEvent = validateEvent[ControlMugg](response.json)
    myEvent.ok mustBe true
  }


  "post ControlSkadedyr" in {
    val json =
      """ {
    "eventType": "controlSkadedyr",
    "note": "tekst",
    "ok": false
  }
      """
    val response = createEvent(json)
    response.status mustBe 201

    val myEvent = validateEvent[ControlSkadedyr](response.json)
    myEvent.ok mustBe false
  }

  "post ControlSprit" in {
    val json =
      """ {
    "eventType": "controlSprit",
    "note": "tekst",
    "ok": true
  }
      """
    val response = createEvent(json)
    response.status mustBe 201

    val myEvent = validateEvent[ControlSprit](response.json)
    myEvent.ok mustBe true
  }

}
