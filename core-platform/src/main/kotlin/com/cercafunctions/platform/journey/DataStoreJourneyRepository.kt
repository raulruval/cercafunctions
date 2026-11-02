package com.cercafunctions.platform.journey

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cercafunctions.domain.journey.HabitualJourney
import com.cercafunctions.domain.journey.JourneyReadResult
import com.cercafunctions.domain.journey.JourneyRepository
import com.cercafunctions.domain.journey.JourneySaveResult
import com.cercafunctions.domain.journey.JourneyStorageFailure
import java.io.IOException
import kotlinx.coroutines.flow.first

/** Android persistence boundary; business rules remain in the domain. */
class DataStoreJourneyRepository(private val store: DataStore<Preferences>) : JourneyRepository {
    override suspend fun getHabitualJourney(): JourneyReadResult = try {
        decode(store.data.first())
    } catch (_: CorruptionException) {
        JourneyReadResult.Unavailable(JourneyStorageFailure.INVALID_STORED_DATA)
    } catch (_: IOException) {
        JourneyReadResult.Unavailable(JourneyStorageFailure.READ_FAILED)
    }

    override suspend fun saveHabitualJourney(journey: HabitualJourney): JourneySaveResult = try {
        // A single transaction prevents mixing fields from different updates.
        store.edit {
            it[JourneyPreferenceKeys.ORIGIN] = journey.origin
            it[JourneyPreferenceKeys.DESTINATION] = journey.destination
            it[JourneyPreferenceKeys.DURATION] = journey.estimatedDurationMinutes
        }
        // edit returns only after the data has been persisted.
        JourneySaveResult.Saved(journey)
    } catch (_: IOException) {
        JourneySaveResult.Unavailable(JourneyStorageFailure.WRITE_FAILED)
    }

    private fun decode(preferences: Preferences): JourneyReadResult = try {
        val origin = preferences[JourneyPreferenceKeys.ORIGIN]
        val destination = preferences[JourneyPreferenceKeys.DESTINATION]
        val duration = preferences[JourneyPreferenceKeys.DURATION]
        when {
            origin == null && destination == null && duration == null -> JourneyReadResult.NotConfigured
            origin == null || destination == null || duration == null -> invalidData()
            else -> JourneyReadResult.Configured(HabitualJourney(origin, destination, duration))
        }
    } catch (_: IllegalArgumentException) {
        invalidData()
    } catch (_: ClassCastException) {
        invalidData()
    }

    private fun invalidData() = JourneyReadResult.Unavailable(JourneyStorageFailure.INVALID_STORED_DATA)
}

internal object JourneyPreferenceKeys {
    val ORIGIN = stringPreferencesKey("origin")
    val DESTINATION = stringPreferencesKey("destination")
    val DURATION = intPreferencesKey("duration_minutes")
}
