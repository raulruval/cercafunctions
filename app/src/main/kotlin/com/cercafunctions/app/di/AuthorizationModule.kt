package com.cercafunctions.app.di
import android.content.Context
import android.os.Build
import androidx.appfunctions.AppFunctionManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.cercafunctions.appfunctions.JourneyAppFunctionService
import com.cercafunctions.domain.authorization.*
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.domain.assessment.AssessArrival
import com.cercafunctions.domain.estimate.QueryJourneyArrival
import com.cercafunctions.platform.authorization.*
import com.cercafunctions.presentation.authorization.AuthorizationIo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import java.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
@Module @InstallIn(SingletonComponent::class)
object AuthorizationModule {
    @Provides @AuthorizationIo fun provideIo():CoroutineDispatcher=Dispatchers.IO
    @Provides fun provideGrantClock(@ApplicationContext context:Context,clock:Clock):GrantClock=AndroidGrantClock(context.contentResolver,clock)
    @Provides @Singleton fun provideGrants(@ApplicationContext context:Context,store:DataStore<Preferences>):GrantRepository {
        val manager=if(Build.VERSION.SDK_INT>=36 && context.getSystemService(android.app.appfunctions.AppFunctionManager::class.java)!=null)
            AppFunctionManager.getInstance(context) else null
        return DataStoreGrantRepository(store,manager!=null){enabled->
            if(Build.VERSION.SDK_INT>=36)requireNotNull(manager).setAppFunctionEnabled(
                JourneyAppFunctionService.FUNCTION_ID_EVALUATE_AUTHORIZED_COMMITMENT,
                if(enabled)AppFunctionManager.APP_FUNCTION_STATE_ENABLED else AppFunctionManager.APP_FUNCTION_STATE_DISABLED)
        }
    }
    @Provides @Singleton fun provideAuthorizations(repo:GrantRepository,validate:RevalidateCalendarSelection,calendar:GetCalendarDay,clock:GrantClock)=
        AssessmentAuthorizations(repo,validate,calendar,clock)
    @Provides fun provideAuthorizedAssessment(grants:AssessmentAuthorizations,query:QueryJourneyArrival,assess:AssessArrival)=AuthorizedAssessment(grants,query,assess)
}
