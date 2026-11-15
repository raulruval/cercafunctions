package com.cercafunctions.platform.demo

import androidx.datastore.preferences.core.*
import com.cercafunctions.domain.demo.*
import java.io.File
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreDemoSettingsRepositoryTest {
    @get:Rule val temporary=TemporaryFolder()
    @Test fun `safe default and independent changes survive store recreation`()=runTest {
        val file=File(temporary.root,"demo.preferences_pb")
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
        val store=PreferenceDataStoreFactory.create(scope=scope,produceFile={file})
        try {
            val repository=DataStoreDemoSettingsRepository(store)
            assertEquals(DemoSettings(),repository.get())
            repository.setScenario(DemoScenario.UNKNOWN)
            repository.setAgentAllowed(true)
            assertEquals(DemoSettings(DemoScenario.UNKNOWN,true),repository.get())
            repository.setAgentAllowed(false)
        } finally {scope.coroutineContext.job.cancelAndJoin()}
        val secondScope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
        try {
            val second=PreferenceDataStoreFactory.create(scope=secondScope,produceFile={file})
            assertEquals(DemoSettings(DemoScenario.UNKNOWN,false),DataStoreDemoSettingsRepository(second).get())
        } finally {secondScope.coroutineContext.job.cancelAndJoin()}
    }
    @Test fun `invalid persisted scenario fails instead of silently granting access`()=runTest {
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
        try {
            val store=PreferenceDataStoreFactory.create(scope=scope,produceFile={File(temporary.root,"invalid.preferences_pb")})
            store.edit{it[stringPreferencesKey("simple_demo_scenario")]="REFERENCE"}
            try{DataStoreDemoSettingsRepository(store).get();fail()}catch(_:IllegalArgumentException){}
        } finally {scope.coroutineContext.job.cancelAndJoin()}
    }
}
