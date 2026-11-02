package com.cercafunctions.data.rail

import com.cercafunctions.domain.rail.RailFailure
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Production endpoint is fixed. Internal constructor exists for loopback contract tests. */
class OkHttpAlertsSource internal constructor(private val endpoint:HttpUrl, private val client:OkHttpClient): RenfeAlertsSource {
    constructor():this("https://gtfsrt.renfe.com/alerts.json".toHttpUrl(),OkHttpClient.Builder()
        .connectTimeout(5,TimeUnit.SECONDS).readTimeout(10,TimeUnit.SECONDS).callTimeout(15,TimeUnit.SECONDS)
        .followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false).build())

    override suspend fun fetch():String = suspendCancellableCoroutine { continuation ->
        val call=client.newCall(Request.Builder().url(endpoint).header("Accept","application/json").build())
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object:Callback {
            override fun onFailure(call:Call,e:IOException) { continuation.resumeWithException(e) }
            override fun onResponse(call:Call,response:Response) {
                try {
                    val text=response.use { reply ->
                        if(!reply.isSuccessful) throw RailSourceException(RailFailure.HTTP)
                        val body=reply.body ?: throw RailSourceException(RailFailure.INVALID_FORMAT)
                        if(body.contentLength()>MAX_BYTES) throw RailSourceException(RailFailure.TOO_LARGE)
                        val buffer=ByteArray(8192)
                        val output=ByteArrayOutputStream()
                        body.byteStream().use { input ->
                            while(true) {
                                val count=input.read(buffer)
                                if(count<0)break
                                if(output.size()+count>MAX_BYTES) throw RailSourceException(RailFailure.TOO_LARGE)
                                output.write(buffer,0,count)
                            }
                        }
                        output.toByteArray().toString(Charsets.UTF_8)
                    }
                    continuation.resume(text)
                } catch(e:Exception) { continuation.resumeWithException(e) }
            }
        })
    }
    private companion object { const val MAX_BYTES=1_048_576 }
}
