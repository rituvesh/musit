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

package no.uio.musit.models

import org.scalatestplus.play.PlaySpec

class MuseumIdentifierSpec extends PlaySpec {
  "Interacting with MuseumIdentifier" when {
    "only MusemNO is set" should {
      "should get MuseumIdentifier with only museumNo and slash" in {
        val sqlString = "CH3456/"
        val ident = MuseumIdentifier.fromSqlString(sqlString)
        ident.museumNo mustBe "CH3456"
        ident.subNo mustBe Some("")
      }
      "get MuseumIdentifier with only museumNo and no slash" in {
        val sqlString = "CH3456"
        val ident = MuseumIdentifier.fromSqlString(sqlString)
        ident.museumNo mustBe "CH3456"
        ident.subNo mustBe None
      }
      "get MuseumIdentifier with museumNo with subNo and subSubNo" in {
        val sqlString = "CH3456/33/AB34"
        val ident = MuseumIdentifier.fromSqlString(sqlString)
        ident.museumNo mustBe "CH3456"
        ident.subNo mustBe Some("33/AB34")
      }
      "get MuseumIdentifier with museumNo and subNo (with wrong subNo)" in {
        val sqlString = "CH3456//AB34"
        val ident = MuseumIdentifier.fromSqlString(sqlString)
        ident.museumNo mustBe "CH3456"
        ident.subNo mustBe Some("/AB34")
      }
    }
  }
}
