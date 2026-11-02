package com.cercafunctions.data.rail

import com.cercafunctions.domain.rail.*
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class RenfeRailRepositoryTest {
    private fun feed(body:String="",header:String="""{"gtfsRealtimeVersion":"2.0","timestamp":"1793602800"}""") = """{"header":$header,"entity":[$body]}"""
    private val entity="""{"id":"one","alert":{"activePeriod":[{"start":"1793602800"}],"informedEntity":[{"stopId":"70100"}],"descriptionText":{"translation":[{"language":"en","text":"English"},{"language":"es-ES","text":"Ascensor sin servicio"}]}}}"""
    @Test fun `parse camel case numeric strings optional fields and spanish translation`() {
        val f=GtfsAlertsParser().parse(feed(entity))
        assertEquals(Instant.ofEpochSecond(1793602800),f.publishedAt)
        val a=f.alerts.single()
        assertEquals("Ascensor sin servicio",a.text)
        assertEquals("70100",a.selectors.single().stopId)
        assertFalse(a.selectors.single().hasOtherConstraints)
        assertNull(a.publication.single().end); assertTrue(a.impact.isEmpty())
    }
    @Test fun `snake case and numeric epoch preserve values`() {
        val json=feed(entity).replace("gtfsRealtimeVersion","gtfs_realtime_version").replace("activePeriod","active_period").replace("informedEntity","informed_entity").replace("stopId","stop_id").replace("descriptionText","description_text").replace("\"1793602800\"","1793602800")
        assertEquals(GtfsAlertsParser().parse(feed(entity)),GtfsAlertsParser().parse(json))
    }
    @Test fun `missing timestamp is retained as absent`() {
        assertNull(GtfsAlertsParser().parse(feed(header="""{"gtfsRealtimeVersion":"2.0"}""")).publishedAt)
    }
    @Test fun `other selector constraints never disappear including route type zero`() {
        val f=GtfsAlertsParser().parse(feed(entity.replace("\"stopId\":\"70100\"","\"stopId\":\"70100\",\"routeType\":0")))
        assertTrue(f.alerts.single().selectors.single().hasOtherConstraints)
    }
    @Test fun `impact differs from publication and supports open ranges`() {
        val a=GtfsAlertsParser().parse(feed(entity.replace("activePeriod","impactPeriod"))).alerts.single()
        assertTrue(a.publication.isEmpty()); assertEquals(1,a.impact.size)
    }
    @Test fun `incomplete or malformed content fails whole feed`() {
        val malformed=listOf("{}","[]",feed(entity).dropLast(1),feed(entity).replace("70100",""),
            feed(entity).replace("1793602800","-1"),feed(entity).replace("1793602800","1.5"),
            feed(entity).replace("1793602800","9999999999999999999999999"),
            feed(entity).replace("\"start\":\"1793602800\"","\"end\":\"0\",\"start\":\"1793602800\""),
            feed(entity).replace("{\"start\":\"1793602800\"}","{}"),
            feed(entity).replace("\"informedEntity\":[{\"stopId\":\"70100\"}]","\"informedEntity\":[]"),
            feed(entity, """{"gtfsRealtimeVersion":"2.0","incrementality":"DIFFERENTIAL"}"""),feed(entity+","+entity))
        for(json in malformed) {
            try { GtfsAlertsParser().parse(json); fail("Accepted malformed payload: $json") } catch(_:IllegalArgumentException) {} catch(_:com.google.gson.JsonParseException) {}
        }
    }
    @Test fun `entity limit is enforced without silently dropping alerts`() {
        val rows=(0..2000).joinToString(",") { entity.replace("\"one\"","\"$it\"") }
        try {GtfsAlertsParser().parse(feed(rows));fail("too many")} catch(_:IllegalArgumentException) {}
    }
    @Test fun `catalog ids are unique and match known source stations`() {
        val entries=RenfeStationCatalog().stations()
        assertEquals(1147,entries.size);assertEquals(entries.size,entries.map {it.id}.toSet().size)
        assertEquals("Vicálvaro",entries.single {it.id=="70100"}.name)
        assertEquals("Coslada",entries.single {it.id=="70108"}.name)
    }
    @Test fun `repository maps malformed to failure rather than empty`()=runTest {
        val conflicting=feed(entity+","+entity.replace("Ascensor sin servicio","Aviso diferente"))
        assertEquals(RailFeedRead.Failure(RailFailure.INVALID_FORMAT,Instant.ofEpochSecond(1793602800)),
            RenfeRailContextRepository(RenfeAlertsSource {conflicting}).read())
        for(payload in listOf("{}",feed()+" trailing")) {
            assertEquals(RailFeedRead.Failure(RailFailure.INVALID_FORMAT),RenfeRailContextRepository(RenfeAlertsSource {payload}).read())
        }
    }
    @Test fun `repository preserves network and http failures and cancellation`()=runTest {
        for(reason in listOf(RailFailure.HTTP,RailFailure.TOO_LARGE,RailFailure.NETWORK)) {
            assertEquals(RailFeedRead.Failure(reason),RenfeRailContextRepository(RenfeAlertsSource {throw RailSourceException(reason)}).read())
        }
        assertEquals(RailFeedRead.Failure(RailFailure.NETWORK),RenfeRailContextRepository(RenfeAlertsSource {throw java.io.IOException("offline")}).read())
        try {RenfeRailContextRepository(RenfeAlertsSource {throw CancellationException("injected")}).read();fail("cancel")}catch(_:CancellationException){}
    }
}
