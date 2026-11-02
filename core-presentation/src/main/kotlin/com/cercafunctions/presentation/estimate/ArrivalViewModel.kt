package com.cercafunctions.presentation.estimate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.update
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.journey.HabitualJourney
import java.time.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ArrivalInputError { DATE, TIME, ZONE, UNAVAILABLE }
data class ArrivalUiState(
    val date: String, val time: String = "08:00", val zone: String,
    val scenario: ArrivalScenario = ArrivalScenario.REFERENCE,
    val selectedOffset: ZoneOffset? = null,
    val offsets: List<ZoneOffset> = emptyList(),
    val result: ArrivalResult? = null,
    val error: ArrivalInputError? = null,
    val busy: Boolean = false,
    val fixture: JourneyFixture? = null,
)

@HiltViewModel
class ArrivalViewModel @Inject constructor(private val query: QueryJourneyArrival, defaultZone: ZoneId) : ViewModel() {
    private val mutable = MutableStateFlow(ArrivalUiState(query.tomorrow(defaultZone).toString(), zone = defaultZone.id))
    val uiState = mutable.asStateFlow()
    private var referenceDraft = mutable.value
    private var job: Job? = null
    private var generation = 0L
    private var observedJourney: HabitualJourney? = null

    /** Recomposition/recreation alone must not discard a query held by this ViewModel. */
    fun onJourneyChanged(journey: HabitualJourney?) {
        if (observedJourney != journey) {
            invalidate()
            observedJourney = journey
        }
    }

    /** Cancellation plus generation prevents an older coroutine publishing over newer input. */
    fun invalidate() {
        generation++
        job?.cancel()
        mutable.update { it.copy(result = null, error = null, busy = false, selectedOffset = null, offsets = emptyList()) }
    }
    private fun edit(change: (ArrivalUiState) -> ArrivalUiState) {
        if (mutable.value.fixture != null) return
        invalidate()
        mutable.update(change)
    }
    fun updateDate(value: String) = edit { it.copy(date = value) }
    fun updateTime(value: String) = edit { it.copy(time = value) }
    fun updateZone(value: String) = edit { it.copy(zone = value) }
    fun tomorrow() {
        if (mutable.value.fixture != null) return
        val zone = try { ZoneId.of(mutable.value.zone) } catch (_: DateTimeException) {
            invalidate(); mutable.update { it.copy(error = ArrivalInputError.ZONE) }; return
        }
        edit { it.copy(date = query.tomorrow(zone).toString()) }
    }
    fun selectScenario(value: ArrivalScenario) {
        if (mutable.value.scenario == value) return
        if (mutable.value.scenario == ArrivalScenario.REFERENCE) referenceDraft = mutable.value
        invalidate()
        val demo = query.fixture(value)
        mutable.value = if (value == ArrivalScenario.REFERENCE) referenceDraft.copy(result = null, error = null,
            busy = false, offsets = emptyList(), selectedOffset = null) else {
            requireNotNull(demo)
            ArrivalUiState(demo.departure.local.toLocalDate().toString(), demo.departure.local.toLocalTime().toString(),
                demo.departure.zone.id, scenario = value, fixture = demo)
        }
    }
    fun chooseOffset(value: ZoneOffset) {
        if (value !in mutable.value.offsets) return
        generation++; job?.cancel()
        mutable.update { it.copy(selectedOffset = value, result = null, error = null, busy = false) }
    }
    fun consult() {
        val state = mutable.value
        if (state.busy) return
        val date = try {
            require(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(state.date))
            LocalDate.parse(state.date)
        } catch (_: IllegalArgumentException) { mutable.update { it.copy(error = ArrivalInputError.DATE) }; return
        } catch (_: DateTimeException) { mutable.update { it.copy(error = ArrivalInputError.DATE) }; return }
        val time = try {
            require(Regex("[0-9]{2}:[0-9]{2}").matches(state.time))
            LocalTime.parse(state.time)
        } catch (_: IllegalArgumentException) { mutable.update { it.copy(error = ArrivalInputError.TIME) }; return
        } catch (_: DateTimeException) { mutable.update { it.copy(error = ArrivalInputError.TIME) }; return }
        val zone = try { ZoneId.of(state.zone) } catch (_: DateTimeException) {
            mutable.update { it.copy(error = ArrivalInputError.ZONE) }; return
        }
        val token = ++generation
        mutable.update { it.copy(busy = true, result = null, error = null) }
        job = viewModelScope.launch {
            try {
                val result = query(DepartureInput(LocalDateTime.of(date, time), zone, state.selectedOffset), state.scenario)
                if (generation == token) mutable.update { it.copy(result = result,
                    offsets = if (result is ArrivalResult.Ambiguous) result.offsets else it.offsets) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (generation == token) mutable.update { it.copy(error = ArrivalInputError.UNAVAILABLE) }
            } finally {
                if (generation == token) mutable.update { it.copy(busy = false) }
            }
        }
    }
}
