package com.cercafunctions.appfunctions
import androidx.annotation.RequiresApi
import androidx.appfunctions.*
import androidx.appfunctions.metadata.AppFunctionMetadata
import com.cercafunctions.domain.estimate.*
import com.cercafunctions.domain.rail.RailSelection
import java.time.ZoneOffset
import java.time.DateTimeException
import kotlinx.coroutines.*
import com.cercafunctions.domain.rail.QueryRailContext
import com.cercafunctions.domain.sharing.JourneySharingRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.time.LocalDateTime
import java.time.ZoneId

@RequiresApi(36)
@AndroidEntryPoint
@AppFunctionServiceEntryPoint(serviceName="JourneyAppFunctionService",appFunctionXmlFileName="journey_app_function_service")
abstract class BaseJourneyAppFunctionService:AppFunctionService() {
    @Inject internal lateinit var arrivalQuery:QueryJourneyArrival
    @Inject internal lateinit var railQuery:QueryRailContext
    @Inject internal lateinit var sharing:JourneySharingRepository

    /**
     * Queries the habitual journey's station arrival and qualitative railway context for an explicit departure.
     * Reference uses the saved duration and is not an operational forecast. Explicit demos retain their fixed
     * journey, date and clock. Sharing must be authorized in the app; calendar and contact data are excluded.
     * @param departure Local departure date and time. A past reference or nonexistent/ambiguous unresolved time is invalid.
     * @param zone IANA or fixed-offset zone for departure; no implicit device-zone default.
     * @param scenario Null selects REFERENCE. Other exact values: DEMO_NORMAL, DEMO_DELAYED, DEMO_UNKNOWN.
     * @param utcOffset Null unless disambiguating a repeated local time; otherwise an offset such as +01:00 matching that time.
     * @param originStationId Optional explicit Renfe origin catalog ID. Both station IDs must be supplied together and differ.
     * @param destinationStationId Optional explicit destination catalog ID. Null IDs leave context UNRESOLVED; demos skip the feed.
     * @return Arrival evidence and bounded railway summary, including source and observation times. Null arrival/delay are unknown.
     */
    @OptIn(ExperimentalAppFunctionsApi::class)
    @AppFunctionAccessLevel(level=AppFunctionMetadata.ACCESS_LEVEL_ANDROID_TRUSTED,isCompatEnforcementEnabled=true)
    @AppFunctionDeclaration(isDescribedByKDoc=true,isEnabled=false)
    suspend fun queryJourney(departure:LocalDateTime,zone:ZoneId,scenario:String?=null,utcOffset:String?=null,
        originStationId:String?=null,destinationStationId:String?=null):JourneyResponse=withContext(Dispatchers.IO) {
        try {
            if(!sharing.isAllowed())throw AppFunctionDeniedException("Journey sharing is not authorized in the app.")
            val mode=try{if(scenario==null)ArrivalScenario.REFERENCE else ArrivalScenario.valueOf(scenario)}
                catch(_:IllegalArgumentException){throw AppFunctionInvalidArgumentException("Unknown journey scenario.")}
            val offset=try{utcOffset?.let(ZoneOffset::of)}
                catch(_:DateTimeException){throw AppFunctionInvalidArgumentException("Invalid UTC offset.")}
            if((originStationId==null)!=(destinationStationId==null))
                throw AppFunctionInvalidArgumentException("Provide both station IDs or neither.")
            val selection=if(originStationId==null)null else {
                val ids=railQuery.stations().map{it.id}.toSet()
                if(originStationId==destinationStationId || originStationId !in ids || destinationStationId !in ids)
                    throw AppFunctionInvalidArgumentException("Station IDs must be distinct entries from the catalog.")
                RailSelection(originStationId,requireNotNull(destinationStationId))
            }
            val result=arrivalQuery(DepartureInput(departure,zone,offset),mode)
            val value=when(result) {
                is ArrivalResult.Estimated -> result.value
                ArrivalResult.MissingJourney -> throw AppFunctionElementNotFoundException("Configure the habitual journey in the app.")
                is ArrivalResult.StorageUnavailable -> throw AppFunctionAppUnknownException("Journey configuration is unavailable.")
                ArrivalResult.Gap -> throw AppFunctionInvalidArgumentException("Departure does not exist in the selected zone.")
                is ArrivalResult.Ambiguous -> throw AppFunctionInvalidArgumentException("Departure is repeated; provide a matching UTC offset.")
                ArrivalResult.InvalidOffset -> throw AppFunctionInvalidArgumentException("UTC offset does not match departure.")
                ArrivalResult.PastDeparture -> throw AppFunctionInvalidArgumentException("Reference departure is in the past.")
            }
            val context=railQuery(value,selection)
            currentCoroutineContext().ensureActive()
            if(!sharing.isAllowed())throw AppFunctionDeniedException("Journey sharing is not authorized in the app.")
            JourneyResponse(value.journey.origin,value.journey.destination,value.journey.estimatedDurationMinutes,
                value.departure,value.arrival,value.zone,value.source.name,value.scenario.name,value.observedAt,value.delayMinutes,
                RailSummary(context.status.name,context.consultedAt,context.sourceUrl,context.publishedAt,
                    context.alerts.size,context.unresolvedCount,"ENDPOINTS_ONLY",context.failure?.name))
        } catch(cancelled:CancellationException){throw cancelled
        } catch(known:AppFunctionException){throw known
        } catch(_:Exception){throw AppFunctionAppUnknownException("Journey query is temporarily unavailable.")}
    }
}
