package com.cercafunctions.appfunctions
import androidx.appfunctions.AppFunctionSerializable
import java.time.Instant
import java.time.ZoneId
/** Minimal authorized comparison; never a calendar listing or an operational railway prediction. */
@AppFunctionSerializable(isDescribedByKDoc=true)
data class AssessmentResponse(
    /** ON_TIME, LATE or INDETERMINATE for station arrival versus the authorized occurrence. */
    val status:String,
    /** Reason for an indeterminate arrival; null for a completed comparison. */
    val reason:String?,
    /** Complete available minutes, zero at equality, or negative ceil of late minutes; null when indeterminate. */
    val signedMarginMinutes:Long?,
    /** Effective departure instant, preserving fixed demo dates, or null without arrival evidence. */
    val departure:Instant?,
    /** Station arrival instant; null means unknown, never zero delay. */
    val arrival:Instant?,
    /** Zone used to display departure and arrival when evidence exists. */
    val arrivalZone:ZoneId?,
    /** Authorized occurrence start, disclosed only for a completed comparison. */
    val meetingStart:Instant?,
    /** Display zone of that authorized start, or null for an indeterminate result. */
    val meetingZone:ZoneId?,
    /** REFERENCE uses saved duration; FIXTURE uses explicitly requested synthetic data; null without evidence. */
    val source:String?,
    /** Effective REFERENCE or fixed demo scenario, when evidence exists. */
    val scenario:String?,
    /** Arrival evidence observation instant; fixed for demos, not a validity guarantee. */
    val observedAt:Instant?,
    /** Instant the shared domain evaluation was performed. */
    val assessedAt:Instant,
)
