package com.cercafunctions.platform.calendar

import android.content.ContentResolver
import android.content.ContentUris
import android.os.CancellationSignal
import android.os.OperationCanceledException
import android.provider.CalendarContract.*
import com.cercafunctions.domain.calendar.*
import java.time.Instant
import kotlinx.coroutines.*
import kotlin.coroutines.resume

/** Android-only read boundary. Permission requests and Activity are never used here. */
class AndroidCalendarRepository(private val resolver:ContentResolver,private val permissionCheck:()->Boolean,
    private val io:CoroutineDispatcher=Dispatchers.IO):CalendarRepository {
    override fun hasReadAccess():Boolean=permissionCheck()
    override suspend fun read(day:CalendarDay):CalendarRead=withContext(io) {
        ensureActive()
        suspendCancellableCoroutine { continuation ->
            val signal=CancellationSignal()
            continuation.invokeOnCancellation {signal.cancel()}
            try {
                if(!hasReadAccess()) {continuation.resume(CalendarRead.PermissionRequired);return@suspendCancellableCoroutine}
                if(day.start==day.end) {continuation.resume(CalendarRead.Data(emptyList()));return@suspendCancellableCoroutine}
                val builder=Instances.CONTENT_URI.buildUpon()
                ContentUris.appendId(builder,day.start.toEpochMilli())
                ContentUris.appendId(builder,day.end.toEpochMilli())
                val cursor=resolver.query(builder.build(),PROJECTION,
                    "${Instances.BEGIN} >= ? AND ${Instances.BEGIN} < ? AND ${Instances.ALL_DAY} = 0 AND (${Instances.STATUS} IS NULL OR ${Instances.STATUS} != ?) AND ${Calendars.VISIBLE} = 1",
                    arrayOf(day.start.toEpochMilli().toString(),day.end.toEpochMilli().toString(),Events.STATUS_CANCELED.toString()),
                    "${Instances.BEGIN} ASC, ${Instances.EVENT_ID} ASC",signal)
                if(cursor==null) {continuation.resume(CalendarRead.Unavailable(CalendarFailure.PROVIDER_UNAVAILABLE));return@suspendCancellableCoroutine}
                val items=cursor.use { rows ->
                    buildList {
                        while(rows.moveToNext()) {
                            signal.throwIfCanceled()
                            if(rows.getInt(5)!=0 || (!rows.isNull(6) && rows.getInt(6)==Events.STATUS_CANCELED) || rows.getInt(7)!=1) continue
                            require(!rows.isNull(0) && !rows.isNull(1) && !rows.isNull(2))
                            val begin=Instant.ofEpochMilli(rows.getLong(1))
                            if(!day.contains(begin))continue
                            add(CalendarOccurrence(OccurrenceKey(rows.getLong(0),begin),Instant.ofEpochMilli(rows.getLong(2)),
                                rows.getString(3).orEmpty(),rows.getString(4).orEmpty()))
                        }
                    }
                }
                signal.throwIfCanceled()
                continuation.resume(if(hasReadAccess()) CalendarRead.Data(items) else CalendarRead.PermissionRequired)
            } catch(cancelled:CancellationException) {continuation.cancel(cancelled)
            } catch(_:OperationCanceledException) {
                if(signal.isCanceled)continuation.cancel(CancellationException("Calendar query cancelled"))
                else continuation.resume(CalendarRead.Unavailable(CalendarFailure.QUERY_FAILED))
            } catch(_:SecurityException) {continuation.resume(CalendarRead.PermissionRequired)
            } catch(_:IllegalArgumentException) {continuation.resume(CalendarRead.Unavailable(CalendarFailure.INVALID_DATA))
            } catch(_:RuntimeException) {continuation.resume(CalendarRead.Unavailable(CalendarFailure.QUERY_FAILED))}
        }
    }
    private companion object {
        val PROJECTION=arrayOf(Instances.EVENT_ID,Instances.BEGIN,Instances.END,Instances.TITLE,
            Calendars.CALENDAR_DISPLAY_NAME,Instances.ALL_DAY,Instances.STATUS,Calendars.VISIBLE)
    }
}
