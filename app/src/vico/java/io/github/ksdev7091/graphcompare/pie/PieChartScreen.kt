package io.github.ksdev7091.graphcompare.pie

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.PieSize
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.PieValueFormatter
import com.patrykandpatrick.vico.compose.pie.data.pieModel
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import io.github.ksdev7091.graphcompare.core.ChartFormat
import io.github.ksdev7091.graphcompare.core.PieChartSpec
import io.github.ksdev7091.graphcompare.core.ui.ChartColors
import io.github.ksdev7091.graphcompare.core.ui.PieSelectionText

private val SliceLabelFormatter = PieValueFormatter { _, _, index ->
    ChartFormat.pieSliceText(PieChartSpec.slices[index], PieChartSpec.total)
}

/**
 * Vico 3.3.1 のドーナツグラフ（PieChart は 3.x で追加）。
 * 中央の文字とタップでの選択は Vico にないので、Compose で重ねて自前で作る。
 * - 中央の文字：PieChartHost の上に Text を重ねる
 * - タップ：pointerInput でタップ位置の角度からスライスを求め、選んだスライスを offsetFromCenter で外へずらす
 */
@Composable
fun PieChartScreen(modifier: Modifier = Modifier) {
    val slices = PieChartSpec.slices
    var selectedIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val modelProducer = remember { PieChartModelProducer() }
    LaunchedEffect(slices) {
        modelProducer.runTransaction { pieModel { series(slices.map { it.value }) } }
    }

    val label = remember {
        PieChart.SliceLabel.Inside(
            TextComponent(
                textStyle = TextStyle(color = Color.White, fontSize = PieChartSpec.LABEL_TEXT_SIZE_SP.sp, textAlign = TextAlign.Center),
                lineCount = 2,
            ),
        )
    }
    val sliceProvider = remember(selectedIndex) {
        PieChart.SliceProvider.series(
            slices.indices.map { index ->
                PieChart.Slice(
                    fill = Fill(ChartColors.series[index]),
                    offsetFromCenter = if (index == selectedIndex) PieChartSpec.SELECTION_SHIFT_DP.dp else 0.dp,
                    label = label,
                )
            },
        )
    }
    val outerRadiusPx = with(LocalDensity.current) { (PieChartSpec.OUTER_DIAMETER_DP / 2).dp.toPx() }
    val innerRadiusPx = outerRadiusPx * PieChartSpec.HOLE_RATIO

    Column(modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(PieChartSpec.HEIGHT_DP.dp)
                .pointerInput(outerRadiusPx) {
                    detectTapGestures { offset ->
                        val index = PieChartSpec.sliceIndexAt(
                            dx = offset.x - size.width / 2f,
                            dy = offset.y - size.height / 2f,
                            outerRadius = outerRadiusPx,
                            innerRadius = innerRadiusPx,
                        )
                        // MPAndroidChart と同じく、同じスライスをもう一度タップするか、円の外をタップすると選択を外す
                        selectedIndex = index.takeIf { it != selectedIndex }
                    }
                },
        ) {
            PieChartHost(
                chart = rememberPieChart(
                    sliceProvider = sliceProvider,
                    spacing = PieChartSpec.SLICE_SPACE_DP.dp,
                    // Vico は「指定した半径 − 一番大きいずらし量」で円を描くので、スライスを選ぶたびに円が縮む。
                    // MPAndroidChart は最初からずらす分を空けていて大きさが変わらないので、選択中はずらす分だけ大きく指定する
                    outerSize = PieSize.Outer.fixed(
                        (PieChartSpec.OUTER_DIAMETER_DP + if (selectedIndex != null) PieChartSpec.SELECTION_SHIFT_DP * 2 else 0f).dp,
                    ),
                    innerSize = PieSize.Inner.fixed((PieChartSpec.OUTER_DIAMETER_DP * PieChartSpec.HOLE_RATIO).dp),
                    valueFormatter = SliceLabelFormatter,
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PieChartSpec.HEIGHT_DP.dp),
                chartAreaHeight = PieChartSpec.HEIGHT_DP.dp,
            )
            Text(
                text = ChartFormat.pieCenterText(PieChartSpec.total),
                fontSize = PieChartSpec.CENTER_TEXT_SIZE_SP.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        PieSelectionText(selectedIndex?.let { slices[it] }, PieChartSpec.total)
    }
}
