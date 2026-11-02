package com.cercafunctions.app.di

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.platform.calendar.AndroidCalendarRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CalendarModule {
    @Provides @Singleton
    fun provideCalendarRepository(@ApplicationContext context:Context):CalendarRepository =
        AndroidCalendarRepository(context.contentResolver,{context.checkSelfPermission(Manifest.permission.READ_CALENDAR)==PackageManager.PERMISSION_GRANTED})
    @Provides fun provideGetCalendarDay(repository:CalendarRepository)=GetCalendarDay(repository)
    @Provides fun provideRevalidateCalendarSelection(get:GetCalendarDay)=RevalidateCalendarSelection(get)
}
