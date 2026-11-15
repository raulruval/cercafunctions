package com.cercafunctions.platform.authorization
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.cercafunctions.domain.authorization.*
import com.cercafunctions.domain.calendar.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import java.time.*
import java.io.IOException
class DataStoreGrantRepositoryTest {
    private class Store:DataStore<Preferences> {
        val value=MutableStateFlow<Preferences>(emptyPreferences())
        var fail=false
        override val data:Flow<Preferences> = flow{if(fail)throw IOException("private");emitAll(value)}
        override suspend fun updateData(transform:suspend(Preferences)->Preferences):Preferences {
            if(fail)throw IOException("private")
            return transform(value.value).also{value.value=it}
        }
    }
    private val start=Instant.parse("2026-11-04T09:00:00Z")
    private val record=GrantRecord("a".repeat(64),"b".repeat(64),CalendarSelection(OccurrenceKey(99,start),start.plusSeconds(3600),CalendarDay(LocalDate.parse("2026-11-04"),ZoneOffset.UTC)),
        GrantTime(start.minusSeconds(600),10_000,2),start,610_000)
    @Test fun roundTripAcrossRepositoryInstancesAndRevoke()=runTest {
        val s=Store();val r=DataStoreGrantRepository(s,true){}
        assertNull(r.read());r.publish(record)
        assertEquals(record,DataStoreGrantRepository(s,true){}.read());r.revokeAll();assertNull(r.read())
    }
    @Test fun systemEnablePrecedesPublishAndClearPrecedesDisable()=runTest {
        val s=Store();val seen=mutableListOf<GrantRecord?>();lateinit var r:DataStoreGrantRepository
        r=DataStoreGrantRepository(s,true){seen+=r.read()}
        r.publish(record);r.revokeAll();assertEquals(listOf(null,null),seen)
    }
    @Test fun oldRequestCannotInvalidateNewGrantOrDisableItsFunction()=runTest {
        val s=Store();val calls=mutableListOf<Boolean>();val r=DataStoreGrantRepository(s,true){calls+=it}
        r.publish(record);val next=record.copy(referenceHash="c".repeat(64));r.publish(next)
        r.revokeIf(record.referenceHash);assertEquals(next,r.read());assertEquals(listOf(true,true),calls)
        r.revokeIf(next.referenceHash);assertNull(r.read());assertEquals(false,calls.last())
    }
    @Test fun failedEnableAndCancelledPublishDoNotGrantAndFailedDisableStillClears()=runTest {
        val s=Store();val r=DataStoreGrantRepository(s,true){throw IOException("private")}
        try{r.publish(record);fail()}catch(_:IOException){};assertNull(r.read())
        val good=DataStoreGrantRepository(s,true){};good.publish(record)
        try{r.revokeAll();fail()}catch(_:IOException){};assertNull(good.read())
        val cancelled=DataStoreGrantRepository(s,true){throw CancellationException("cancel")}
        try{cancelled.publish(record);fail()}catch(_:CancellationException){};assertNull(good.read())
    }
    @Test fun corruptRecordAndUnavailableStorageDenyRatherThanReconstructingPartialGrant()=runTest {
        val s=Store();val r=DataStoreGrantRepository(s,true){};r.publish(record)
        s.value.value=s.value.value.toMutablePreferences().apply{remove(stringPreferencesKey("assessment_reference_hash"))}
        try{r.read();fail()}catch(_:AssessmentAccessDenied){}
        s.fail=true;try{r.read();fail()}catch(_:IOException){}
    }
    @Test fun unrelatedJourneyPreferencesRemainAndUnsupportedCannotPublish()=runTest {
        val s=Store();val key=stringPreferencesKey("origin")
        s.value.value=mutablePreferencesOf(key to "POC origin")
        val r=DataStoreGrantRepository(s,true){};r.publish(record);r.revokeAll()
        assertEquals("POC origin",s.value.value[key])
        val unsupported=DataStoreGrantRepository(s,false){error("system absent")}
        try{unsupported.publish(record);fail()}catch(_:UnsupportedOperationException){}
        unsupported.revokeAll();assertNull(unsupported.read())
    }
}
