package models.analysis.events

import models.analysis.AnalysisStatuses.AnalysisStatus
import models.analysis.events.AnalysisResults._
import no.uio.musit.formatters.WithDateTimeFormatters
import no.uio.musit.models.{ActorId, CaseNumbers, EventId, ObjectUUID}
import org.joda.time.DateTime
import play.api.libs.json._

import scala.reflect.ClassTag

/**
 * Describes the least common denominator for analysis events. Different
 * implementations may contain more fields than this trait.
 */
sealed trait AnalysisEvent {
  val id: Option[EventId]
  val analysisTypeId: AnalysisTypeId
  val doneBy: Option[ActorId]
  val doneDate: Option[DateTime]
  val partOf: Option[EventId]
  val objectId: Option[ObjectUUID]
  val note: Option[String]
  val registeredBy: Option[ActorId]
  val registeredDate: Option[DateTime]
  val responsible: Option[ActorId]
  val administrator: Option[ActorId]
  val updatedBy: Option[ActorId]
  val updatedDate: Option[DateTime]
  val completedBy: Option[ActorId]
  val completedDate: Option[DateTime]
  val caseNumbers: Option[CaseNumbers]
  val status: Option[AnalysisStatus]
  val reason: Option[String]

  /**
   * Returns an AnalysisEvent that contains a result.
   */
  def withResult(res: Option[AnalysisResult]): AnalysisEvent = {
    this match {
      case a: Analysis            => a.copy(result = res)
      case ac: AnalysisCollection => ac.copy(result = res)
      case sc                     => sc
    }
  }

  /**
   * Returns an Option of an AnalysisEvent with a result. If the AnalysisEvent
   * type is Analysis, the Option will be Some(Analysis), otherwise None
   */
  def withResultAsOpt[A <: AnalysisEvent: ClassTag](
      res: Option[AnalysisResult]
  ): Option[A] =
    this match {
      case a: Analysis            => Option(a.copy(result = res).asInstanceOf[A])
      case ac: AnalysisCollection => Option(ac.copy(result = res).asInstanceOf[A])
      case _                      => None
    }
}

object AnalysisEvent extends WithDateTimeFormatters {

  private val mustBeTypeMsg = "Type must be either Analysis or AnalysisCollection"

  // key for type discriminator used in JSON formatters
  private val tpe = "type"

  /**
   * The implicit Reads for all AnalysisEvent implementations. It ensures that
   * the JSON message aligns with one of the types defined in the AnalysisEvent
   * ADT. If not the parsing will (and should) fail.
   */
  implicit val reads: Reads[AnalysisEvent] = Reads { jsv =>
    implicit val ar  = Analysis.reads
    implicit val acr = AnalysisCollection.reads
    implicit val sc  = SampleCreated.reads

    (jsv \ tpe).validateOpt[String] match {
      case JsSuccess(maybeType, path) =>
        maybeType.map {
          case Analysis.discriminator =>
            jsv.validate[Analysis]

          case AnalysisCollection.discriminator =>
            jsv.validate[AnalysisCollection]

          case SampleCreated.discriminator =>
            jsv.validate[SampleCreated]

          case unknown =>
            JsError(path, s"$unknown is not a valid type. $mustBeTypeMsg")

        }.getOrElse {
          JsError(path, mustBeTypeMsg)
        }

      case err: JsError =>
        err
    }
  }

  /**
   * Implicit Writes for AnalaysisEvent implementations. Ensures that each type
   * is written with their specific type discriminator. This ensure that the
   * JSON message is readable on the other end.
   */
  implicit val writes: Writes[AnalysisEvent] = Writes {
    case a: Analysis =>
      Analysis.writes.writes(a).as[JsObject] ++
        Json.obj(tpe -> Analysis.discriminator)

    case c: AnalysisCollection =>
      implicit val aw = Analysis.writes
      AnalysisCollection.writes.writes(c).as[JsObject] ++
        Json.obj(tpe -> AnalysisCollection.discriminator)

    case sc: SampleCreated =>
      SampleCreated.writes.writes(sc).as[JsObject] ++
        Json.obj(tpe -> SampleCreated.discriminator)
  }

}

