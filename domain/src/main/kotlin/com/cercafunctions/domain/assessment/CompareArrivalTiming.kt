package com.cercafunctions.domain.assessment

import com.cercafunctions.domain.estimate.ArrivalEstimate
import java.time.Duration
import java.time.Instant

/** Pure comparison shared by the personal-calendar and simplified-demo workflows. */
fun compareArrivalTiming(evidence:ArrivalEstimate,meeting:MeetingTiming,assessedAt:Instant):ComparedArrival? {
    val arrived=evidence.arrival ?: return null
    val late=arrived.isAfter(meeting.start)
    val duration=if(late)Duration.between(meeting.start,arrived) else Duration.between(arrived,meeting.start)
    val complete=duration.toMinutes()
    val minutes=if(late) -(complete+if(duration.seconds%60!=0L || duration.nano!=0)1 else 0) else complete
    return ComparedArrival(evidence,meeting,if(late)ComparisonStatus.LATE else ComparisonStatus.ON_TIME,minutes,assessedAt)
}
