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

package no.uio.musit.security

import no.uio.musit.security.crypto.MusitCrypto

case class EncryptedToken(underlying: String) extends AnyVal {

  def asString = underlying

  def urlEncoded = java.net.URLEncoder.encode(underlying, "utf-8")

}

object EncryptedToken {

  def fromBearerToken(bt: BearerToken)(implicit crypto: MusitCrypto) = {
    EncryptedToken(crypto.encryptAES(bt.underlying))
  }

}