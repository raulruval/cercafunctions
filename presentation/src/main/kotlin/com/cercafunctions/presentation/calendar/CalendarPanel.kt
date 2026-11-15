package com.cercafunctions.presentation.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cercafunctions.domain.calendar.*
import com.cercafunctions.presentation.R
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal data class CalendarActions(val onDate:(String)->Unit={},val onZone:(String)->Unit={},val onTomorrow:()->Unit={},
    val onConsult:()->Unit={},val onSettings:()->Unit={},val onSelect:(OccurrenceKey)->Unit={},val onRevalidate:()->Unit={})

@Composable
internal fun CalendarPanel(state:CalendarUiState,actions:CalendarActions=CalendarActions()) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.calendar_heading),style=MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.calendar_privacy),style=MaterialTheme.typography.bodySmall,modifier=Modifier.testTag("calendar_privacy"))
            OutlinedTextField(state.date,actions.onDate,label={Text(stringResource(R.string.calendar_date))},singleLine=true,
                isError=state.inputError==CalendarInputError.DATE,modifier=Modifier.fillMaxWidth().testTag("calendar_date"))
            OutlinedTextField(state.zone,actions.onZone,label={Text(stringResource(R.string.calendar_zone))},singleLine=true,
                isError=state.inputError==CalendarInputError.ZONE,modifier=Modifier.fillMaxWidth().testTag("calendar_zone"))
            TextButton(actions.onTomorrow,modifier=Modifier.testTag("calendar_tomorrow")){Text(stringResource(R.string.calendar_tomorrow))}
            state.inputError?.let {Text(stringResource(if(it==CalendarInputError.DATE)R.string.calendar_invalid_date else R.string.calendar_invalid_zone),color=MaterialTheme.colorScheme.error)}
            Button(actions.onConsult,enabled=!state.busy,modifier=Modifier.testTag("calendar_consult")){Text(stringResource(R.string.calendar_consult))}
            val status=when(state.status) {
                CalendarStatus.IDLE -> R.string.calendar_idle
                CalendarStatus.ACCESS_REQUIRED -> R.string.calendar_access
                CalendarStatus.DENIED -> R.string.calendar_denied
                CalendarStatus.LOADING -> R.string.calendar_loading
                CalendarStatus.CHANGED -> R.string.calendar_changed
                CalendarStatus.DATA -> R.string.calendar_choose
                CalendarStatus.UNAVAILABLE -> when(state.failure) {
                    CalendarFailure.PROVIDER_UNAVAILABLE -> R.string.calendar_no_provider
                    CalendarFailure.INVALID_DATA -> R.string.calendar_invalid_data
                    else -> R.string.calendar_failed
                }
            }
            Text(stringResource(status),modifier=Modifier.testTag("calendar_status"))
            if(state.status==CalendarStatus.DENIED || state.status==CalendarStatus.ACCESS_REQUIRED) {
                TextButton(actions.onSettings,modifier=Modifier.testTag("calendar_settings")){Text(stringResource(R.string.calendar_settings))}
            }
            if(state.status==CalendarStatus.DATA) {
                if(state.items.isEmpty())Text(stringResource(R.string.calendar_empty),modifier=Modifier.testTag("calendar_empty"))
                else for((index,item) in state.items.withIndex()) key(item.key) {
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            OccurrenceDetails(item,ZoneId.of(state.zone))
                            OutlinedButton(onClick={actions.onSelect(item.key)},enabled=!state.busy,
                                modifier=Modifier.testTag("calendar_choose_${item.key.eventId}_${item.key.begin.toEpochMilli()}")) {
                                Text(stringResource(R.string.calendar_choose_number,index+1))
                            }
                        }
                    }
                }
            }
            val selected=state.selected
            val selection=state.selection
            if(selected!=null && selection!=null) {
                HorizontalDivider()
                Text(stringResource(R.string.calendar_selected),style=MaterialTheme.typography.titleMedium,modifier=Modifier.testTag("calendar_selected"))
                OccurrenceDetails(selected,selection.day.zone,"calendar_selected_time")
                Text(stringResource(if(state.validated)R.string.calendar_validated else R.string.calendar_not_validated),modifier=Modifier.testTag("calendar_validation"))
                Button(actions.onRevalidate,enabled=!state.busy,modifier=Modifier.testTag("calendar_revalidate")){Text(stringResource(R.string.calendar_revalidate))}
            }
        }
    }
}

@Composable
private fun OccurrenceDetails(item:CalendarOccurrence,zone:ZoneId,tag:String="calendar_item_time") {
    val format=DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm xxx").withZone(zone)
    Text(item.title.ifBlank{stringResource(R.string.calendar_no_title)},style=MaterialTheme.typography.titleMedium)
    Text(item.calendarName.ifBlank{stringResource(R.string.calendar_no_name)},style=MaterialTheme.typography.bodySmall)
    Text(stringResource(R.string.calendar_time,format.format(item.key.begin),format.format(item.end),zone.id),modifier=Modifier.testTag(tag))
}
