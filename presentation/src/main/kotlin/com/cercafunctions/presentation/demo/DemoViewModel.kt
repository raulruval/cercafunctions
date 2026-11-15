package com.cercafunctions.presentation.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cercafunctions.domain.demo.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class DemoUiState(val busy:Boolean=true,val settings:DemoSettings=DemoSettings(),val result:DemoResult?=null,val failed:Boolean=false)

@HiltViewModel
class DemoViewModel @Inject constructor(private val evaluate:EvaluateDemoJourney,
    private val repository:DemoSettingsRepository):ViewModel() {
    private val state=MutableStateFlow(DemoUiState())
    val uiState:StateFlow<DemoUiState> = state.asStateFlow()
    private var work:Job?=null
    init {refresh()}
    fun refresh(){if(work?.isActive!=true)load()}
    fun select(scenario:DemoScenario){if(!state.value.busy)load{repository.setScenario(scenario)}}
    fun setAgentAllowed(allowed:Boolean){if(!state.value.busy)load{repository.setAgentAllowed(allowed)}}
    private fun load(change:suspend()->Unit={}) {
        state.value=state.value.copy(busy=true,failed=false,result=null)
        work=viewModelScope.launch {
            try {
                change()
                val settings=repository.get()
                val result=evaluate()
                state.value=DemoUiState(busy=false,settings=settings,result=result)
            } catch(cancelled:CancellationException){throw cancelled
            } catch(_:Exception){state.value=state.value.copy(busy=false,failed=true,result=null)}
        }
    }
}
