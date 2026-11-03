package com.cercafunctions.app.di
import android.content.Context
import android.os.Build
import androidx.appfunctions.AppFunctionManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.cercafunctions.appfunctions.JourneyAppFunctionService
import com.cercafunctions.domain.sharing.JourneySharingRepository
import com.cercafunctions.platform.sharing.DataStoreJourneySharingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object SharingModule {
    @Provides @Singleton
    fun provideSharing(@ApplicationContext context:Context,store:DataStore<Preferences>):JourneySharingRepository {
        val manager=if(Build.VERSION.SDK_INT>=36 && context.getSystemService(android.app.appfunctions.AppFunctionManager::class.java)!=null)
            AppFunctionManager.getInstance(context) else null
        return DataStoreJourneySharingRepository(store,manager!=null){allowed->
            if(Build.VERSION.SDK_INT>=36)requireNotNull(manager).setAppFunctionEnabled(
                JourneyAppFunctionService.FUNCTION_ID_QUERY_JOURNEY,
                if(allowed)AppFunctionManager.APP_FUNCTION_STATE_ENABLED else AppFunctionManager.APP_FUNCTION_STATE_DISABLED)
        }
    }
}
