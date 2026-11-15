package com.cercafunctions.appfunctions

import androidx.appfunctions.AppFunctionSerializable
import java.time.Instant
import java.time.ZoneId

/** Synthetic train compared with the timing of the real next calendar meeting tomorrow. */
@AppFunctionSerializable(isDescribedByKDoc=true)
data class DemoJourneyResponse(
    /** Synthetic origin, not a real selected route or operational service. */
    val origin:String,
    /** Synthetic destination. */
    val destination:String,
    /** FIXTURE: only the train is synthetic, never an operational forecast. */
    val journeySource:String,
    /** ANDROID_CALENDAR: meeting timing read live from the device calendar. */
    val meetingSource:String,
    /** DEMO_NORMAL, DEMO_DELAYED or DEMO_UNKNOWN selected in the app. */
    val scenario:String,
    /** Demo departure set to 35 minutes before the real meeting start. */
    val departure:Instant,
    /** Demo station arrival; null when the selected scenario has no delay evidence. */
    val arrival:Instant?,
    /** Start of the first eligible meeting tomorrow in the device zone. No title or identifier. */
    val meetingStart:Instant,
    /** Device display zone at the time of the request. */
    val zone:ZoneId,
    /** ON_TIME, LATE or INDETERMINATE. Arrival exactly at the start is on time. */
    val status:String,
    /** Zero on time, negative 15 when late, null if indeterminate. No extra transfer time included. */
    val signedMarginMinutes:Long?,
)
