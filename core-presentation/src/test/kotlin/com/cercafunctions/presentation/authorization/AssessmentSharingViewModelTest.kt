package com.cercafunctions.presentation.authorization
import androidx.lifecycle.ViewModelStore
import com.cercafunctions.domain.authorization.*
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.presentation.home.MainDispatcherRule
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import java.time.*
@OptIn(ExperimentalCoroutinesApi::class)
class AssessmentSharingViewModelTest {
    @get:Rule val main=MainDispatcherRule()
    private val store=ViewModelStore()
    private var access=true
    private var gate:CompletableDeferred<Unit>?=null
    private var publishes=0
    private val event=CalendarOccurrence(OccurrenceKey(1,Instant.parse("2026-11-04T09:00:00Z")),Instant.parse("2026-11-04T10:00:00Z"),"private","private")
    private val target=GrantTarget(CalendarSelection(event.key,event.end,CalendarDay(LocalDate.parse("2026-11-04"),ZoneOffset.UTC)),event)
    private val repo=object:GrantRepository {
        override val available=true;var grant:GrantRecord?=null
        override suspend fun read()=grant
        override suspend fun publish(grant:GrantRecord){gate?.await();publishes++;this.grant=grant}
        override suspend fun revokeAll(){grant=null}
        override suspend fun revokeIf(referenceHash:String){if(grant?.referenceHash==referenceHash)grant=null}
    }
    private fun model():AssessmentSharingViewModel {
        val calendar=GetCalendarDay(object:CalendarRepository {
            override fun hasReadAccess()=access
            override suspend fun read(day:CalendarDay)=CalendarRead.Data(listOf(event))
        })
        val grants=AssessmentAuthorizations(repo,RevalidateCalendarSelection(calendar),calendar,
            GrantClock{GrantTime(Instant.parse("2026-11-03T08:00:00Z"),1000,1)})
        return AssessmentSharingViewModel(grants,main.dispatcher).also{store.put("vm",it)}
    }
    @After fun clear(){store.clear()}
    @Test fun `initial status never issues token`()=runTest {
        val m=model();runCurrent();assertFalse(m.uiState.value.busy);assertNull(m.uiState.value.reference);assertEquals(0,publishes)
    }
    @Test fun `explicit authorization and revocation update scoped state`()=runTest {
        val m=model();runCurrent();m.setTarget(target);m.authorize();runCurrent()
        assertNotNull(m.uiState.value.reference);assertNotNull(m.uiState.value.active)
        m.revoke();runCurrent();assertNull(m.uiState.value.reference);assertNull(m.uiState.value.active);assertNull(repo.grant)
    }
    @Test fun `no selection cannot issue and failure contains no details`()=runTest {
        val m=model();runCurrent();m.authorize();runCurrent();assertEquals(0,publishes)
        m.setTarget(target);access=false;m.authorize();runCurrent()
        assertTrue(m.uiState.value.error);assertNull(m.uiState.value.reference);assertNull(m.uiState.value.active)
    }
    @Test fun `stop removes raw reference and recreation can revoke persisted grant`()=runTest {
        val m=model();runCurrent();m.setTarget(target);m.authorize();runCurrent();m.onStop()
        assertNull(m.uiState.value.reference);assertNotNull(repo.grant)
        val recreated=model();runCurrent();assertNotNull(recreated.uiState.value.active);assertNull(recreated.uiState.value.reference)
        recreated.revoke();runCurrent();assertNull(repo.grant)
    }
    @Test fun `stop cancels pending publication and suppresses token`()=runTest {
        val m=model();runCurrent();m.setTarget(target);gate=CompletableDeferred();m.authorize();m.authorize();runCurrent()
        assertTrue(m.uiState.value.busy);m.onStop();gate!!.complete(Unit);runCurrent()
        assertNull(m.uiState.value.reference);assertEquals(0,publishes);assertFalse(m.uiState.value.busy)
    }
    @Test fun `changing target never silently shares another occurrence`()=runTest {
        val m=model();runCurrent();m.setTarget(target);m.authorize();runCurrent()
        m.setTarget(null);assertNull(m.uiState.value.reference);assertNotNull(m.uiState.value.active);assertEquals(1,publishes)
    }
}
