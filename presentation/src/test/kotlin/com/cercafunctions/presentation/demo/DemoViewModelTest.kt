package com.cercafunctions.presentation.demo

import androidx.lifecycle.ViewModelStore
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.demo.*
import com.cercafunctions.presentation.home.MainDispatcherRule
import java.time.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DemoViewModelTest {
    @get:Rule val main=MainDispatcherRule()
    private val store=ViewModelStore()
    private var saved=DemoSettings()
    private var permission=false
    private var broken=false
    private var gate:CompletableDeferred<Unit>?=null
    private val repository=object:DemoSettingsRepository {
        override suspend fun get():DemoSettings {gate?.await();if(broken)error("private storage");return saved}
        override suspend fun setScenario(scenario:DemoScenario){saved=saved.copy(scenario=scenario)}
        override suspend fun setAgentAllowed(allowed:Boolean){saved=saved.copy(agentAllowed=allowed)}
    }
    private val calendar=object:CalendarRepository {
        override fun hasReadAccess()=permission
        override suspend fun read(day:CalendarDay)=CalendarRead.Data(emptyList())
    }
    private fun model()=DemoViewModel(EvaluateDemoJourney(GetCalendarDay(calendar),repository,
        Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"),ZoneOffset.UTC),ZoneOffset.UTC),repository).also{store.put("demo",it)}
    @After fun clear(){store.clear()}
    @Test fun `permission outcome refreshes when access changes`()=runTest {
        val vm=model();runCurrent();assertEquals(DemoResult.PermissionRequired,vm.uiState.value.result)
        permission=true;vm.refresh();runCurrent()
        assertEquals(DemoResult.NoMeeting(LocalDate.parse("2026-11-05"),ZoneOffset.UTC),vm.uiState.value.result)
    }
    @Test fun `scenario and explicit sharing persist independently`()=runTest {
        val vm=model();runCurrent();assertFalse(vm.uiState.value.settings.agentAllowed)
        vm.select(DemoScenario.ON_TIME);runCurrent()
        vm.setAgentAllowed(true);runCurrent()
        assertEquals(DemoSettings(DemoScenario.ON_TIME,true),saved)
        vm.setAgentAllowed(false);runCurrent();assertFalse(vm.uiState.value.settings.agentAllowed)
    }
    @Test fun `loading guards against competing changes and hides stale result`()=runTest {
        val vm=model();runCurrent();gate=CompletableDeferred()
        vm.refresh();vm.select(DemoScenario.UNKNOWN);runCurrent()
        assertTrue(vm.uiState.value.busy);assertNull(vm.uiState.value.result)
        assertEquals(DemoScenario.DELAYED,saved.scenario)
        gate!!.complete(Unit);runCurrent();assertFalse(vm.uiState.value.busy)
    }
    @Test fun `errors clear result and refresh can recover`()=runTest {
        val vm=model();runCurrent();broken=true;vm.refresh();runCurrent()
        assertTrue(vm.uiState.value.failed);assertNull(vm.uiState.value.result)
        broken=false;vm.refresh();runCurrent();assertFalse(vm.uiState.value.failed)
    }
    @Test fun `clearing ViewModel cancels in flight work`()=runTest {
        gate=CompletableDeferred();val vm=model();runCurrent()
        clear();gate!!.complete(Unit);runCurrent()
        assertTrue(vm.uiState.value.busy);assertFalse(vm.uiState.value.failed)
    }
}
