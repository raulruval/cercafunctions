package com.cercafunctions.presentation.sharing
import androidx.lifecycle.ViewModelStore
import com.cercafunctions.domain.sharing.JourneySharingRepository
import com.cercafunctions.presentation.home.MainDispatcherRule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
@OptIn(ExperimentalCoroutinesApi::class)
class SharingViewModelTest {
    @get:Rule val main=MainDispatcherRule()
    private val store=ViewModelStore()
    private val flag=MutableStateFlow(false)
    private var available=true
    private var readFails=false
    private var writeFails=false
    private var gate:CompletableDeferred<Unit>?=null
    private var calls=0
    private fun model()=SharingViewModel(object:JourneySharingRepository {
        override val available=this@SharingViewModelTest.available
        override val allowed=flow{if(readFails)error("private read");emitAll(flag)}
        override suspend fun setAllowed(value:Boolean){calls++;gate?.await();if(writeFails)error("private write");flag.value=value}
    }).also{store.put("vm",it)}
    @After fun clear(){store.clear()}
    @Test fun `reads initial false without enabling`()=runTest {
        val m=model();runCurrent();assertFalse(m.uiState.value.loading);assertFalse(m.uiState.value.allowed);assertEquals(0,calls)
    }
    @Test fun `only explicit actions enable and revoke`()=runTest {
        val m=model();runCurrent();m.change();runCurrent();assertTrue(m.uiState.value.allowed)
        m.change();runCurrent();assertFalse(m.uiState.value.allowed);assertEquals(2,calls)
    }
    @Test fun `unsupported enable is unavailable but existing consent may be revoked`()=runTest {
        available=false;val m=model();runCurrent();m.change();runCurrent();assertEquals(0,calls)
        flag.value=true;runCurrent();m.change();runCurrent();assertFalse(flag.value);assertEquals(1,calls)
    }
    @Test fun `pending operation blocks duplicate action`()=runTest {
        val m=model();runCurrent();gate=CompletableDeferred();m.change();m.change();runCurrent()
        assertTrue(m.uiState.value.busy);assertFalse(m.uiState.value.allowed);assertEquals(1,calls)
        gate!!.complete(Unit);runCurrent();assertTrue(m.uiState.value.allowed);assertFalse(m.uiState.value.busy)
    }
    @Test fun `write failure has bounded error and no optimistic grant`()=runTest {
        val m=model();runCurrent();writeFails=true;m.change();runCurrent()
        assertTrue(m.uiState.value.error);assertFalse(m.uiState.value.allowed);assertFalse(m.uiState.value.busy)
    }
    @Test fun `read failure closes UI and explicit refresh recovers`()=runTest {
        readFails=true;val m=model();runCurrent();assertTrue(m.uiState.value.error);assertFalse(m.uiState.value.allowed)
        readFails=false;m.refresh();runCurrent();assertFalse(m.uiState.value.error);assertFalse(m.uiState.value.loading)
    }
    @Test fun `clearing ViewModel cancels pending consent write`()=runTest {
        val m=model();runCurrent();gate=CompletableDeferred();m.change();runCurrent();store.clear()
        gate!!.complete(Unit);runCurrent();assertFalse(flag.value)
    }
}
