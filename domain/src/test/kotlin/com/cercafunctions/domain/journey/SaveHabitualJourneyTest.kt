package com.cercafunctions.domain.journey

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SaveHabitualJourneyTest {
    @Test fun `trim preserves accents and case before saving`() = runTest {
        val repository = SaveRepository()
        val expected = HabitualJourney("Alcalá", "Chamartín", 35)
        assertEquals(JourneySaveResult.Saved(expected), SaveHabitualJourney(repository)("  Alcalá ", " Chamartín  ", 35))
        assertEquals(expected, repository.saved)
        assertEquals(1, repository.calls)
    }

    @Test fun `blank origin never reaches storage`() = runTest {
        val repository = SaveRepository()
        assertEquals(JourneySaveResult.InvalidInput(setOf(JourneyField.ORIGIN)), SaveHabitualJourney(repository)(" ", "B", 35))
        assertEquals(0, repository.calls)
    }

    @Test fun `blank destination never reaches storage`() = runTest {
        val repository = SaveRepository()
        assertEquals(JourneySaveResult.InvalidInput(setOf(JourneyField.DESTINATION)), SaveHabitualJourney(repository)("A", " ", 35))
        assertEquals(0, repository.calls)
    }

    @Test fun `same names after normalization mark both fields`() = runTest {
        val repository = SaveRepository()
        assertEquals(JourneySaveResult.InvalidInput(setOf(JourneyField.ORIGIN, JourneyField.DESTINATION)), SaveHabitualJourney(repository)(" Alcalá ", "ALCALÁ", 35))
        assertEquals(0, repository.calls)
    }

    @Test fun `duration boundaries are enforced`() = runTest {
        for (duration in listOf(Int.MIN_VALUE, 0, 1441, Int.MAX_VALUE)) {
            val repository = SaveRepository()
            assertEquals(JourneySaveResult.InvalidInput(setOf(JourneyField.DURATION)), SaveHabitualJourney(repository)("A", "B", duration))
            assertEquals(0, repository.calls)
        }
        for (duration in listOf(1, 1440)) {
            assertTrue(SaveHabitualJourney(SaveRepository())("A", "B", duration) is JourneySaveResult.Saved)
        }
    }

    @Test fun `write failure is not reported as success`() = runTest {
        val result = JourneySaveResult.Unavailable(JourneyStorageFailure.WRITE_FAILED)
        val repository = SaveRepository(result = result)
        assertEquals(result, SaveHabitualJourney(repository)("A", "B", 35))
        assertEquals(1, repository.calls)
    }

    @Test fun `save cancellation propagates`() = runTest {
        val cancelled = CancellationException("cancelled")
        val repository = SaveRepository(cancellation = cancelled)
        try { SaveHabitualJourney(repository)("A", "B", 35); fail("Cancellation must propagate") }
        catch (actual: CancellationException) { assertSame(cancelled, actual) }
    }

    @Test fun `invalid model cannot be constructed`() {
        for (create in listOf<() -> HabitualJourney>(
            { HabitualJourney("A", "a", 35) }, { HabitualJourney(" A", "B", 35) },
            { HabitualJourney("A", "B", 1441) }, { HabitualJourney("A", "B", 0) },
            { HabitualJourney(" ", "B", 35) }, { HabitualJourney("A", " ", 35) },
        )) {
            try { create(); fail("Invalid model accepted") } catch (_: IllegalArgumentException) { }
        }
    }
}

private class SaveRepository(
    val result: JourneySaveResult? = null,
    val cancellation: CancellationException? = null,
) : JourneyRepository {
    var calls = 0
    var saved: HabitualJourney? = null
    override suspend fun getHabitualJourney() = JourneyReadResult.NotConfigured
    override suspend fun saveHabitualJourney(journey: HabitualJourney): JourneySaveResult {
        calls++
        cancellation?.let { throw it }
        if (result != null) return result
        saved = journey
        return JourneySaveResult.Saved(journey)
    }
}
