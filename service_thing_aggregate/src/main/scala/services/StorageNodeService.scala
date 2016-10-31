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

package services

import com.google.inject.Inject
import dao.StorageNodeDao
import no.uio.musit.models.{MuseumId, StorageNodeId}
import no.uio.musit.service.MusitResults.MusitResult

import scala.concurrent.Future

class StorageNodeService @Inject() (
    val storageNodeDao: StorageNodeDao
) {

  /**
   * Checks if the node exists in the storage facility for given museum ID.
   *
   * @param mid MuseumId
   * @param nodeId StorageNodeId
   * @return
   */
  def nodeExists(
    mid: MuseumId,
    nodeId: StorageNodeId
  ): Future[MusitResult[Boolean]] = storageNodeDao.nodeExists(mid, nodeId)

}
