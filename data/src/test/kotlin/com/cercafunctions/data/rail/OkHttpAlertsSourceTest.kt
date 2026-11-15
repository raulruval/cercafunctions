package com.cercafunctions.data.rail

import com.cercafunctions.domain.rail.RailFailure
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.net.InetSocketAddress
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test

class OkHttpAlertsSourceTest {
    @Test fun `http success and errors close response bodies`()=runBlocking {
        val server=HttpServer.create(InetSocketAddress("127.0.0.1",0),0)
        server.createContext("/ok") { e -> val data="{\"header\":{}}".toByteArray(); e.sendResponseHeaders(200,data.size.toLong());e.responseBody.use{it.write(data)} }
        server.createContext("/fail") { e -> e.sendResponseHeaders(503,2);e.responseBody.use{it.write(byteArrayOf(78,111))} }
        server.createContext("/large") { e -> e.sendResponseHeaders(200,1_048_577);try{e.responseBody.use{it.write(ByteArray(1_048_577))}}catch(_:java.io.IOException){} }
        server.createContext("/chunked") { e -> e.sendResponseHeaders(200,0);try{e.responseBody.use{it.write(ByteArray(1_048_577))}}catch(_:java.io.IOException){} }
        server.createContext("/redirect") { e -> e.responseHeaders.add("Location","/ok");e.sendResponseHeaders(302,-1);e.close() }
        server.start()
        val client=OkHttpClient.Builder().followRedirects(false).callTimeout(2,TimeUnit.SECONDS).build()
        fun source(path:String)=OkHttpAlertsSource("http://127.0.0.1:${server.address.port}$path".toHttpUrl(),client)
        try {
            assertEquals("{\"header\":{}}",source("/ok").fetch())
            for((path,reason) in listOf("/fail" to RailFailure.HTTP,"/large" to RailFailure.TOO_LARGE,"/chunked" to RailFailure.TOO_LARGE,"/redirect" to RailFailure.HTTP)) {
                try{source(path).fetch();fail("expected failure")}catch(e:RailSourceException){assertEquals(reason,e.reason)}
            }
        } finally { server.stop(0);client.connectionPool.evictAll();client.dispatcher.executorService.shutdown() }
    }
    @Test fun `call timeout bounds an unresponsive source`()=runBlocking {
        val release=CountDownLatch(1)
        val server=HttpServer.create(InetSocketAddress("127.0.0.1",0),0)
        server.createContext("/") { e -> release.await(2,TimeUnit.SECONDS);e.close() }
        server.start()
        val client=OkHttpClient.Builder().callTimeout(150,TimeUnit.MILLISECONDS).retryOnConnectionFailure(false).build()
        try {
            val source=OkHttpAlertsSource("http://127.0.0.1:${server.address.port}/".toHttpUrl(),client)
            try {source.fetch();fail("expected timeout")}catch(_:java.io.IOException){}
        } finally {release.countDown();server.stop(0);client.connectionPool.evictAll();client.dispatcher.executorService.shutdown()}
    }
    @Test fun `cancel coroutine cancels underlying call`()=runBlocking {
        val received=CompletableDeferred<Unit>();val cancelled=CompletableDeferred<Unit>();val release=CountDownLatch(1)
        val server=HttpServer.create(InetSocketAddress("127.0.0.1",0),0)
        server.createContext("/") { e -> received.complete(Unit);release.await(5,TimeUnit.SECONDS);e.close() }
        server.start()
        val client=OkHttpClient.Builder().eventListener(object:EventListener(){override fun canceled(call:Call){cancelled.complete(Unit)}}).build()
        try {
            val source=OkHttpAlertsSource("http://127.0.0.1:${server.address.port}/".toHttpUrl(),client)
            val job=launch { source.fetch() }
            withTimeout(5000){received.await()}
            job.cancelAndJoin()
            withTimeout(5000){cancelled.await()}
            assertTrue(job.isCancelled)
        } finally {release.countDown();server.stop(0);client.connectionPool.evictAll();client.dispatcher.executorService.shutdown()}
    }
}
