package io.github.winschneid.graphcompare.performance

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import io.github.winschneid.graphcompare.common.FixedYAxisSize
import io.github.winschneid.graphcompare.common.rememberChartAxisLabel
import io.github.winschneid.graphcompare.common.rememberChartGridLine
import io.github.winschneid.graphcompare.common.rememberFitZoomState
import io.github.winschneid.graphcompare.common.rememberFixedScrollState
import io.github.winschneid.graphcompare.common.toItemPlacer
import io.github.winschneid.graphcompare.common.toRangeProvider
import io.github.winschneid.graphcompare.common.xLabelPlacer
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.ChartSpec
import io.github.winschneid.graphcompare.core.UpdateAnimationSpec
import io.github.winschneid.graphcompare.core.ui.ChartColors
import io.github.winschneid.graphcompare.core.ui.UpdateDataButton

private val MonthFormatter = CartesianValueFormatter { _, value, _ -> ChartFormat.month(value) }
private val IntegerFormatter = CartesianValueFormatter { _, value, _ -> ChartFormat.integer(value) }
private val Range = UpdateAnimationSpec.range.toRangeProvider()
private val ItemPlacer = UpdateAnimationSpec.range.toItemPlacer()
private val MonthItemPlacer = xLabelPlacer(UpdateAnimationSpec.MONTH_LABEL_STEP)

// 棒の太さと両端の余白は複合グラフと同じ考え方（MPAndroidChart の割合を dp に換算する）
private val BarThickness = ChartSpec.BAR_THICKNESS_DP.dp
private val BarSpacing = ChartSpec.BAR_SPACING_DP.dp
private val LayerPadding = CartesianLayerPadding(
    scalableStart = BarSpacing / 2,
    scalableEnd = BarSpacing / 2,
    unscalableEnd = ChartSpec.NO_AXIS_SIDE_PADDING_DP.dp,
)

// 時間と動き方を MPAndroidChart（300ms、等速）に合わせる。既定は約 500ms で、だんだん遅くなる動き
private val DiffAnimation = tween<Float>(durationMillis = UpdateAnimationSpec.DURATION_MS, easing = LinearEasing)

/**
 * Vico 3.3.1 でデータを差し替えてアニメーションさせる。
 * ModelProducer に新しいデータを流すと、前の値から新しい値へ自動で動く。
 */
@Composable
fun UpdateAnimationScreen(modifier: Modifier = Modifier) {
    var generation by rememberSaveable { mutableLongStateOf(1L) }
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(generation) {
        modelProducer.runTransaction { columnModel { series(UpdateAnimationSpec.values(generation)) } }
    }

    Column(modifier) {
        UpdateDataButton(onClick = { generation++ })
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(fill = Fill(ChartColors.series[0]), thickness = BarThickness),
                    ),
                    columnCollectionSpacing = BarSpacing,
                    rangeProvider = Range,
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = rememberChartAxisLabel(),
                    valueFormatter = IntegerFormatter,
                    guideline = rememberChartGridLine(),
                    itemPlacer = ItemPlacer,
                    size = FixedYAxisSize,
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = rememberChartAxisLabel(),
                    valueFormatter = MonthFormatter,
                    guideline = null,
                    itemPlacer = MonthItemPlacer,
                ),
                layerPadding = { LayerPadding },
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            scrollState = rememberFixedScrollState(),
            zoomState = rememberFitZoomState(),
            animationSpec = DiffAnimation,
            // 最初の表示はアニメーションさせない（MPAndroidChart に合わせる）
            initialAnimationSpec = null,
        )
    }
}
