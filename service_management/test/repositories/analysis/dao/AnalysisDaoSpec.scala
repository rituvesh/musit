package repositories.analysis.dao

import models.analysis.events.AnalysisResults.{AnalysisResult, DatingResult}
import models.analysis.events.{Analysis, AnalysisCollection}
import no.uio.musit.MusitResults.{MusitDbError, MusitResult, MusitSuccess}
import no.uio.musit.models.{EventId, ObjectUUID}
import no.uio.musit.test.MusitSpecWithAppPerSuite
import no.uio.musit.test.matchers.{DateTimeMatchers, MusitResultValues}
import no.uio.musit.time.dateTimeNow
import org.scalatest.Inspectors.forAll
import org.scalatest.OptionValues
import utils.AnalysisGenerators

class AnalysisDaoSpec
    extends MusitSpecWithAppPerSuite
    with DateTimeMatchers
    with MusitResultValues
    with OptionValues
    with AnalysisGenerators {

  private val dao = fromInstanceCache[AnalysisDao]

  def saveAnalysis(
      oid: Option[ObjectUUID],
      res: Option[AnalysisResult]
  ): MusitResult[EventId] = {
    val a = dummyAnalysis(oid, res)
    dao.insert(a).futureValue
  }

  "AnalysisDao" when {

    "inserting analysis events" should {
      "return the EventId allocated to a single analysis" in {
        val gr = Some(dummyGenericResult())
        saveAnalysis(Some(oid1), gr) mustBe MusitSuccess(EventId(1))
      }

      "return the EventId allocated for an analysis collection with many analyses" in {
        val gr = Some(dummyGenericResult())
        val e1 = dummyAnalysis(Some(oid1))
        val e2 = dummyAnalysis(Some(oid2))
        val e3 = dummyAnalysis(Some(oid3))
        val ac = dummyAnalysisCollection(gr, e1, e2, e3)

        val res = dao.insertCol(ac).futureValue

        res.successValue mustBe EventId(2)
      }

      "return the EventId allocated for an analysis collection with one analysis" in {
        val e1 = dummyAnalysis(Some(oid1))
        val ac = dummyAnalysisCollection(None, e1)

        val res = dao.insertCol(ac).futureValue

        res.successValue mustBe EventId(6)
      }
    }

    "fetching analysis events" should {

      "return an analysis collection with many children" in {
        val res = dao.findById(EventId(2)).futureValue

        res.successValue.value mustBe an[AnalysisCollection]
        val ac = res.successValue.value.asInstanceOf[AnalysisCollection]
        ac.events must not be empty
        ac.events.size mustBe 3
        ac.analysisTypeId mustBe dummyAnalysisTypeId
        ac.partOf mustBe empty
      }

      "return an analysis collection with one child" in {
        val res = dao.findById(EventId(6)).futureValue

        res.successValue.value mustBe an[AnalysisCollection]
        val ac = res.successValue.value.asInstanceOf[AnalysisCollection]
        ac.events must not be empty
        ac.events.size mustBe 1
        ac.analysisTypeId mustBe dummyAnalysisTypeId
        ac.partOf mustBe empty
      }

      "return each child in a collection separately by fetching them with their IDs" in {
        val res1 = dao.findById(EventId(3)).futureValue
        val res2 = dao.findById(EventId(4)).futureValue
        val res3 = dao.findById(EventId(5)).futureValue

        res1.successValue.value.partOf mustBe Some(EventId(2))
        res2.successValue.value.partOf mustBe Some(EventId(2))
        res3.successValue.value.partOf mustBe Some(EventId(2))
      }

      "list all child events for an analysis collection" in {
        val children = dao.listChildren(EventId(2)).futureValue
        children.successValue.size mustBe 3

        forAll(children.successValue) { child =>
          child.result mustBe None
          child.analysisTypeId mustBe dummyAnalysisTypeId
          child.partOf mustBe Some(EventId(2))
          child.registeredBy mustBe Some(dummyActorId)
        }
      }

      "return the analysis event that matches the given id" in {
        val gr =
          dummyGenericResult(extRef = Some(Seq("shizzle", "in", "the", "drizzle")))

        val mra = saveAnalysis(Some(oid2), Some(gr))

        val res = dao.findById(mra.successValue).futureValue
        res.successValue.value mustBe an[Analysis]
        val a = res.successValue.value.asInstanceOf[Analysis]
        a.registeredBy mustBe Some(dummyActorId)
        a.analysisTypeId mustBe dummyAnalysisTypeId
        a.result.value.comment mustBe gr.comment
        a.result.value.extRef mustBe gr.extRef
        a.result.value.registeredBy mustBe gr.registeredBy
      }

      "return an analysis event with a dating result" in {
        val dr = dummyDatingResult(age = Some("really really old"))

        val mra = saveAnalysis(Some(oid2), Some(dr))

        val res = dao.findById(mra.successValue).futureValue
        res.successValue.value mustBe an[Analysis]
        val analysis = res.successValue.value.asInstanceOf[Analysis]
        analysis.registeredBy mustBe Some(dummyActorId)
        analysis.analysisTypeId mustBe dummyAnalysisTypeId
        analysis.result.value mustBe a[DatingResult]
        val datingResult = analysis.result.value.asInstanceOf[DatingResult]
        datingResult.comment mustBe dr.comment
        datingResult.extRef mustBe dr.extRef
        datingResult.registeredBy mustBe dr.registeredBy
        datingResult.age mustBe dr.age
      }

      "return all analysis events for a given object" in {
        dao.findByObjectUUID(oid2).futureValue.successValue.size mustBe 3
      }

      "successfully add a result to an analysis" in {
        val e1 = dummyAnalysis(Some(oid1))
        val ac = dummyAnalysisCollection(None, e1)
        val gr = dummyGenericResult(comment = Some("updated result"))

        val eid = dao.insertCol(ac).futureValue.successValue

        dao.upsertResult(eid, gr).futureValue.isSuccess mustBe true

        dao.findById(eid).futureValue.successValue.value match {
          case good: AnalysisCollection =>
            val res = good.result.value
            res.registeredBy mustBe gr.registeredBy
            res.registeredDate mustApproximate gr.registeredDate
            res.extRef mustBe gr.extRef
            res.comment mustBe gr.comment

          case bad =>
            fail(
              s"Expected an ${AnalysisCollection.getClass} but got an ${bad.getClass}"
            )
        }
      }

      "successfully update a result belonging to an analysis" in {
        val eid = EventId(6L)
        val ae  = dao.findById(eid).futureValue.successValue.value
        ae mustBe an[AnalysisCollection]
        ae.asInstanceOf[AnalysisCollection].result mustBe None

        val gr = dummyGenericResult(comment = Some("I'm a new result"))

        dao.upsertResult(eid, gr).futureValue.isSuccess mustBe true

        dao.findById(eid).futureValue.successValue.value match {
          case good: AnalysisCollection =>
            val res = good.result.value
            res.registeredBy mustBe gr.registeredBy
            res.registeredDate mustApproximate gr.registeredDate
            res.extRef mustBe gr.extRef
            res.comment mustBe gr.comment

          case bad =>
            fail(
              s"Expected an ${AnalysisCollection.getClass} but got an ${bad.getClass}"
            )
        }
      }

      "fail when trying to add a result to a non-existing analysis" in {
        val gr  = dummyGenericResult()
        val res = dao.upsertResult(EventId(100), gr).futureValue
        res.isFailure mustBe true
        res mustBe a[MusitDbError]
      }
    }

    "updating an analysis event" should {

      "successfully save the modified fields" in {
        val eid = EventId(1L)
        val ae  = dao.findById(eid).futureValue.successValue.value.asInstanceOf[Analysis]

        val upd = ae.copy(
          updatedBy = Some(dummyActorId),
          updatedDate = Some(dateTimeNow),
          note = Some("I was just updated")
        )

        val res = dao.update(eid, upd).futureValue.successValue.value

        res.note mustBe upd.note
        res.updatedBy mustBe Some(dummyActorId)
        res.updatedDate mustApproximate Some(dateTimeNow)
      }

      "fail if the analysis doesn't exist" in {
        val eid = EventId(200)
        val a   = dummyAnalysis(ObjectUUID.generateAsOpt()).copy(id = Some(eid))

        dao.update(eid, a).futureValue.isFailure mustBe true
      }
    }

  }

}
