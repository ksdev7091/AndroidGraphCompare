package io.github.winschneid.graphcompare.performance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
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
import io.github.winschneid.graphcompare.core.LazyListSpec
import io.github.winschneid.graphcompare.core.ui.ChartColors

private val DayFormatter = CartesianValueFormatter { _, value, _ -> ChartFormat.day(value) }
private val IntegerFormatter = CartesianValueFormatter { _, value, _ -> ChartFormat.integer(value) }
private val Range = LazyListSpec.range.toRangeProvider()
private val ItemPlacer = LazyListSpec.range.toItemPlacer()
private val DayItemPlacer = xLabelPlacer(LazyListSpec.DAY_LABEL_STEP)
private val LayerPadding = CartesianLayerPadding(unscalableEnd = ChartSpec.NO_AXIS_SIDE_PADDING_DP.dp)

/**
 * Vico 3.3.1 のチャートを LazyColumn に並べる。Compose で描くので View は作らない。
 * データは更新しないので、ModelProducer を使わずにモデルをそのまま渡す。
 */
@Composable
fun LazyListScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(LazyListSpec.ITEM_COUNT) { index ->
            val model = remember(index) {
                CartesianChartModel(LineCartesianLayerModel.build { series(LazyListSpec.series(index)) })
            }
            Column {
                Text(LazyListSpec.title(index), style = MaterialTheme.typography.titleSmall)
                SmallLineChart(model)
            }
        }
    }
}

@Composable
private fun SmallLineChart(model: CartesianChartModel) {
    val color = ChartColors.series[0]
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = remember(color) { LineCartesianLayer.LineFill.single(Fill(color)) },
                        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 2.dp),
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
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberChartAxisLabel(),
                valueFormatter = DayFormatter,
                guideline = null,
                itemPlacer = DayItemPlacer,
            ),
            layerPadding = { LayerPadding },
        ),
        model = model,
        modifier = Modifier
            .fillMaxWidth()
            .height(LazyListSpec.CHART_HEIGHT_DP.dp),
        // マーカーもスクロールもないので、指の動きはリストのスクロールに渡る
        scrollState = rememberFixedScrollState(),
        zoomState = rememberFitZoomState(),
    )
}
