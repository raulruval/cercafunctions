package com.cercafunctions.platform.calendar

import android.content.*
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.CancellationSignal
import android.provider.CalendarContract.*
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SdkSuppress
import com.cercafunctions.domain.calendar.*
import java.time.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

@SdkSuppress(minSdkVersion=29)
class AndroidCalendarRepositoryTest {
    private val day=CalendarDay(LocalDate.parse("2026-11-03"),ZoneId.of("Europe/Madrid"))
    private val columns=arrayOf(Instances.EVENT_ID,Instances.BEGIN,Instances.END,Instances.TITLE,
        Calendars.CALENDAR_DISPLAY_NAME,Instances.ALL_DAY,Instances.STATUS,Calendars.VISIBLE)
    private fun cursor(vararg rows:Array<Any?>)=MatrixCursor(columns).apply{rows.forEach{addRow(it)}}
    private fun row(id:Long=1,begin:Instant=day.start.plusSeconds(3600),allDay:Int=0,status:Int=Events.STATUS_CONFIRMED,visible:Int=1):Array<Any?> =
        arrayOf(id,begin.toEpochMilli(),begin.plusSeconds(3600).toEpochMilli(),"POC evento","POC calendario",allDay,status,visible)
    private fun resolver(provider:QueryProvider):ContentResolver {
        provider.attachInfo(ApplicationProvider.getApplicationContext(),ProviderInfo().apply{authority="com.android.calendar";exported=true})
        return ContentResolver.wrap(provider)
    }
    @Test fun permissionDeniedDoesNotQueryProvider()=runBlocking {
        val provider=QueryProvider{error("must not query")}
        val repo=AndroidCalendarRepository(resolver(provider),{false})
        assertEquals(CalendarRead.PermissionRequired,repo.read(day));assertEquals(0,provider.calls)
    }
    @Test fun uriProjectionFiltersAndCursorClosureProtectBoundary()=runBlocking {
        var closed=false
        val data=object:MatrixCursor(columns){override fun close(){closed=true;super.close()}}
        listOf(row(),row(2,day.start.minusSeconds(10)),row(3,day.end),row(4,allDay=1),row(5,status=Events.STATUS_CANCELED),row(6,visible=0)).forEach{data.addRow(it)}
        val provider=QueryProvider{data}
        val result=AndroidCalendarRepository(resolver(provider),{true}).read(day) as CalendarRead.Data
        assertEquals(listOf(1L),result.occurrences.map{it.key.eventId});assertTrue(closed)
        assertEquals(day.start.toEpochMilli().toString(),provider.uri!!.pathSegments.takeLast(2)[0])
        assertEquals(day.end.toEpochMilli().toString(),provider.uri!!.lastPathSegment)
        assertArrayEquals(columns,provider.projection)
        assertTrue(provider.selection!!.contains(Instances.BEGIN));assertTrue(provider.selection!!.contains(Instances.ALL_DAY))
        assertTrue(provider.selection!!.contains(Calendars.VISIBLE))
    }
    @Test fun nullProviderAndQueryFailureDifferFromEmpty()=runBlocking {
        assertEquals(CalendarRead.Unavailable(CalendarFailure.PROVIDER_UNAVAILABLE),AndroidCalendarRepository(resolver(QueryProvider{null}),{true}).read(day))
        assertEquals(CalendarRead.Data(emptyList<CalendarOccurrence>()),AndroidCalendarRepository(resolver(QueryProvider{cursor()}),{true}).read(day))
        assertEquals(CalendarRead.Unavailable(CalendarFailure.QUERY_FAILED),AndroidCalendarRepository(resolver(QueryProvider{throw IllegalStateException("provider")}),{true}).read(day))
    }
    @Test fun securityExceptionAndRevocationDuringReadDiscardAllData()=runBlocking {
        assertEquals(CalendarRead.PermissionRequired,AndroidCalendarRepository(resolver(QueryProvider{throw SecurityException("revoked")}),{true}).read(day))
        var access=true
        val provider=QueryProvider{access=false;cursor(row())}
        assertEquals(CalendarRead.PermissionRequired,AndroidCalendarRepository(resolver(provider),{access}).read(day))
    }
    @Test fun invalidConsumedRowFailsAndClosesCursor()=runBlocking {
        var closed=false
        val bad=object:MatrixCursor(columns){override fun close(){closed=true;super.close()}}
        bad.addRow(row(id=0))
        assertEquals(CalendarRead.Unavailable(CalendarFailure.INVALID_DATA),AndroidCalendarRepository(resolver(QueryProvider{bad}),{true}).read(day))
        assertTrue(closed)
    }
    @Test fun cancellationSignalsBlockedProviderAndDoesNotBecomeAnError()=runBlocking {
        val entered=CountDownLatch(1);val released=CountDownLatch(1)
        var captured:CancellationSignal?=null
        val provider=QueryProvider { signal ->
            captured=signal;signal!!.setOnCancelListener{released.countDown()};entered.countDown()
            released.await(5,TimeUnit.SECONDS);signal.throwIfCanceled();cursor()
        }
        val repo=AndroidCalendarRepository(resolver(provider),{true})
        val job=launch(Dispatchers.Default){repo.read(day)}
        assertTrue(entered.await(5,TimeUnit.SECONDS));job.cancelAndJoin()
        assertTrue(captured!!.isCanceled);assertTrue(job.isCancelled)
    }
}

private class QueryProvider(val handler:(CancellationSignal?)->Cursor?):ContentProvider() {
    var calls=0;var uri:Uri?=null;var projection:Array<out String>?=null;var selection:String?=null
    override fun onCreate()=true
    override fun query(uri:Uri,projection:Array<out String>?,selection:String?,selectionArgs:Array<out String>?,sortOrder:String?):Cursor? =
        query(uri,projection,selection,selectionArgs,sortOrder,null)
    override fun query(uri:Uri,projection:Array<out String>?,selection:String?,selectionArgs:Array<out String>?,sortOrder:String?,cancellationSignal:CancellationSignal?):Cursor? {
        calls++;this.uri=uri;this.projection=projection;this.selection=selection
        return handler(cancellationSignal)
    }
    override fun getType(uri:Uri):String?=null
    override fun insert(uri:Uri,values:ContentValues?):Uri?=error("read only")
    override fun delete(uri:Uri,selection:String?,selectionArgs:Array<out String>?):Int=error("read only")
    override fun update(uri:Uri,values:ContentValues?,selection:String?,selectionArgs:Array<out String>?):Int=error("read only")
}