/**
 * Representation of typical events related to the analysis of museum objects.
 * The specific event types are encoded as {{{EventTypeId}}}.
 */
case class Analysis(
    id: Option[EventId],
    analysisTypeId: AnalysisTypeId,
    doneBy: Option[ActorId],
    doneDate: Option[DateTime],
    registeredBy: Option[ActorId],
    registeredDate: Option[DateTime],
    responsible: Option[ActorId],
    administrator: Option[ActorId],
    updatedBy: Option[ActorId],
    updatedDate: Option[DateTime],
    completedBy: Option[ActorId],
    completedDate: Option[DateTime],
    objectId: Option[ObjectUUID],
    partOf: Option[EventId],
    note: Option[String],
    result: Option[AnalysisResult]
) extends AnalysisEvent {
  val reason: Option[String]           = None
  val status: Option[AnalysisStatus]   = None
  val caseNumbers: Option[CaseNumbers] = None
}

object Analysis extends WithDateTimeFormatters {
  val discriminator = "Analysis"

  // The below formatters cannot be implicit due to undesirable implicit ambiguities
  val reads: Reads[Analysis]   = Json.reads[Analysis]
  val writes: Writes[Analysis] = Json.writes[Analysis]

}

/**
 * Represents a "batch" of analysis "events" of the same type.
 * Each "event" will be persisted as a separate analysis, with a reference
 * to the AnalysisCollection in the Analysis.partOf attribute.
 */
case class AnalysisCollection(
    id: Option[EventId],
    analysisTypeId: AnalysisTypeId,
    doneBy: Option[ActorId],
    doneDate: Option[DateTime],
    registeredBy: Option[ActorId],
    registeredDate: Option[DateTime],
    responsible: Option[ActorId],
    administrator: Option[ActorId],
    updatedBy: Option[ActorId],
    updatedDate: Option[DateTime],
    completedBy: Option[ActorId],
    completedDate: Option[DateTime],
    note: Option[String],
    result: Option[AnalysisResult],
    events: Seq[Analysis],
    restriction: Option[Restriction],
    reason: Option[String],
    status: Option[AnalysisStatus],
    caseNumbers: Option[CaseNumbers]
) extends AnalysisEvent {

  val partOf: Option[EventId]      = None
  val objectId: Option[ObjectUUID] = None

  def withoutChildren = copy(events = Seq.empty)

}

object AnalysisCollection extends WithDateTimeFormatters {
  val discriminator = "AnalysisCollection"

  // The below formatters cannot be implicit due to undesirable implicit ambiguities
  def reads(implicit r: Reads[Analysis]): Reads[AnalysisCollection] =
    Json.reads[AnalysisCollection]

  def writes(implicit w: Writes[Analysis]): Writes[AnalysisCollection] =
    Json.writes[AnalysisCollection]

}

case class SampleCreated(
    id: Option[EventId],
    doneBy: Option[ActorId],
    doneDate: Option[DateTime],
    registeredBy: Option[ActorId],
    registeredDate: Option[DateTime],
    objectId: Option[ObjectUUID],
    sampleObjectId: Option[ObjectUUID],
    externalLinks: Option[Seq[String]]
) extends AnalysisEvent {
  val partOf: Option[EventId]          = None
  val note: Option[String]             = None
  val responsible: Option[ActorId]     = None
  val administrator: Option[ActorId]   = None
  val updatedBy: Option[ActorId]       = None
  val updatedDate: Option[DateTime]    = None
  val completedBy: Option[ActorId]     = None
  val completedDate: Option[DateTime]  = None
  val analysisTypeId                   = SampleCreated.sampleEventTypeId
  val reason: Option[String]           = None
  val status: Option[AnalysisStatus]   = None
  val caseNumbers: Option[CaseNumbers] = None
}

object SampleCreated extends WithDateTimeFormatters {
  val sampleEventTypeId: AnalysisTypeId = AnalysisTypeId.empty

  val discriminator = "SampleCreated"

  val reads: Reads[SampleCreated] = Json.reads[SampleCreated]

  val writes: Writes[SampleCreated] = Json.writes[SampleCreated]

}
