package com.cercafunctions.platform.sharing
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException

class DataStoreJourneySharingRepositoryTest {
    private class Store:DataStore<Preferences> {
        val state=MutableStateFlow<Preferences>(emptyPreferences())
        var fail=false
        override val data:Flow<Preferences> get()=flow{if(fail)throw IOException("private-path");emitAll(state)}
        override suspend fun updateData(transform:suspend(Preferences)->Preferences):Preferences {
            if(fail)throw IOException("private-path")
            return transform(state.value).also{state.value=it}
        }
    }
    @Test fun `new consent is false and persists across repository recreation`()=runTest {
        val s=Store();val r=DataStoreJourneySharingRepository(s,true){}
        assertFalse(r.isAllowed());r.setAllowed(true)
        assertTrue(DataStoreJourneySharingRepository(s,true){}.isAllowed())
        r.setAllowed(false);assertFalse(r.isAllowed())
    }
    @Test fun `enable system before publishing consent and revoke consent before system`()=runTest {
        val s=Store();lateinit var r:DataStoreJourneySharingRepository
        val observed=mutableListOf<Boolean>()
        r=DataStoreJourneySharingRepository(s,true){observed+=r.isAllowed()}
        r.setAllowed(true);r.setAllowed(false)
        assertEquals(listOf(false,false),observed);assertFalse(r.isAllowed())
    }
    @Test fun `failed enable cannot grant consent and failed disable still revokes`()=runTest {
        val s=Store();val good=DataStoreJourneySharingRepository(s,true){}
        val bad=DataStoreJourneySharingRepository(s,true){throw IOException("private-system")}
        try{bad.setAllowed(true);fail()}catch(_:IOException){}
        assertFalse(good.isAllowed());good.setAllowed(true)
        try{bad.setAllowed(false);fail()}catch(_:IOException){}
        assertFalse(good.isAllowed())
    }
    @Test fun `unsupported enable rejects but revoke is possible`()=runTest {
        val r=DataStoreJourneySharingRepository(Store(),false){error("no system call")}
        try{r.setAllowed(true);fail()}catch(_:UnsupportedOperationException){}
        r.setAllowed(false);assertFalse(r.isAllowed())
    }
    @Test fun `storage failure propagates and cancellation cannot become consent`()=runTest {
        val s=Store();s.fail=true;val r=DataStoreJourneySharingRepository(s,true){}
        try{r.isAllowed();fail()}catch(_:IOException){}
        s.fail=false;val cancelled=DataStoreJourneySharingRepository(s,true){throw CancellationException("cancelled")}
        try{cancelled.setAllowed(true);fail()}catch(_:CancellationException){}
        assertFalse(r.isAllowed())
    }
}
