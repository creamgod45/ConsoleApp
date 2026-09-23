package cg.creamgod.consoleapp.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker as MaterialDatePicker
import androidx.compose.material3.DatePickerDialog as MaterialDatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker as MaterialDateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker as MaterialTimePicker
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class DateRangeValue(
    val startMillis: Long,
    val endMillis: Long,
)

data class TimeValue(
    val hour: Int,
    val minute: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    visible: Boolean,
    selectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "選擇日期",
    confirmText: String = "確認",
    dismissText: String = "取消",
    yearRange: IntRange = DatePickerDefaults.YearRange,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
) {
    if (!visible) return
    val state = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis,
        yearRange = yearRange,
        selectableDates = selectableDates,
    )

    MaterialDatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedDateMillis != null,
                onClick = {
                    state.selectedDateMillis?.let(onDateSelected)
                    onDismiss()
                },
            ) { Text(confirmText) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissText) } },
    ) {
        MaterialDatePicker(
            state = state,
            modifier = modifier,
            title = { Text(title, modifier = Modifier.padding(start = 24.dp, top = 16.dp)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    visible: Boolean,
    value: DateRangeValue?,
    onValueChange: (DateRangeValue) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "選擇日期範圍",
    confirmText: String = "確認",
    dismissText: String = "取消",
    yearRange: IntRange = DatePickerDefaults.YearRange,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
) {
    if (!visible) return
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = value?.startMillis,
        initialSelectedEndDateMillis = value?.endMillis,
        yearRange = yearRange,
        selectableDates = selectableDates,
    )
    val start = state.selectedStartDateMillis
    val end = state.selectedEndDateMillis

    MaterialDatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = start != null && end != null,
                onClick = {
                    if (start != null && end != null) onValueChange(DateRangeValue(start, end))
                    onDismiss()
                },
            ) { Text(confirmText) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissText) } },
    ) {
        MaterialDateRangePicker(
            state = state,
            modifier = modifier,
            title = { Text(title, modifier = Modifier.padding(start = 24.dp, top = 16.dp)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(
    visible: Boolean,
    value: TimeValue,
    onValueChange: (TimeValue) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "選擇時間",
    is24Hour: Boolean = true,
    confirmText: String = "確認",
    dismissText: String = "取消",
) {
    if (!visible) return
    val state = rememberTimePickerState(
        initialHour = value.hour.coerceIn(0, 23),
        initialMinute = value.minute.coerceIn(0, 59),
        is24Hour = is24Hour,
    )

    Modal(
        visible = true,
        onDismiss = onDismiss,
        title = title,
        footer = {
            TextButton(onClick = onDismiss) { Text(dismissText) }
            TextButton(
                onClick = {
                    onValueChange(TimeValue(state.hour, state.minute))
                    onDismiss()
                },
            ) { Text(confirmText) }
        },
    ) {
        Column(modifier = modifier.padding(vertical = 8.dp)) {
            MaterialTimePicker(state = state)
        }
    }
}
