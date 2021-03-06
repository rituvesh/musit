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

package controllers.storage

import com.google.inject.{Inject, Singleton}
import models.storage.event.old.control.Control
import models.storage.event.old.observation.Observation
import no.uio.musit.MusitResults.{MusitError, MusitSuccess}
import no.uio.musit.security.Authenticator
import no.uio.musit.security.Permissions._
import no.uio.musit.service.MusitController
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import services.old.{ControlService, ObservationService}

import scala.concurrent.Future

@Singleton
class EventController @Inject()(
    val authService: Authenticator,
    val controlService: ControlService,
    val observationService: ObservationService
) extends MusitController {

  val logger = Logger(classOf[EventController])

  /**
   * Controller endpoint for adding a new Control for a storage node with
   * the given nodeId.
   */
  def addControl(
      mid: Int,
      nodeId: Long
  ) = MusitSecureAction(mid, Write).async(parse.json) { implicit request =>
    request.body.validate[Control] match {
      case JsSuccess(ctrl, jsPath) =>
        controlService.add(mid, nodeId, ctrl)(request.user).map {
          case MusitSuccess(addedCtrl) =>
            Created(Json.toJson(addedCtrl))

          case err: MusitError =>
            InternalServerError(Json.obj("message" -> err.message))
        }
      case JsError(errors) =>
        Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

  /**
   * Controller endpoint for adding a new Observation for a storage node with
   * the given nodeId.
   */
  def addObservation(
      mid: Int,
      nodeId: Long
  ) = MusitSecureAction(mid, Write).async(parse.json) { implicit request =>
    request.body.validate[Observation] match {
      case JsSuccess(obs, jsPath) =>
        observationService.add(mid, nodeId, obs)(request.user).map {
          case MusitSuccess(addedObs) =>
            Created(Json.toJson(addedObs))

          case err: MusitError =>
            InternalServerError(Json.obj("message" -> err.message))
        }
      case JsError(errors) =>
        Future.successful(BadRequest(JsError.toJson(errors)))
    }
  }

  /**
   * Fetch a Control with the given eventId for a storage node where the id is
   * equal to the provided nodeId.
   */
  def getControl(
      mid: Int,
      nodeId: Long,
      eventId: Long
  ) = MusitSecureAction(mid, Read).async { implicit request =>
    controlService.findBy(mid, eventId).map {
      case MusitSuccess(maybeControl) =>
        maybeControl.map { ctrl =>
          Ok(Json.toJson(ctrl))
        }.getOrElse {
          NotFound
        }

      case err: MusitError =>
        InternalServerError(Json.obj("message" -> err.message))
    }
  }

  /**
   * Fetch an Observation with the given eventId for a storage node where the
   * id is equal to the provided nodeId.
   */
  def getObservation(
      mid: Int,
      nodeId: Long,
      eventId: Long
  ) = MusitSecureAction(mid, Read).async { implicit request =>
    observationService.findBy(mid, eventId).map {
      case MusitSuccess(maybeObservation) =>
        maybeObservation.map { obs =>
          Ok(Json.toJson(obs))
        }.getOrElse {
          NotFound
        }
      case err: MusitError =>
        InternalServerError(Json.obj("message" -> err.message))
    }
  }

  /**
   * Lists all Controls for the given nodeId
   */
  def listControls(
      mid: Int,
      nodeId: Long
  ) = MusitSecureAction(mid, Read).async { implicit request =>
    controlService.listFor(mid, nodeId).map {
      case MusitSuccess(controls) =>
        Ok(Json.toJson(controls))

      case err: MusitError =>
        InternalServerError(Json.obj("message" -> err.message))
    }
  }

  /**
   * Lists all Observations for the given nodeId
   */
  def listObservations(
      mid: Int,
      nodeId: Long
  ) = MusitSecureAction(mid, Read).async { implicit request =>
    observationService.listFor(mid, nodeId).map {
      case MusitSuccess(observations) =>
        Ok(Json.toJson(observations))

      case err: MusitError =>
        InternalServerError(Json.obj("message" -> err.message))
    }
  }

  /**
   * Returns a mixed list of controls and observations for a storage node with
   * the given nodeId.
   */
  def listEventsForNode(
      mid: Int,
      nodeId: Long
  ) = MusitSecureAction(mid, Read).async { implicit request =>
    val eventuallyCtrls = controlService.listFor(mid, nodeId)
    val eventyallyObs   = observationService.listFor(mid, nodeId)
    for {
      ctrlRes <- eventuallyCtrls
      obsRes  <- eventyallyObs
    } yield {
      val sortedRes = for {
        controls     <- ctrlRes
        observations <- obsRes
      } yield {
        controls.union(observations).sortBy(_.doneDate.getMillis)
      }

      sortedRes match {
        case MusitSuccess(sorted) =>
          val jsObjects = sorted.map {
            case ctrl: Control    => Json.toJson(ctrl)
            case obs: Observation => Json.toJson(obs)
            case _                => JsNull
          }
          logger.debug(
            s"Going to return sorted JSON:" +
              s"\n${Json.prettyPrint(JsArray(jsObjects))}"
          )
          Ok(JsArray(jsObjects))

        case err: MusitError =>
          InternalServerError(Json.obj("message" -> err.message))
      }

    }
  }

}
