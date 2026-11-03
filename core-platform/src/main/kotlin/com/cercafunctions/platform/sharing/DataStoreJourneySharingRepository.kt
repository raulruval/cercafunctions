package com.cercafunctions.platform.sharing
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.cercafunctions.domain.sharing.JourneySharingRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
/** Ordering keeps the disclosure gate closed if a system operation fails or is cancelled. */
class DataStoreJourneySharingRepository(private val store:DataStore<Preferences>,override val available:Boolean,
    private val configureSystem:suspend(Boolean)->Unit):JourneySharingRepository {
    private val key=booleanPreferencesKey("journey_sharing_allowed")
    private val mutex=Mutex()
    override val allowed=store.data.map{it[key] ?: false}
    override suspend fun setAllowed(value:Boolean):Unit=mutex.withLock {
        if(value) {
            if(!available)throw UnsupportedOperationException("Journey sharing unavailable")
            configureSystem(true)
            store.edit{it[key]=true}
        } else {
            store.edit{it[key]=false}
            if(available)configureSystem(false)
        }
        Unit
    }
}
