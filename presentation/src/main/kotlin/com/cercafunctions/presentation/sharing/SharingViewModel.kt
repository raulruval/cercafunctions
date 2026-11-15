package com.cercafunctions.presentation.sharing
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cercafunctions.domain.sharing.JourneySharingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
data class SharingUiState(val available:Boolean,val allowed:Boolean=false,val loading:Boolean=true,val busy:Boolean=false,val error:Boolean=false)
@HiltViewModel
class SharingViewModel @Inject constructor(private val repository:JourneySharingRepository):ViewModel() {
    private val mutable=MutableStateFlow(SharingUiState(repository.available))
    val uiState=mutable.asStateFlow()
    private var observer:Job?=null
    init{refresh()}
    fun refresh() {
        observer?.cancel();mutable.update{it.copy(loading=true,allowed=false,error=false)}
        observer=viewModelScope.launch {
            try{repository.allowed.collect{value->mutable.update{it.copy(allowed=value,loading=false)}}}
            catch(cancelled:CancellationException){throw cancelled}
            catch(_:Exception){mutable.update{it.copy(allowed=false,loading=false,error=true)}}
        }
    }
    fun change() {
        val state=mutable.value
        if(state.loading || state.busy || (!state.available && !state.allowed))return
        mutable.update{it.copy(busy=true,error=false)}
        viewModelScope.launch {
            try{repository.setAllowed(!state.allowed)}
            catch(cancelled:CancellationException){throw cancelled}
            catch(_:Exception){mutable.update{it.copy(error=true)}}
            finally{mutable.update{it.copy(busy=false)}}
        }
    }
}
