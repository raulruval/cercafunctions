package com.cercafunctions.app.di

import com.cercafunctions.data.rail.RenfeRailContextRepository
import com.cercafunctions.data.rail.RenfeStationCatalog
import com.cercafunctions.domain.rail.*
import android.content.Context
import java.time.Clock
import java.time.ZoneId
import com.cercafunctions.domain.estimate.JourneyFixtureRepository
import com.cercafunctions.domain.estimate.QueryJourneyArrival
import com.cercafunctions.data.estimate.ReproducibleJourneyFixtures
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.cercafunctions.domain.journey.GetHabitualJourney
import com.cercafunctions.domain.journey.JourneyRepository
import com.cercafunctions.domain.journey.SaveHabitualJourney
import com.cercafunctions.platform.journey.DataStoreJourneyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** One store per file/process. No Activity or UI state participates in persistence. */
@Module
@InstallIn(SingletonComponent::class)
object JourneyModule {
    @Provides @Singleton fun provideRailRepository(): RailContextRepository = RenfeRailContextRepository()
    @Provides @Singleton fun provideRailCatalog(): RailStationCatalog = RenfeStationCatalog()
    @Provides fun provideRailQuery(repository:RailContextRepository,catalog:RailStationCatalog,clock:Clock) = QueryRailContext(repository,catalog,clock)

    @Provides fun provideClock(): Clock = Clock.systemUTC()
    @Provides fun provideZone(): ZoneId = ZoneId.systemDefault()
    @Provides fun provideFixtures(clock: Clock): JourneyFixtureRepository = ReproducibleJourneyFixtures(clock)
    @Provides fun provideArrival(repository: JourneyRepository, fixtures: JourneyFixtureRepository, clock: Clock) =
        QueryJourneyArrival(repository, fixtures, clock)

    @Provides
    @Singleton
    fun provideJourneyStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("habitual_journey") }

    @Provides
    @Singleton
    fun provideJourneyRepository(store: DataStore<Preferences>): JourneyRepository =
        DataStoreJourneyRepository(store)

    @Provides
    fun provideGetHabitualJourney(repository: JourneyRepository) = GetHabitualJourney(repository)

    @Provides
    fun provideSaveHabitualJourney(repository: JourneyRepository) = SaveHabitualJourney(repository)
}
