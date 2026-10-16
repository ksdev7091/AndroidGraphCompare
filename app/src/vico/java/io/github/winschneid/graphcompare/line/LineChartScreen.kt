package io.github.winschneid.graphcompare.line

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
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
import com.patrykandpatrick.vico.compose.common.Fill
import io.github.winschneid.graphcompare.common.FixedYAxisSize
import io.github.winschneid.graphcompare.common.rememberChartAxisLabel
import io.github.winschneid.graphcompare.common.rememberChartGridLine
import io.github.winschneid.graphcompare.common.rememberChartLegend
import io.github.winschneid.graphcompare.common.rememberFitZoomState
import io.github.winschneid.graphcompare.common.rememberFixedScrollState
import io.github.winschneid.graphcompare.common.toItemPlacer
import io.github.winschneid.graphcompare.common.toRangeProvider
import io.github.winschneid.graphcompare.common.xLabelPlacer
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.ChartSpec
import io.github.winschneid.graphcompare.core.LineChartSpec
import io.github.winschneid.graphcompare.core.SampleData
import io.github.winschneid.graphcompare.core.ui.ChartColors

private val DayFormatter = CartesianValueFormatter { _, value, _ -> ChartFormat.day(value) }
private val VisitorsFormatter = CartesianValueFormatter { _, value, _ -> ChartFormat.integer(value) }
private val VisitorsRange = LineChartSpec.visitors.toRangeProvider()
private val VisitorsItemPlacer = LineChartSpec.visitors.toItemPlacer()
private val DayItemPlacer = xLabelPlacer(LineChartSpec.DAY_LABEL_STEP)

// 右に軸がないとき、MPAndroidChart は最小余白（15dp）を取り、Vico は端まで描く。MPAndroidChart に合わせる。
// なお Vico は描画領域の端ちょうどにある x のラベル（1日）を描かない。ラベルの最大幅が端までの距離で決まるため
private val LayerPadding = CartesianLayerPadding(unscalableEnd = ChartSpec.NO_AXIS_SIDE_PADDING_DP.dp)

/** Vico 3.3.1 の折れ線グラフ。2 系列を曲線でつなぎ、下をグラデーションで塗る。 */
@Composable
fun LineChartScreen(modifier: Modifier = Modifier) {
    val series = SampleData.lineSeries
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(series) {
        modelProducer.runTransaction {
            lineModel { series.forEach { series(it) } }
        }
    }

    val colors = ChartColors.series.take(series.size)
    val lines = colors.map { color ->
        LineCartesianLayer.rememberLine(
            fill = remember(color) { LineCartesianLayer.LineFill.single(Fill(color)) },
            stroke = LineCartesianLayer.LineStroke.Continuous(thickness = LineChartSpec.LINE_WIDTH_DP.dp),
            // グラデーションは Compose の Brush をそのまま渡せる
            areaFill = remember(color) {
                LineCartesianLayer.AreaFill.single(
                    Fill(Brush.verticalGradient(listOf(color.copy(alpha = LineChartSpec.FILL_TOP_ALPHA), Color.Transparent))),
                )
            },
            // cubic() は点の間で角が残り直線に近く見える。MPAndroidChart の CUBIC_BEZIER に近い、点を通るなめらかな曲線にする
            interpolator = remember { LineCartesianLayer.Interpolator.catmullRom() },
        )
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(lines),
                rangeProvider = VisitorsRange,
            ),
            startAxis = VerticalAxis.rememberStart(
                label = rememberChartAxisLabel(),
                valueFormatter = VisitorsFormatter,
                guideline = rememberChartGridLine(),
                itemPlacer = VisitorsItemPlacer,
                size = FixedYAxisSize,
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberChartAxisLabel(),
                valueFormatter = DayFormatter,
                guideline = null,
                itemPlacer = DayItemPlacer,
            ),
            layerPadding = { LayerPadding },
            legend = rememberChartLegend(colors.zip(ChartFormat.LINE_SERIES_LABELS)),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        scrollState = rememberFixedScrollState(),
        zoomState = rememberFitZoomState(),
    )
}
