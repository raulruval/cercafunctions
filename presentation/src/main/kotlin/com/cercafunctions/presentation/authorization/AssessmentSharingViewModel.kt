package com.cercafunctions.presentation.authorization
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cercafunctions.domain.authorization.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Qualifier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AuthorizationIo
data class AuthorizationUiState(val available:Boolean,val target:GrantTarget?=null,val active:GrantSummary?=null,
    val reference:String?=null,val busy:Boolean=true,val error:Boolean=false)
@HiltViewModel
class AssessmentSharingViewModel @Inject constructor(private val authorizations:AssessmentAuthorizations,
    @AuthorizationIo private val io:CoroutineDispatcher):ViewModel() {
    private val mutable=MutableStateFlow(AuthorizationUiState(authorizations.available))
    val uiState=mutable.asStateFlow()
    private var job:Job?=null
    private var generation=0L
    private var issuing=false
    init{mutable.update{it.copy(busy=false)};refresh()}
    fun setTarget(target:GrantTarget?) {
        if(target==mutable.value.target)return
        if(issuing){generation++;job?.cancel();issuing=false;mutable.update{it.copy(busy=false)}}
        mutable.update{it.copy(target=target,reference=null)}
    }
    private fun operation(issue:Boolean=false,block:suspend()->Unit) {
        if(mutable.value.busy)return
        val token=++generation;issuing=issue
        mutable.update{it.copy(busy=true,error=false)}
        job=viewModelScope.launch {
            try{block()}
            catch(cancelled:CancellationException){throw cancelled}
            catch(_:Exception){if(token==generation)mutable.update{it.copy(error=true,reference=null,active=null)}}
            finally{if(token==generation){issuing=false;mutable.update{it.copy(busy=false)}}}
        }
    }
    fun refresh()=operation {
        val status=withContext(io){authorizations.status()}
        mutable.update{it.copy(active=status,reference=null)}
    }
    fun authorize() {
        val target=mutable.value.target ?: return
        if(!mutable.value.available)return
        operation(true) {
            val issued=withContext(io){authorizations.issue(target)}
            currentCoroutineContext().ensureActive()
            mutable.update{it.copy(reference=issued.reference,active=GrantSummary(issued.expiresAt,issued.meetingStart))}
        }
    }
    fun revoke()=operation {
        withContext(io){authorizations.revoke()}
        mutable.update{it.copy(reference=null,active=null)}
    }
    fun onStop() {
        if(issuing){generation++;job?.cancel();issuing=false;mutable.update{it.copy(busy=false)}}
        mutable.update{it.copy(reference=null)}
    }
}
