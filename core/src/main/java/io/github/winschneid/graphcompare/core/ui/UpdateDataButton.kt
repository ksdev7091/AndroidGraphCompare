package io.github.winschneid.graphcompare.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.winschneid.graphcompare.core.UpdateAnimationSpec

/** データ更新アニメーションの画面で、値を差し替えるボタン。ベンチマークは testTag で探してタップする。 */
@Composable
fun UpdateDataButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(bottom = 12.dp)
            .testTag(UpdateAnimationSpec.UPDATE_BUTTON_TAG),
    ) {
        Text("データを更新")
    }
}
