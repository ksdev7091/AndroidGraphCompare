package io.github.winschneid.graphcompare.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.winschneid.graphcompare.core.BarMode

/** 棒グラフの「グループ / 積み上げ」を切り替えるボタン。 */
@Composable
fun BarModeSelector(mode: BarMode, onModeChange: (BarMode) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(
        modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
    ) {
        BarMode.entries.forEachIndexed { index, entry ->
            SegmentedButton(
                selected = mode == entry,
                onClick = { onModeChange(entry) },
                shape = SegmentedButtonDefaults.itemShape(index, BarMode.entries.size),
                modifier = Modifier.testTag("bar_mode_${entry.name.lowercase()}"),
            ) {
                Text(entry.label, maxLines = 1)
            }
        }
    }
}
