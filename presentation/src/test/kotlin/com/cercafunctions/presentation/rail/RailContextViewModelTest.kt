package com.cercafunctions.presentation.rail

import androidx.lifecycle.ViewModelStore
import com.cercafunctions.domain.rail.*
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.HabitualJourney
import com.cercafunctions.presentation.home.MainDispatcherRule
import java.time.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class RailContextViewModelTest {
    @get:Rule val main=MainDispatcherRule()
    private val store=ViewModelStore()
    private val now=Instant.parse("2026-11-02T07:00:00Z")
    private val arrival=ArrivalEstimate(HabitualJourney("A","B",35),now.plusSeconds(86400),now.plusSeconds(88500),ZoneOffset.UTC,ArrivalSource.REFERENCE,ArrivalScenario.REFERENCE,now,null)
    private val stations=listOf(RailStation("70100","Vicálvaro"),RailStation("70108","Coslada"))
    private var reads=0
    private var gate:CompletableDeferred<Unit>?=null
    private var cancel=false
    private fun model():RailContextViewModel {
        val repo=object:RailContextRepository {
            override val sourceUrl="https://gtfsrt.renfe.com/alerts.json"
            override suspend fun read():RailFeedRead {reads++;gate?.await();if(cancel)throw CancellationException("injected");return RailFeedRead.Success(RailFeed(now,emptyList()))}
        }
        return RailContextViewModel(QueryRailContext(repo,RailStationCatalog{stations},Clock.fixed(now,ZoneOffset.UTC))).also {store.put("vm",it)}
    }
    private fun select(m:RailContextViewModel){m.selectOrigin("70100");m.selectDestination("70108")}
    @After fun clear(){store.clear()}
    @Test fun `search supports id and names but never auto selects`()=runTest {
        val m=model();m.setArrival(arrival);m.updateOrigin("70100")
        assertEquals(stations.take(1),m.suggestions("70100"))
        assertEquals(stations.drop(1),m.suggestions("cos"))
        assertTrue(m.suggestions("c").isEmpty())
        assertNull(m.uiState.value.origin)
        m.consult();runCurrent()
        assertEquals(RailStatus.UNRESOLVED,m.uiState.value.result?.status);assertEquals(0,reads)
    }
    @Test fun `explicit selection enables local filtering and query`()=runTest {
        val m=model();m.setArrival(arrival);select(m);m.consult();runCurrent()
        assertEquals(1,reads);assertEquals(RailStatus.AVAILABLE,m.uiState.value.result?.status)
        m.updateOrigin("edited")
        assertNull(m.uiState.value.origin);assertNull(m.uiState.value.result)
    }
    @Test fun `same arrival after recreation preserves context but new arrival clears association`()=runTest {
        val m=model();m.setArrival(arrival);select(m);m.consult();runCurrent()
        val result=m.uiState.value.result
        m.setArrival(arrival.copy());assertEquals(result,m.uiState.value.result)
        m.setArrival(arrival.copy(arrival=arrival.arrival!!.plusSeconds(300)))
        assertNull(m.uiState.value.result);assertNull(m.uiState.value.origin)
    }
    @Test fun `editing cancels in flight and prevents stale publication and duplicate queries`()=runTest {
        gate=CompletableDeferred()
        val m=model();m.setArrival(arrival);select(m);m.consult();m.consult();runCurrent()
        assertEquals(1,reads);assertTrue(m.uiState.value.busy)
        m.updateDestination("new");gate!!.complete(Unit);runCurrent()
        assertFalse(m.uiState.value.busy);assertNull(m.uiState.value.result)
    }
    @Test fun `demo and invalid ids never reach network`()=runTest {
        val m=model();m.setArrival(arrival.copy(source=ArrivalSource.FIXTURE,scenario=ArrivalScenario.DEMO_DELAYED));select(m);m.consult();runCurrent()
        assertEquals(RailStatus.DEMO_SKIPPED,m.uiState.value.result?.status);assertEquals(0,reads)
        m.setArrival(arrival);m.selectOrigin("invented")
        assertNull(m.uiState.value.origin)
    }
    @Test fun `cancellation is not an unavailable result`()=runTest {
        cancel=true
        val m=model();m.setArrival(arrival);select(m);m.consult();runCurrent()
        assertFalse(m.uiState.value.busy);assertNull(m.uiState.value.result)
    }
}
