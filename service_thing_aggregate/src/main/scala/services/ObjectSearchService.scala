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
import dao.{ObjectSearchDao, StorageNodeDao}
import models.{MuseumNo, MusitObject, SubNo}
import no.uio.musit.service.MusitResults._
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.util.control.NonFatal

class ObjectSearchService @Inject() (
    objSearchDao: ObjectSearchDao,
    nodeDao: StorageNodeDao
) {

  private val logger = Logger(classOf[ObjectSearchService])

  /**
   * Search for objects based on the given criteria.
   *
   * @param mid
   * @param page
   * @param limit
   * @param museumNo
   * @param subNo
   * @param term
   * @return
   */
  def search(
    mid: Int,
    page: Int,
    limit: Int,
    museumNo: Option[MuseumNo],
    subNo: Option[SubNo],
    term: Option[String]
  ): Future[MusitResult[Seq[MusitObject]]] = {
    objSearchDao.search(mid, page, limit, museumNo, subNo, term).flatMap {
      case MusitSuccess(objects) =>
        // We found some objects...now we need to find the current location for each.
        Future.sequence {
          objects.map { obj =>
            nodeDao.currentLocation(mid, obj.id).flatMap {
              case Some(nodeIdAndPath) =>
                nodeDao.namesForPath(nodeIdAndPath._2).map { pathNames =>
                  obj.copy(
                    currentLocationId = Some(nodeIdAndPath._1),
                    path = Some(nodeIdAndPath._2),
                    pathNames = Some(pathNames)
                  )
                }
              case None =>
                Future.successful(obj)
            }
          }
        }.map(MusitSuccess.apply).recover {
          case NonFatal(ex) =>
            val msg = s"An error occured when executing object search"
            logger.error(msg, ex)
            MusitInternalError(msg)
        }

      case err: MusitError =>
        Future.successful(err)
    }
  }
}