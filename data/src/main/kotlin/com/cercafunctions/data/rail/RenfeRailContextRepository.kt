package com.cercafunctions.data.rail

import com.cercafunctions.domain.rail.*
import kotlinx.coroutines.*
import java.io.IOException

fun interface RenfeAlertsSource { suspend fun fetch(): String }
class RailSourceException(val reason: RailFailure) : IOException(reason.name)

class RenfeRailContextRepository(
    private val source: RenfeAlertsSource = OkHttpAlertsSource(),
) : RailContextRepository {
    override val sourceUrl: String = "https://gtfsrt.renfe.com/alerts.json"
    override suspend fun read(): RailFeedRead = withContext(Dispatchers.IO) {
        try {
            val json=source.fetch()
            ensureActive()
            RailFeedRead.Success(GtfsAlertsParser().parse(json))
        } catch(cancelled: CancellationException) { throw cancelled
        } catch(failure: RailSourceException) { RailFeedRead.Failure(failure.reason)
        } catch(_: IOException) { RailFeedRead.Failure(RailFailure.NETWORK)
        } catch(failure: RailFeedFormatException) { RailFeedRead.Failure(RailFailure.INVALID_FORMAT,failure.publishedAt)
        } catch(_: IllegalArgumentException) { RailFeedRead.Failure(RailFailure.INVALID_FORMAT)
        } catch(_: com.google.gson.JsonParseException) { RailFeedRead.Failure(RailFailure.INVALID_FORMAT) }
    }
}
