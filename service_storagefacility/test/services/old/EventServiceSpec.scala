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

package services.old

import no.uio.musit.models.{EventId, MuseumId}
import no.uio.musit.test.MusitSpecWithAppPerSuite
import no.uio.musit.test.matchers.MusitResultValues
import utils.testhelpers.{EventGenerators_Old, NodeGenerators}

class EventServiceSpec
    extends MusitSpecWithAppPerSuite
    with NodeGenerators
    with EventGenerators_Old
    with MusitResultValues {

  val controlService: ControlService = fromInstanceCache[ControlService]
  val obsService: ObservationService = fromInstanceCache[ObservationService]

  "Processing events" should {
    "successfully insert a new Control" in {
      val c = createControl(defaultBuilding.id)

      val ce = controlService.add(defaultMuseumId, defaultBuilding.id.get, c).futureValue
      ce.successValue.id.value mustBe EventId(1L)
    }

    "fail when inserting a Control with wrong museumId" in {
      val anotherMid = MuseumId(4)
      val ctrl       = createControl(defaultBuilding.id)

      val res = controlService.add(anotherMid, defaultBuilding.id.get, ctrl).futureValue
      res.isSuccess mustBe false
      res.isFailure mustBe true
    }

    "successfully insert a new Observation" in {
      val obs = createObservation(defaultBuilding.id)

      val res = obsService.add(defaultMuseumId, defaultBuilding.id.get, obs).futureValue

      val theObs = res.successValue
      theObs.id.value mustBe EventId(9L)

      theObs.alcohol mustBe obs.alcohol
      theObs.cleaning mustBe obs.cleaning
      theObs.gas mustBe obs.gas
      theObs.pest mustBe obs.pest
      theObs.mold mustBe obs.mold
      theObs.hypoxicAir mustBe obs.hypoxicAir
      theObs.temperature mustBe obs.temperature
      theObs.relativeHumidity mustBe obs.relativeHumidity
      theObs.lightingCondition mustBe obs.lightingCondition
      theObs.perimeterSecurity mustBe obs.perimeterSecurity
      theObs.fireProtection mustBe obs.fireProtection
      theObs.theftProtection mustBe obs.theftProtection
      theObs.waterDamageAssessment mustBe obs.waterDamageAssessment
    }

    "fail when inserting a Observation with wrong museumId" in {
      val anotherMid = MuseumId(4)
      val obs        = createObservation(defaultBuilding.id)

      val res = obsService.add(anotherMid, defaultBuilding.id.get, obs).futureValue
      res.isSuccess mustBe false
      res.isFailure mustBe true
    }
  }
}
