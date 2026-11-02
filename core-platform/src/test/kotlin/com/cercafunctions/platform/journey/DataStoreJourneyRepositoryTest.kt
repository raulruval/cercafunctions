package com.cercafunctions.platform.journey

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.cercafunctions.domain.journey.*
import java.io.File
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreJourneyRepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private val scopes = mutableListOf<CoroutineScope>()
    private val journey = HabitualJourney("Alcalá", "Chamartín", 35)

    private fun store(file: File): Pair<DataStore<Preferences>, CoroutineScope> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scopes += scope
        return PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }) to scope
    }

    @After fun closeStores() = runBlocking { scopes.forEach { it.coroutineContext.job.cancelAndJoin() } }

    @Test fun `empty file is unconfigured`() = runTest {
        val (store) = store(File(temporary.root, "journey.preferences_pb"))
        assertEquals(JourneyReadResult.NotConfigured, DataStoreJourneyRepository(store).getHabitualJourney())
    }

    @Test fun `saved fields survive closing and recreating store`() = runTest {
        val file = File(temporary.root, "journey.preferences_pb")
        val (first, scope) = store(file)
        assertEquals(JourneySaveResult.Saved(journey), DataStoreJourneyRepository(first).saveHabitualJourney(journey))
        scope.coroutineContext.job.cancelAndJoin()
        val (second) = store(file)
        assertEquals(JourneyReadResult.Configured(journey), DataStoreJourneyRepository(second).getHabitualJourney())
    }

    @Test fun `replacement is the whole journey`() = runTest {
        val (store) = store(File(temporary.root, "journey.preferences_pb"))
        val repository = DataStoreJourneyRepository(store)
        repository.saveHabitualJourney(journey)
        val updated = HabitualJourney("C", "D", 40)
        assertEquals(JourneySaveResult.Saved(updated), repository.saveHabitualJourney(updated))
        assertEquals(JourneyReadResult.Configured(updated), repository.getHabitualJourney())
    }

    @Test fun `partial preferences are not first installation`() = runTest {
        val (store) = store(File(temporary.root, "journey.preferences_pb"))
        store.edit { it[JourneyPreferenceKeys.ORIGIN] = "A" }
        assertEquals(JourneyReadResult.Unavailable(JourneyStorageFailure.INVALID_STORED_DATA), DataStoreJourneyRepository(store).getHabitualJourney())
    }

    @Test fun `invalid complete preferences are unavailable`() = runTest {
        val (store) = store(File(temporary.root, "journey.preferences_pb"))
        store.edit {
            it[JourneyPreferenceKeys.ORIGIN] = "A"
            it[JourneyPreferenceKeys.DESTINATION] = "a"
            it[JourneyPreferenceKeys.DURATION] = 1441
        }
        assertEquals(JourneyReadResult.Unavailable(JourneyStorageFailure.INVALID_STORED_DATA), DataStoreJourneyRepository(store).getHabitualJourney())
    }

    @Test fun `corrupt file is not silently replaced`() = runTest {
        val file = File(temporary.root, "journey.preferences_pb")
        file.writeBytes(byteArrayOf(0xFF.toByte(), 0xFF.toByte()))
        val (store) = store(file)
        assertEquals(JourneyReadResult.Unavailable(JourneyStorageFailure.INVALID_STORED_DATA), DataStoreJourneyRepository(store).getHabitualJourney())
    }

    @Test fun `read IO failure is explicit`() = runTest {
        val broken = object : DataStore<Preferences> {
            override val data = flow<Preferences> { throw IOException("injected") }
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences = error("unused")
        }
        assertEquals(JourneyReadResult.Unavailable(JourneyStorageFailure.READ_FAILED), DataStoreJourneyRepository(broken).getHabitualJourney())
    }

    @Test fun `write IO failure does not acknowledge or mutate`() = runTest {
        val original = preferencesOf(JourneyPreferenceKeys.ORIGIN to "A", JourneyPreferenceKeys.DESTINATION to "B", JourneyPreferenceKeys.DURATION to 35)
        val broken = object : DataStore<Preferences> {
            override val data = flowOf(original)
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
                transform(original)
                throw IOException("injected")
            }
        }
        val repository = DataStoreJourneyRepository(broken)
        assertEquals(JourneySaveResult.Unavailable(JourneyStorageFailure.WRITE_FAILED), repository.saveHabitualJourney(journey))
        assertEquals(JourneyReadResult.Configured(HabitualJourney("A", "B", 35)), repository.getHabitualJourney())
    }

    @Test fun `cancellation is not converted into storage failure`() = runTest {
        val cancelled = CancellationException("injected")
        val store = object : DataStore<Preferences> {
            override val data = flow<Preferences> { throw cancelled }
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences = throw cancelled
        }
        val repository = DataStoreJourneyRepository(store)
        try { repository.getHabitualJourney(); fail("Cancellation lost") }
        catch (actual: CancellationException) { assertSame(cancelled, actual) }
        try { repository.saveHabitualJourney(journey); fail("Cancellation lost") }
        catch (actual: CancellationException) { assertSame(cancelled, actual) }
    }

    @Test fun `concurrent writes never mix journey fields`() = runTest {
        val (store) = store(File(temporary.root, "journey.preferences_pb"))
        val repository = DataStoreJourneyRepository(store)
        val other = HabitualJourney("C", "D", 40)
        coroutineScope {
            listOf(async { repository.saveHabitualJourney(journey) }, async { repository.saveHabitualJourney(other) }).awaitAll()
        }
        val actual = repository.getHabitualJourney()
        assertTrue(actual == JourneyReadResult.Configured(journey) || actual == JourneyReadResult.Configured(other))
    }
}
