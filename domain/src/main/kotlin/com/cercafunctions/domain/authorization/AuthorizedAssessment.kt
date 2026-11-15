package com.cercafunctions.domain.authorization
import com.cercafunctions.domain.assessment.*
import com.cercafunctions.domain.estimate.*
/** Shares the UC-005 orchestration, enclosing it with temporal disclosure checks. */
class AuthorizedAssessment(private val authorizations:AssessmentAuthorizations,private val query:QueryJourneyArrival,
    private val assess:AssessArrival) {
    suspend operator fun invoke(reference:String,input:DepartureInput,scenario:ArrivalScenario=ArrivalScenario.REFERENCE):AssessmentResult {
        val grant=authorizations.resolve(reference)
        val snapshot=when(val read=query(input,scenario)) {
            is ArrivalResult.Estimated -> read.value
            ArrivalResult.MissingJourney,is ArrivalResult.StorageUnavailable -> null
            else -> throw InvalidAssessmentDeparture()
        }
        val result=assess(snapshot,grant.selection)
        if(result is AssessmentResult.Indeterminate && result.reason in setOf(AssessmentReason.CALENDAR_CHANGED,
            AssessmentReason.CALENDAR_PERMISSION,AssessmentReason.CALENDAR_UNAVAILABLE)) {
            authorizations.invalidate(reference);throw AssessmentAccessDenied()
        }
        authorizations.resolve(reference)
        return result
    }
}
