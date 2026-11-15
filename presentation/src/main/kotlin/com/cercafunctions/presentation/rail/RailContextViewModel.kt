package com.cercafunctions.presentation.rail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cercafunctions.domain.rail.*
import com.cercafunctions.domain.estimate.ArrivalEstimate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class RailUiState(val arrival:ArrivalEstimate?=null, val originText:String="",val destinationText:String="",
    val origin:RailStation?=null,val destination:RailStation?=null,val busy:Boolean=false,
    val result:RailContext?=null,val failed:Boolean=false)

@HiltViewModel
class RailContextViewModel @Inject constructor(private val query:QueryRailContext):ViewModel() {
    private val mutable=MutableStateFlow(RailUiState())
    val uiState=mutable.asStateFlow()
    private val stations=query.stations()
    private var generation=0L
    private var job:Job?=null
    fun suggestions(input:String):List<RailStation> = if(input.trim().length<2) emptyList() else stations
        .filter {it.id.contains(input.trim()) || it.name.contains(input.trim(),ignoreCase=true)}.take(5)
    private fun invalidate() {
        generation++;job?.cancel()
        mutable.update {it.copy(result=null,busy=false,failed=false)}
    }
    fun setArrival(value:ArrivalEstimate?) {
        if(mutable.value.arrival==value)return
        invalidate();mutable.value=RailUiState(arrival=value)
    }
    fun updateOrigin(value:String) { invalidate();mutable.update {it.copy(originText=value,origin=null)} }
    fun updateDestination(value:String) { invalidate();mutable.update {it.copy(destinationText=value,destination=null)} }
    fun selectOrigin(id:String) {
        val station=stations.find {it.id==id} ?: return
        invalidate();mutable.update {it.copy(origin=station,originText="${station.name} · ${station.id}")}
    }
    fun selectDestination(id:String) {
        val station=stations.find {it.id==id} ?: return
        invalidate();mutable.update {it.copy(destination=station,destinationText="${station.name} · ${station.id}")}
    }
    fun consult() {
        val state=mutable.value
        val arrival=state.arrival ?: return
        if(state.busy)return
        val selection=if(state.origin!=null && state.destination!=null) RailSelection(state.origin.id,state.destination.id) else null
        val token=++generation
        mutable.update {it.copy(result=null,busy=true,failed=false)}
        job=viewModelScope.launch {
            try {
                val result=query(arrival,selection)
                if(generation==token)mutable.update {it.copy(result=result)}
            } catch(cancelled:CancellationException) {throw cancelled
            } catch(_:Exception) {if(generation==token)mutable.update {it.copy(failed=true)}
            } finally {if(generation==token)mutable.update {it.copy(busy=false)}}
        }
    }
}
