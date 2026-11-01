package com.cercafunctions.domain.journey

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class GetHabitualJourneyTest {
    @Test
    fun `unconfigured journey remains absent`() = runTest {
        val repository = object : JourneyRepository {
            override suspend fun getHabitualJourney(): HabitualJourney? = null
        }
        assertNull(GetHabitualJourney(repository)())
    }

    @Test
    fun `configured journey preserves the user supplied baseline`() = runTest {
        val journey = HabitualJourney("Origen de prueba", "Destino de prueba", 35)
        val repository = object : JourneyRepository {
            override suspend fun getHabitualJourney() = journey
        }
        assertEquals(journey, GetHabitualJourney(repository)())
    }

    @Test
    fun `cancellation propagates to the caller`() = runTest {
        val cancellation = CancellationException("Caller cancelled")
        val repository = object : JourneyRepository {
            override suspend fun getHabitualJourney(): HabitualJourney = throw cancellation
        }
        try {
            GetHabitualJourney(repository)()
            throw AssertionError("Cancellation must propagate")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duration cannot be zero`() { HabitualJourney("A", "B", 0) }

    @Test(expected = IllegalArgumentException::class)
    fun `duration cannot be negative`() { HabitualJourney("A", "B", -1) }

    @Test(expected = IllegalArgumentException::class)
    fun `origin cannot be blank`() { HabitualJourney(" ", "B", 35) }

    @Test(expected = IllegalArgumentException::class)
    fun `destination cannot be blank`() { HabitualJourney("A", " ", 35) }
}
