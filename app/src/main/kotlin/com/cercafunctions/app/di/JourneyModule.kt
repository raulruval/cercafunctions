package com.cercafunctions.app.di

import com.cercafunctions.data.journey.InMemoryJourneyRepository
import com.cercafunctions.domain.journey.GetHabitualJourney
import com.cercafunctions.domain.journey.JourneyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** The composition root keeps domain and data independent of Hilt. */
@Module
@InstallIn(SingletonComponent::class)
object JourneyModule {
    @Provides
    @Singleton
    fun provideJourneyRepository(): JourneyRepository = InMemoryJourneyRepository()

    @Provides
    fun provideGetHabitualJourney(repository: JourneyRepository): GetHabitualJourney =
        GetHabitualJourney(repository)
}
