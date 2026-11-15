package com.cercafunctions.domain.sharing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
/** Explicit permission to disclose the habitual journey, never calendar data. */
interface JourneySharingRepository {
    val available:Boolean
    val allowed:Flow<Boolean>
    suspend fun isAllowed():Boolean = allowed.first()
    suspend fun setAllowed(value:Boolean)
}
