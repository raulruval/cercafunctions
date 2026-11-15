package com.cercafunctions.app
import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.provider.CalendarContract.*
import androidx.appfunctions.AppFunctionManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.cercafunctions.appfunctions.JourneyAppFunctionService
import com.cercafunctions.presentation.authorization.AssessmentSharingViewModel
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import java.time.*
import java.util.UUID
@SdkSuppress(minSdkVersion=36)
class AuthorizationFlowTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Before fun openAdvancedScreen() {
        compose.onNodeWithTag("demo_advanced").performScrollTo().performClick()
    }

    private val instrumentation=InstrumentationRegistry.getInstrumentation()
    private val resolver=instrumentation.targetContext.contentResolver
    private val date=LocalDate.now(ZoneOffset.UTC).plusDays(1)
    private val start=date.atTime(9,0).toInstant(ZoneOffset.UTC)
    private var calendarId:Long?=null
    private var eventId=0L
    @Before fun seed() {
        val account="cercafunctions-authorization-"+UUID.randomUUID()
        instrumentation.uiAutomation.adoptShellPermissionIdentity(Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR)
        try {
            val uri=Calendars.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter","true")
                .appendQueryParameter("account_name",account).appendQueryParameter("account_type","LOCAL").build()
            calendarId=ContentUris.parseId(requireNotNull(resolver.insert(uri,ContentValues().apply{
                put(Calendars.ACCOUNT_NAME,account);put(Calendars.ACCOUNT_TYPE,"LOCAL");put(Calendars.NAME,account)
                put(Calendars.CALENDAR_DISPLAY_NAME,"POC UC007");put(Calendars.CALENDAR_COLOR,0xff00796b.toInt())
                put(Calendars.CALENDAR_ACCESS_LEVEL,Calendars.CAL_ACCESS_OWNER);put(Calendars.OWNER_ACCOUNT,account)
                put(Calendars.CALENDAR_TIME_ZONE,"UTC");put(Calendars.VISIBLE,1);put(Calendars.SYNC_EVENTS,1)
            })))
            eventId=ContentUris.parseId(requireNotNull(resolver.insert(Events.CONTENT_URI,ContentValues().apply{
                put(Events.CALENDAR_ID,calendarId!!);put(Events.TITLE,"POC authorized occurrence")
                put(Events.DTSTART,start.toEpochMilli());put(Events.DTEND,start.plusSeconds(3600).toEpochMilli())
                put(Events.EVENT_TIMEZONE,"UTC");put(Events.ALL_DAY,0);put(Events.STATUS,Events.STATUS_CONFIRMED)
            })))
        } finally {instrumentation.uiAutomation.dropShellPermissionIdentity()}
        instrumentation.uiAutomation.grantRuntimePermission(instrumentation.targetContext.packageName,Manifest.permission.READ_CALENDAR)
    }
    @After fun cleanup() {
        instrumentation.uiAutomation.adoptShellPermissionIdentity(Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR)
        try{calendarId?.let{resolver.delete(ContentUris.withAppendedId(Calendars.CONTENT_URI,it),null,null)}}
        finally{instrumentation.uiAutomation.dropShellPermissionIdentity()}
    }
    private fun has(tag:String)=compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    private fun reference():String {
        var value:String?=null
        compose.activityRule.scenario.onActivity{value=ViewModelProvider(it)[AssessmentSharingViewModel::class.java].uiState.value.reference}
        return requireNotNull(value)
    }
    @Test fun issueHideRotateAndRevokeOneOccurrence() {
        compose.onNodeWithTag("calendar_date").performScrollTo().performTextReplacement(date.toString())
        compose.onNodeWithTag("calendar_zone").performScrollTo().performTextReplacement("UTC")
        compose.onNodeWithTag("calendar_consult").performScrollTo().performClick()
        val choose="calendar_choose_${eventId}_${start.toEpochMilli()}"
        compose.waitUntil(10_000){has(choose)};compose.onNodeWithTag(choose).performScrollTo().performClick()
        compose.onNodeWithTag("authorization_issue").performScrollTo().performClick()
        compose.waitUntil(10_000){has("authorization_reference")};val first=reference()
        val manager=requireNotNull(AppFunctionManager.getInstance(compose.activity))
        val id=JourneyAppFunctionService.FUNCTION_ID_EVALUATE_AUTHORIZED_COMMITMENT
        assertTrue(runBlocking{manager.isAppFunctionEnabled(id)})
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("authorization_reference").assertDoesNotExist()
        compose.onNodeWithTag("authorization_active").assertExists()
        compose.onNodeWithTag("authorization_issue").performScrollTo().performClick()
        compose.waitUntil(10_000){has("authorization_reference")};val second=reference()
        assertFalse("Rotation creates a new opaque reference",first==second)
        compose.onNodeWithTag("authorization_revoke").performScrollTo().performClick()
        compose.waitUntil(10_000){has("authorization_none") && !has("authorization_reference")}
        assertFalse(runBlocking{manager.isAppFunctionEnabled(id)})
    }
}
