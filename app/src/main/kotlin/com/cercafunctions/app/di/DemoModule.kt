package com.cercafunctions.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.cercafunctions.domain.demo.*
import com.cercafunctions.domain.calendar.GetCalendarDay
import com.cercafunctions.platform.demo.DataStoreDemoSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.ZoneId
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DemoModule {
    @Provides @Singleton fun settings(store:DataStore<Preferences>):DemoSettingsRepository=DataStoreDemoSettingsRepository(store)
    @Provides fun evaluate(calendar:GetCalendarDay,settings:DemoSettingsRepository,clock:Clock,zone:ZoneId)=
        EvaluateDemoJourney(calendar,settings,clock,zone)
}
