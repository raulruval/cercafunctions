package com.cercafunctions.app

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.provider.CalendarContract
import android.provider.CalendarContract.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import java.time.*
import java.util.UUID
import org.junit.*
import org.junit.Assert.assertNull

/** Real read-only domain graph; SDK writes are isolated to this fixture calendar. */
@SdkSuppress(minSdkVersion=29)
class AssessmentFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    private val instrumentation=InstrumentationRegistry.getInstrumentation()
    private val resolver=instrumentation.targetContext.contentResolver
    private val date=LocalDate.now(ZoneOffset.UTC).plusDays(1)
    private val start=date.atTime(9,0).toInstant(ZoneOffset.UTC)
    private var calendarId:Long?=null
    private var eventId:Long=0
    @Before fun fixture() {
        instrumentation.uiAutomation.adoptShellPermissionIdentity(Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR)
        try {
            val account="cercafunctions-assessment-${UUID.randomUUID()}"
            val uri=Calendars.CONTENT_URI.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,"true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME,account).appendQueryParameter(Calendars.ACCOUNT_TYPE,CalendarContract.ACCOUNT_TYPE_LOCAL).build()
            calendarId=ContentUris.parseId(requireNotNull(resolver.insert(uri,ContentValues().apply {
                put(Calendars.ACCOUNT_NAME,account);put(Calendars.ACCOUNT_TYPE,CalendarContract.ACCOUNT_TYPE_LOCAL)
                put(Calendars.NAME,account);put(Calendars.CALENDAR_DISPLAY_NAME,"POC evaluación")
                put(Calendars.CALENDAR_COLOR,0xff00796b.toInt());put(Calendars.CALENDAR_ACCESS_LEVEL,Calendars.CAL_ACCESS_OWNER)
                put(Calendars.OWNER_ACCOUNT,account);put(Calendars.CALENDAR_TIME_ZONE,"UTC");put(Calendars.VISIBLE,1);put(Calendars.SYNC_EVENTS,1)
            })))
            eventId=ContentUris.parseId(requireNotNull(resolver.insert(Events.CONTENT_URI,ContentValues().apply {
                put(Events.CALENDAR_ID,calendarId!!);put(Events.TITLE,"POC compromiso 09:00")
                put(Events.DTSTART,start.toEpochMilli());put(Events.DTEND,start.plusSeconds(3600).toEpochMilli())
                put(Events.EVENT_TIMEZONE,"UTC");put(Events.ALL_DAY,0);put(Events.STATUS,Events.STATUS_CONFIRMED)
            })))
        } finally {instrumentation.uiAutomation.dropShellPermissionIdentity()}
        instrumentation.uiAutomation.grantRuntimePermission(instrumentation.targetContext.packageName,Manifest.permission.READ_CALENDAR)
    }
    @After fun cleanup() {
        instrumentation.uiAutomation.adoptShellPermissionIdentity(Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR)
        try {calendarId?.let{resolver.delete(ContentUris.withAppendedId(Calendars.CONTENT_URI,it),null,null)}}
        finally {instrumentation.uiAutomation.dropShellPermissionIdentity()}
    }
    @Test fun compareEarlyEqualLateUnknownThenRemoveInvalidMeeting() {
        compose.waitUntil(10_000){has("configure") || has("edit")}
        compose.onNodeWithTag("calendar_date").performScrollTo().performTextReplacement(date.toString())
        compose.onNodeWithTag("calendar_zone").performScrollTo().performTextReplacement("UTC")
        compose.onNodeWithTag("calendar_consult").performScrollTo().performClick()
        val choose="calendar_choose_${eventId}_${start.toEpochMilli()}"
        compose.waitUntil(10_000){has(choose)}
        compose.onNodeWithTag(choose).performScrollTo().performClick()

        compose.onNodeWithTag(if(has("configure"))"configure" else "edit").performScrollTo().performClick()
        compose.onNodeWithTag("origin").performScrollTo().performTextReplacement("Atocha Cercanías")
        compose.onNodeWithTag("destination").performScrollTo().performTextReplacement("Chamartín")
        compose.onNodeWithTag("duration").performScrollTo().performTextReplacement("35")
        compose.onNodeWithTag("save").performScrollTo().performClick()
        compose.waitUntil(10_000){has("saved_confirmation")}
        compose.onNodeWithTag("arrival_date").performScrollTo().performTextReplacement(date.toString())
        compose.onNodeWithTag("arrival_zone").performScrollTo().performTextReplacement("UTC")
        for((time,expected) in listOf("08:15" to "Margen previsto: 10 min","08:25" to "Margen previsto: 0 min","08:40" to "Retraso previsto: 15 min")) {
            compose.onNodeWithTag("arrival_time").performScrollTo().performTextReplacement(time)
            compose.onNodeWithTag("assessment_outcome").assertDoesNotExist()
            consultArrival();evaluate()
            compose.waitUntil(10_000){has("assessment_outcome")}
            compose.onNodeWithTag("assessment_outcome").assertTextContains(expected,substring=true)
            compose.onNodeWithTag("assessment_source").assertTextContains("Referencia",substring=true)
            if(time=="08:15") {
                compose.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
                compose.activityRule.scenario.onActivity { activity ->
                    assertNull(androidx.lifecycle.ViewModelProvider(activity)
                        [com.cercafunctions.presentation.calendar.CalendarViewModel::class.java].uiState.value.assessment)
                }
                compose.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
                compose.activityRule.scenario.recreate()
                compose.onNodeWithTag("calendar_selected").assertExists()
                compose.onNodeWithTag("assessment_outcome").assertDoesNotExist()
            }
        }
        compose.onNodeWithTag("arrival_mode_DEMO_UNKNOWN").performScrollTo().performClick()
        consultArrival();evaluate()
        compose.waitUntil(10_000){has("assessment_indeterminate")}
        compose.onNodeWithTag("assessment_indeterminate").assertTextContains("desconocida",substring=true)
        compose.onNodeWithTag("assessment_source").assertTextContains("Demostración",substring=true)
        compose.onNodeWithTag("arrival_mode_REFERENCE").performScrollTo().performClick()
        consultArrival()
        instrumentation.uiAutomation.adoptShellPermissionIdentity(Manifest.permission.WRITE_CALENDAR)
        try {resolver.delete(ContentUris.withAppendedId(Events.CONTENT_URI,eventId),null,null)}
        finally {instrumentation.uiAutomation.dropShellPermissionIdentity()}
        evaluate()
        compose.waitUntil(10_000){has("assessment_indeterminate")}
        compose.onNodeWithTag("assessment_indeterminate").assertTextContains("cambió",substring=true)
        compose.onNodeWithTag("calendar_selected").assertDoesNotExist()
    }
    private fun has(tag:String)=compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    private fun consultArrival(){compose.onNodeWithTag("arrival_consult").performScrollTo().performClick();compose.waitUntil(10_000){has("arrival_result")}}
    private fun evaluate(){compose.onNodeWithTag("assessment_evaluate").performScrollTo().performClick()}
}
