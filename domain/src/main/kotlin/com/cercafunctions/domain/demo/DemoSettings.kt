package com.cercafunctions.domain.demo

/** Only the train is synthetic. Calendar access and disclosure remain separate decisions. */
enum class DemoScenario { ON_TIME, DELAYED, UNKNOWN }
data class DemoSettings(val scenario:DemoScenario=DemoScenario.DELAYED,val agentAllowed:Boolean=false)
interface DemoSettingsRepository {
    suspend fun get():DemoSettings
    suspend fun setScenario(scenario:DemoScenario)
    suspend fun setAgentAllowed(allowed:Boolean)
}
