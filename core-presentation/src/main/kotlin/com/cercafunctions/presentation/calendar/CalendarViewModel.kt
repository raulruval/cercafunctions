package com.cercafunctions.presentation.calendar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.estimate.ArrivalEstimate
import com.cercafunctions.domain.assessment.*
import java.time.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CalendarStatus { IDLE, ACCESS_REQUIRED, DENIED, LOADING, DATA, UNAVAILABLE, CHANGED }
enum class CalendarInputError { DATE, ZONE }
data class CalendarUiState(val date:String,val zone:String,val status:CalendarStatus=CalendarStatus.IDLE,
    val items:List<CalendarOccurrence> = emptyList(),val selection:CalendarSelection?=null,
    val selected:CalendarOccurrence?=null,val validated:Boolean=false,val busy:Boolean=false,
    val inputError:CalendarInputError?=null,val failure:CalendarFailure?=null,
    val arrival:ArrivalEstimate?=null,val assessment:AssessmentResult?=null,val assessmentBusy:Boolean=false)
@HiltViewModel
class CalendarViewModel @Inject constructor(private val getDay:GetCalendarDay,private val validate:RevalidateCalendarSelection,
    private val clock:Clock,defaultZone:ZoneId,private val assess:AssessArrival):ViewModel() {
    private val mutable=MutableStateFlow(CalendarUiState(clock.instant().atZone(defaultZone).toLocalDate().plusDays(1).toString(),defaultZone.id,
        status=if(getDay.hasReadAccess())CalendarStatus.IDLE else CalendarStatus.ACCESS_REQUIRED))
    val uiState=mutable.asStateFlow()
    private var generation=0L
    private var job:Job?=null
    private var loadedDay:CalendarDay?=null
    private var assessmentJob:Job?=null
    private var assessmentGeneration=0L
    private fun invalidateAssessment() {
        assessmentGeneration++;assessmentJob?.cancel()
        mutable.update{it.copy(assessment=null,assessmentBusy=false)}
    }
    fun setArrival(value:ArrivalEstimate?) {
        if(mutable.value.arrival==value)return
        invalidateAssessment();mutable.update{it.copy(arrival=value)}
    }
    fun evaluate() {
        val state=mutable.value
        if(state.busy || state.assessmentBusy)return
        invalidateAssessment()
        val token=assessmentGeneration
        mutable.update{it.copy(assessmentBusy=true)}
        assessmentJob=viewModelScope.launch {
            try {
                val result=assess(state.arrival,state.selection)
                if(token!=assessmentGeneration)return@launch
                if(result is AssessmentResult.Indeterminate) when(result.reason) {
                    AssessmentReason.CALENDAR_CHANGED -> clear(CalendarStatus.CHANGED)
                    AssessmentReason.CALENDAR_PERMISSION -> clear(CalendarStatus.ACCESS_REQUIRED)
                    AssessmentReason.CALENDAR_UNAVAILABLE -> {clear(CalendarStatus.UNAVAILABLE);mutable.update{it.copy(failure=CalendarFailure.QUERY_FAILED)}}
                    else -> Unit
                }
                mutable.update{it.copy(assessment=result,assessmentBusy=false,validated=if(result is AssessmentResult.Compared)true else it.validated)}
            } catch(cancelled:CancellationException){throw cancelled
            } catch(_:Exception) {
                if(token==assessmentGeneration) {
                    clear(CalendarStatus.UNAVAILABLE)
                    mutable.update{it.copy(failure=CalendarFailure.QUERY_FAILED,
                        assessment=AssessmentResult.Indeterminate(AssessmentReason.CALENDAR_UNAVAILABLE,state.arrival,clock.instant()))}
                }
            } finally {if(token==assessmentGeneration)mutable.update{it.copy(assessmentBusy=false)}}
        }
    }
    private fun clear(status:CalendarStatus=if(getDay.hasReadAccess())CalendarStatus.IDLE else CalendarStatus.ACCESS_REQUIRED) {
        invalidateAssessment()
        generation++;job?.cancel();loadedDay=null
        mutable.value=CalendarUiState(mutable.value.date,mutable.value.zone,status,arrival=mutable.value.arrival)
    }
    fun updateDate(value:String) {clear();mutable.update {it.copy(date=value)}}
    fun updateZone(value:String) {clear();mutable.update {it.copy(zone=value)}}
    fun tomorrow() {
        val zone=try{ZoneId.of(mutable.value.zone)}catch(_:DateTimeException){mutable.update{it.copy(inputError=CalendarInputError.ZONE)};return}
        updateDate(clock.instant().atZone(zone).toLocalDate().plusDays(1).toString())
    }
    private fun day():CalendarDay? {
        val state=mutable.value
        val date=try {
            require(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(state.date));LocalDate.parse(state.date)
        }catch(_:IllegalArgumentException){mutable.update{it.copy(inputError=CalendarInputError.DATE)};return null
        }catch(_:DateTimeException){mutable.update{it.copy(inputError=CalendarInputError.DATE)};return null}
        val zone=try{ZoneId.of(state.zone)}catch(_:DateTimeException){mutable.update{it.copy(inputError=CalendarInputError.ZONE)};return null}
        mutable.update{it.copy(inputError=null)}
        return CalendarDay(date,zone)
    }
    fun prepareConsult():Boolean {
        if(!getDay.hasReadAccess())clear(if(mutable.value.status==CalendarStatus.DENIED)CalendarStatus.DENIED else CalendarStatus.ACCESS_REQUIRED)
        return day()!=null
    }
    fun consult() {
        if(mutable.value.busy)return
        val queryDay=day() ?: return
        clear()
        val token=generation
        mutable.update{it.copy(status=CalendarStatus.LOADING,busy=true)}
        job=viewModelScope.launch {
            try {
                val read=getDay(queryDay.date,queryDay.zone)
                if(token!=generation)return@launch
                when(read) {
                    is CalendarRead.Data -> {loadedDay=queryDay;mutable.update{it.copy(status=CalendarStatus.DATA,items=read.occurrences)}}
                    CalendarRead.PermissionRequired -> clear(CalendarStatus.ACCESS_REQUIRED)
                    is CalendarRead.Unavailable -> mutable.update{it.copy(status=CalendarStatus.UNAVAILABLE,failure=read.reason)}
                }
            } catch(cancelled:CancellationException){throw cancelled
            } catch(_:Exception){if(token==generation)mutable.update{it.copy(status=CalendarStatus.UNAVAILABLE,failure=CalendarFailure.QUERY_FAILED)}
            } finally {
                if(token==generation)mutable.update{it.copy(busy=false,status=if(it.status==CalendarStatus.LOADING)CalendarStatus.IDLE else it.status)}
            }
        }
    }
    fun onPermissionResult(granted:Boolean) {if(granted)consult() else clear(CalendarStatus.DENIED)}
    fun onStop() { invalidateAssessment() }
    fun onResume() {
        invalidateAssessment()
        if(!getDay.hasReadAccess())clear(if(mutable.value.status==CalendarStatus.DENIED)CalendarStatus.DENIED else CalendarStatus.ACCESS_REQUIRED)
        else mutable.update{it.copy(validated=false,status=if(it.status in setOf(CalendarStatus.DENIED,CalendarStatus.ACCESS_REQUIRED))CalendarStatus.IDLE else it.status)}
    }
    fun select(key:OccurrenceKey) {
        if(!getDay.hasReadAccess()){clear(CalendarStatus.ACCESS_REQUIRED);return}
        val state=mutable.value
        if(state.busy || state.status!=CalendarStatus.DATA)return
        val item=state.items.find{it.key==key} ?: return
        val day=loadedDay ?: return
        invalidateAssessment()
        mutable.update{it.copy(selection=CalendarSelection(item.key,item.end,day),selected=item,validated=false)}
    }
    fun revalidate() {
        val state=mutable.value
        val selection=state.selection ?: return
        if(state.busy)return
        invalidateAssessment()
        val token=++generation
        mutable.update{it.copy(busy=true,validated=false)}
        job=viewModelScope.launch {
            try {
                val result=validate(selection)
                if(token!=generation)return@launch
                when(result) {
                    is SelectionValidation.Valid -> mutable.update{it.copy(selected=result.occurrence,validated=true,
                        items=it.items.map{item->if(item.key==selection.key)result.occurrence else item})}
                    SelectionValidation.MissingOrChanged -> clear(CalendarStatus.CHANGED)
                    SelectionValidation.PermissionRequired -> clear(CalendarStatus.ACCESS_REQUIRED)
                    is SelectionValidation.Unavailable -> {clear(CalendarStatus.UNAVAILABLE);mutable.update{it.copy(failure=result.reason)}}
                }
            } catch(cancelled:CancellationException){throw cancelled
            } catch(_:Exception){if(token==generation){clear(CalendarStatus.UNAVAILABLE);mutable.update{it.copy(failure=CalendarFailure.QUERY_FAILED)}}
            } finally {if(token==generation)mutable.update{it.copy(busy=false)}}
        }
    }
}
