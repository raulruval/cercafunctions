package com.cercafunctions.platform.demo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.cercafunctions.domain.demo.*
import kotlinx.coroutines.flow.first

/** New keys in the existing singleton store; no personal calendar content is persisted. */
class DataStoreDemoSettingsRepository(private val store:DataStore<Preferences>):DemoSettingsRepository {
    override suspend fun get():DemoSettings {
        val saved=store.data.first()
        val scenario=saved[SCENARIO]?.let(DemoScenario::valueOf) ?: DemoScenario.DELAYED
        return DemoSettings(scenario,saved[AGENT_ALLOWED] ?: false)
    }
    override suspend fun setScenario(scenario:DemoScenario){store.edit{it[SCENARIO]=scenario.name}}
    override suspend fun setAgentAllowed(allowed:Boolean){store.edit{it[AGENT_ALLOWED]=allowed}}
    private companion object {
        val SCENARIO=stringPreferencesKey("simple_demo_scenario")
        val AGENT_ALLOWED=booleanPreferencesKey("simple_demo_agent_allowed")
    }
}
