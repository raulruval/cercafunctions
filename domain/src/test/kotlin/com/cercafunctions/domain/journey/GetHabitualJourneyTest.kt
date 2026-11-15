package com.cercafunctions.domain.journey

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

class GetHabitualJourneyTest {
    @Test fun `absence remains distinct from read failure`() = runTest {
        val repository = ReadRepository(JourneyReadResult.NotConfigured)
        assertEquals(JourneyReadResult.NotConfigured, GetHabitualJourney(repository)())
        repository.result = JourneyReadResult.Unavailable(JourneyStorageFailure.READ_FAILED)
        assertEquals(repository.result, GetHabitualJourney(repository)())
    }

    @Test fun `configured journey is preserved`() = runTest {
        val result = JourneyReadResult.Configured(HabitualJourney("A", "B", 35))
        assertEquals(result, GetHabitualJourney(ReadRepository(result))())
    }

    @Test fun `read cancellation propagates`() = runTest {
        val cancelled = CancellationException("cancelled")
        val repository = object : JourneyRepository {
            override suspend fun getHabitualJourney(): JourneyReadResult = throw cancelled
            override suspend fun saveHabitualJourney(journey: HabitualJourney): JourneySaveResult = error("unused")
        }
        try { GetHabitualJourney(repository)(); fail("Cancellation must propagate") }
        catch (actual: CancellationException) { assertSame(cancelled, actual) }
    }
}

private class ReadRepository(var result: JourneyReadResult) : JourneyRepository {
    override suspend fun getHabitualJourney() = result
    override suspend fun saveHabitualJourney(journey: HabitualJourney): JourneySaveResult = error("unused")
}
