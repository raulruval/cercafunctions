package com.cercafunctions.data.rail

import com.cercafunctions.domain.rail.*
import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.StringReader
import java.time.Instant

/** Explicit boundary mapping. An invalid record fails the feed instead of implying no alerts. */
class RailFeedFormatException(val publishedAt:Instant?,cause:IllegalArgumentException):IllegalArgumentException("Invalid feed records",cause)

class GtfsAlertsParser {
    fun parse(json: String): RailFeed {
        require(json.toByteArray(Charsets.UTF_8).size <= 1_048_576)
        val reader=JsonReader(StringReader(json)).apply { strictness=Strictness.STRICT }
        val root=try {
            val parsed=JsonParser.parseReader(reader).obj()
            require(reader.peek()==JsonToken.END_DOCUMENT)
            parsed
        } catch(e:java.io.IOException) { throw IllegalArgumentException("Invalid JSON",e) }
        val header=requireNotNull(root["header"]).obj()
        require(header.field("gtfsRealtimeVersion","gtfs_realtime_version")?.text()=="2.0")
        val increment=header.field("incrementality")?.text()
        require(increment==null || increment=="FULL_DATASET" || increment=="0")
        val stamp=header.field("timestamp")?.epoch()
        return try { parseEntities(root,stamp) } catch(e:IllegalArgumentException) { throw RailFeedFormatException(stamp,e) }
    }
    private fun parseEntities(root:JsonObject,stamp:Instant?):RailFeed {
        val entities=root.field("entity").array()
        require(entities.size<=2000)
        val seen=mutableSetOf<String>()
        val alerts=entities.map { value ->
            val entity=value.obj()
            val id=requireNotNull(entity.field("id")).string()
            require(id.isNotBlank() && seen.add(id))
            require(entity.field("isDeleted","is_deleted")?.let { require(it.isJsonPrimitive && it.asJsonPrimitive.isBoolean); it.asBoolean }!=true)
            val alert=requireNotNull(entity.field("alert")).obj()
            val selectors=alert.field("informedEntity","informed_entity").array().map { element ->
                val selector=element.obj()
                val stop=selector.field("stopId","stop_id")?.string()
                require(stop==null || stop.isNotBlank())
                val other=selector.entrySet().any { (key,v) -> key !in setOf("stopId","stop_id") && !v.isJsonNull }
                require(stop!=null || other)
                RailSelector(stop,other)
            }
            require(selectors.isNotEmpty())
            val publication=periods(alert.field("activePeriod","active_period"))+periods(alert.field("communicationPeriod","communication_period"))
            val impact=periods(alert.field("impactPeriod","impact_period"))
            val description=translated(alert.field("descriptionText","description_text"))
            val title=translated(alert.field("headerText","header_text"))
            val text=listOfNotNull(title,description).distinct().joinToString("\n").ifBlank { "Aviso sin texto publicado" }
            RailAlert(id,if(text.length>4000) text.take(4000)+" […]" else text,
                alert.field("effect")?.text(),selectors,publication,impact)
        }
        return RailFeed(stamp,alerts)
    }
    private fun periods(value: JsonElement?): List<AlertPeriod> = value.array().map {
        val range=it.obj()
        AlertPeriod(range.field("start")?.epoch(),range.field("end")?.epoch())
    }
    private fun translated(value: JsonElement?): String? {
        if(value==null) return null
        val translations=value.obj().field("translation").array().map { it.obj() }
        val selected=translations.firstOrNull { it.field("language")?.string()?.lowercase()?.let { lang -> lang=="es" || lang.startsWith("es-") }==true }
            ?: translations.firstOrNull()
        return selected?.field("text")?.string()?.takeIf { it.isNotBlank() }
    }
    private fun JsonElement.obj(): JsonObject { require(isJsonObject);return asJsonObject }
    private fun JsonElement?.array(): List<JsonElement> { if(this==null)return emptyList(); require(isJsonArray);return asJsonArray.toList() }
    private fun JsonElement.text(): String { require(isJsonPrimitive && !asJsonPrimitive.isBoolean);return asString }
    private fun JsonElement.string(): String { require(isJsonPrimitive && asJsonPrimitive.isString);return asString }
    private fun JsonElement.epoch(): Instant {
        val raw=text();require(Regex("[0-9]+").matches(raw))
        return try { Instant.ofEpochSecond(raw.toLong()) } catch (e:Exception) { throw IllegalArgumentException("Invalid epoch",e) }
    }
    private fun JsonObject.field(vararg names: String): JsonElement? {
        val present=names.mapNotNull { get(it)?.takeUnless(JsonElement::isJsonNull) }
        require(present.size<=1)
        return present.singleOrNull()
    }
}
