package com.cercafunctions.platform.authorization
import android.content.ContentResolver
import android.os.SystemClock
import android.provider.Settings
import com.cercafunctions.domain.authorization.*
import java.time.Clock
/** Read on IO by grant consumers; unknown boot counter fails closed in domain policy. */
class AndroidGrantClock(private val resolver:ContentResolver,private val wall:Clock):GrantClock {
    override fun now()=GrantTime(wall.instant(),SystemClock.elapsedRealtime(),
        Settings.Global.getInt(resolver,Settings.Global.BOOT_COUNT,-1))
}
