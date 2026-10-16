package io.github.winschneid.graphcompare.performance

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import io.github.winschneid.graphcompare.common.FixedYAxisSize
import io.github.winschneid.graphcompare.common.rememberChartAxisLabel
import io.github.winschneid.graphcompare.common.rememberChartGridLine
import io.github.winschneid.graphcompare.common.toItemPlacer
import io.github.winschneid.graphcompare.common.toRangeProvider
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.ChartSpec
import io.github.winschneid.graphcompare.core.LargeDataSpec
import io.github.winschneid.graphcompare.core.ui.ChartColors

private val IntegerFormatter = CartesianValueFormatter { _, value, _ -> ChartFormat.integer(value) }
private val Range = LargeDataSpec.range.toRangeProvider()
private val ItemPlacer = LargeDataSpec.range.toItemPlacer()
private val LayerPadding = CartesianLayerPadding(unscalableEnd = ChartSpec.NO_AXIS_SIDE_PADDING_DP.dp)

/**
 * Vico 3.3.1 で 10,000 点の折れ線を描き、x 方向のピンチズームとスクロールを有効にする。
 * 最初は全体が収まる倍率（Zoom.Content）で表示し、そこから拡大できるようにする。
 */
@Composable
fun LargeDataScreen(modifier: Modifier = Modifier) {
    val series = LargeDataSpec.series
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(series) {
        modelProducer.runTransaction { lineModel { series(series) } }
    }
    val color = ChartColors.series[0]

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = remember(color) { LineCartesianLayer.LineFill.single(Fill(color)) },
                        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = LargeDataSpec.LINE_WIDTH_DP.dp),
                    ),
                ),
                rangeProvider = Range,
            ),
            startAxis = VerticalAxis.rememberStart(
                label = rememberChartAxisLabel(),
                valueFormatter = IntegerFormatter,
                guideline = rememberChartGridLine(),
                itemPlacer = ItemPlacer,
                size = FixedYAxisSize,
            ),
            // x のラベルの間隔は、拡大率に合わせて重ならないよう Vico に任せる（MPAndroidChart も自動）
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberChartAxisLabel(),
                valueFormatter = IntegerFormatter,
                guideline = null,
            ),
            layerPadding = { LayerPadding },
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        scrollState = rememberVicoScrollState(scrollEnabled = true),
        zoomState = rememberVicoZoomState(zoomEnabled = true, initialZoom = Zoom.Content, minZoom = Zoom.Content),
    )
}
