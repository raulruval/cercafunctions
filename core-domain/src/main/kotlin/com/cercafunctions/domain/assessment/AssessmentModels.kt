package com.cercafunctions.domain.assessment

import com.cercafunctions.domain.estimate.ArrivalEstimate
import java.time.*

enum class ComparisonStatus { ON_TIME, LATE }
/** Timing only: no readable calendar details or local identifiers are returned. */
data class MeetingTiming(val start:Instant,val end:Instant,val zone:ZoneId)
data class ComparedArrival(val evidence:ArrivalEstimate,val meeting:MeetingTiming,
    val status:ComparisonStatus,val signedMarginMinutes:Long,val assessedAt:Instant) {
    init {require(if(status==ComparisonStatus.ON_TIME)signedMarginMinutes>=0 else signedMarginMinutes<0)}
}
enum class AssessmentReason {
    MISSING_ARRIVAL, MISSING_SELECTION, UNKNOWN_ARRIVAL, ARRIVAL_CHANGED,
    ARRIVAL_UNAVAILABLE, DEPARTURE_PASSED, CALENDAR_CHANGED, CALENDAR_PERMISSION, CALENDAR_UNAVAILABLE,
}
sealed interface AssessmentResult {
    data class Compared(val value:ComparedArrival):AssessmentResult
    data class Indeterminate(val reason:AssessmentReason,val evidence:ArrivalEstimate?,val assessedAt:Instant):AssessmentResult
}
