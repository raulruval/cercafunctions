package com.cercafunctions.appfunctions
import android.app.appfunctions.AppFunctionManager
import android.app.appfunctions.ExecuteAppFunctionRequest
import android.app.appfunctions.ExecuteAppFunctionResponse
import android.app.appfunctions.AppFunctionException
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.Assert.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Real foreign UID without shell adoption; install app APK before this library testhost. */
@SdkSuppress(minSdkVersion=36)
class UnauthorizedCallerTest {
    @Test fun foreignUidWithoutExecutePermissionCannotInvokeProvider() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val targetUid=context.packageManager.getApplicationInfo("com.cercafunctions.app",0).uid
        assertNotEquals(targetUid,android.os.Process.myUid())
        assertNotEquals(android.content.pm.PackageManager.PERMISSION_GRANTED,
            context.checkSelfPermission("android.permission.EXECUTE_APP_FUNCTIONS"))
        val manager=requireNotNull(context.getSystemService(AppFunctionManager::class.java))
        val latch=CountDownLatch(1)
        var failure:AppFunctionException?=null
        var success=false
        manager.executeAppFunction(ExecuteAppFunctionRequest.Builder("com.cercafunctions.app",
            JourneyAppFunctionService.FUNCTION_ID_QUERY_JOURNEY).build(),Runnable::run,CancellationSignal(),
            object:OutcomeReceiver<ExecuteAppFunctionResponse,AppFunctionException> {
                override fun onResult(result:ExecuteAppFunctionResponse){success=true;latch.countDown()}
                override fun onError(error:AppFunctionException){failure=error;latch.countDown()}
            })
        assertTrue("System callback",latch.await(15,TimeUnit.SECONDS))
        assertFalse("No journey response",success)
        assertEquals(AppFunctionException.ERROR_DENIED,requireNotNull(failure).errorCode)
    }
}
