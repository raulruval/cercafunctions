package com.cercafunctions.data.rail

import com.cercafunctions.domain.rail.*

/** IDs/names only, extracted from GTFS. Does not establish any trip or service. */
class RenfeStationCatalog : RailStationCatalog {
    private val entries = requireNotNull(javaClass.getResourceAsStream("/renfe-stations.tsv"))
        .bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.filter { !it.startsWith("#") && it.isNotBlank() }.map { line ->
                val fields=line.split('\t',limit=2)
                RailStation(fields[0],fields[1])
            }.toList().sortedBy { it.name }
        }
    override fun stations(): List<RailStation> = entries
}
