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
import org.junit.*
import java.time.*
import java.util.UUID

/** Real Hilt/Activity/provider graph with a private synthetic test calendar. */
@SdkSuppress(minSdkVersion=29)
class CalendarFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Before fun openAdvancedScreen() {
        compose.onNodeWithTag("demo_advanced").performScrollTo().performClick()
    }

    private val instrumentation=InstrumentationRegistry.getInstrumentation()
    private val resolver=instrumentation.targetContext.contentResolver
    private var calendarId:Long?=null
    private val begins=mutableMapOf<Long,Instant>()
    @Before fun fixture() {
        instrumentation.uiAutomation.adoptShellPermissionIdentity(Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR)
        try {
            val account="cercafunctions-flow-${UUID.randomUUID()}"
            val uri=Calendars.CONTENT_URI.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,"true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME,account).appendQueryParameter(Calendars.ACCOUNT_TYPE,CalendarContract.ACCOUNT_TYPE_LOCAL).build()
            calendarId=ContentUris.parseId(requireNotNull(resolver.insert(uri,ContentValues().apply {
                put(Calendars.ACCOUNT_NAME,account);put(Calendars.ACCOUNT_TYPE,CalendarContract.ACCOUNT_TYPE_LOCAL)
                put(Calendars.NAME,account);put(Calendars.CALENDAR_DISPLAY_NAME,"POC calendario de flujo")
                put(Calendars.CALENDAR_COLOR,0xff00796b.toInt());put(Calendars.CALENDAR_ACCESS_LEVEL,Calendars.CAL_ACCESS_OWNER)
                put(Calendars.OWNER_ACCOUNT,account);put(Calendars.CALENDAR_TIME_ZONE,"Europe/Madrid");put(Calendars.VISIBLE,1);put(Calendars.SYNC_EVENTS,1)
            })))
            for(hour in listOf(9,10)) {
                val begin=LocalDate.of(2026,11,3).atTime(hour,0).atZone(ZoneId.of("Europe/Madrid")).toInstant()
                val event=resolver.insert(Events.CONTENT_URI,ContentValues().apply {
                    put(Events.CALENDAR_ID,calendarId!!);put(Events.TITLE,"POC mismo título")
                    put(Events.DTSTART,begin.toEpochMilli());put(Events.DTEND,begin.plusSeconds(1800).toEpochMilli())
                    put(Events.EVENT_TIMEZONE,"Europe/Madrid");put(Events.ALL_DAY,0);put(Events.STATUS,Events.STATUS_CONFIRMED)
                })
                begins[ContentUris.parseId(requireNotNull(event))]=begin
            }
        } finally {instrumentation.uiAutomation.dropShellPermissionIdentity()}
        instrumentation.uiAutomation.grantRuntimePermission(instrumentation.targetContext.packageName,Manifest.permission.READ_CALENDAR)
    }
    @After fun cleanup() {
        instrumentation.uiAutomation.adoptShellPermissionIdentity(Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR)
        try {calendarId?.let {resolver.delete(ContentUris.withAppendedId(Calendars.CONTENT_URI,it),null,null)}}
        finally {instrumentation.uiAutomation.dropShellPermissionIdentity()}
    }
    @Test fun chooseOneOccurrenceRotateRevalidateAndDetectDeletion() {
        compose.onNodeWithTag("calendar_date").performScrollTo().performTextReplacement("2026-11-03")
        compose.onNodeWithTag("calendar_zone").performScrollTo().performTextReplacement("Europe/Madrid")
        compose.onNodeWithTag("calendar_consult").performScrollTo().performClick()
        val (id,begin)=begins.entries.first()
        val tag="calendar_choose_${id}_${begin.toEpochMilli()}"
        compose.waitUntil(10_000){compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithTag("calendar_selected").assertDoesNotExist()
        compose.onNodeWithTag(tag).performScrollTo().performClick()
        compose.onNodeWithTag("calendar_selected_time").assertTextContains("2026-11-03 09:00 +01:00",substring=true)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("calendar_selected").assertExists()
        compose.onNodeWithTag("calendar_revalidate").performScrollTo().performClick()
        compose.waitUntil(10_000){compose.onAllNodesWithText("Ocurrencia revalidada",substring=true).fetchSemanticsNodes().isNotEmpty()}
        instrumentation.uiAutomation.adoptShellPermissionIdentity(Manifest.permission.WRITE_CALENDAR)
        try {resolver.delete(ContentUris.withAppendedId(Events.CONTENT_URI,id),null,null)}
        finally {instrumentation.uiAutomation.dropShellPermissionIdentity()}
        compose.onNodeWithTag("calendar_revalidate").performScrollTo().performClick()
        compose.waitUntil(10_000){compose.onAllNodesWithTag("calendar_selected").fetchSemanticsNodes().isEmpty()}
        compose.onNodeWithTag("calendar_status").assertTextContains("cambió",substring=true)
        compose.onNodeWithTag("calendar_consult").performScrollTo().performClick()
        val (otherId,otherBegin)=begins.entries.last()
        compose.waitUntil(10_000){compose.onAllNodesWithTag("calendar_choose_${otherId}_${otherBegin.toEpochMilli()}").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithTag("calendar_selected").assertDoesNotExist()
    }
}
