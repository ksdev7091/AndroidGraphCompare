package io.github.winschneid.graphcompare.bar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
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
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import io.github.winschneid.graphcompare.common.FixedYAxisSize
import io.github.winschneid.graphcompare.common.rememberChartAxisLabel
import io.github.winschneid.graphcompare.common.rememberChartGridLine
import io.github.winschneid.graphcompare.common.rememberChartLegend
import io.github.winschneid.graphcompare.common.rememberFitZoomState
import io.github.winschneid.graphcompare.common.rememberFixedScrollState
import io.github.winschneid.graphcompare.common.toItemPlacer
import io.github.winschneid.graphcompare.common.toRangeProvider
import io.github.winschneid.graphcompare.common.xLabelPlacer
import io.github.winschneid.graphcompare.core.BarChartSpec
import io.github.winschneid.graphcompare.core.BarMode
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.ChartSpec
import io.github.winschneid.graphcompare.core.ui.BarModeSelector
import io.github.winschneid.graphcompare.core.ui.ChartColors

private val MonthFormatter = CartesianValueFormatter { _, value, _ -> ChartFormat.month(value) }
private val SalesFormatter = CartesianValueFormatter { _, value, _ -> ChartFormat.integer(value) }
private val MonthItemPlacer = xLabelPlacer(1)
private val BarModeKey = ExtraStore.Key<BarMode>()

// MPAndroidChart は太さと間隔を「x の 1 区間に対する割合」、Vico は dp で指定する。
// x の 1 区間を BarChartSpec.X_UNIT_DP とみなして、割合から dp に換算する
private fun ratioToDp(ratio: Float) = (ratio * BarChartSpec.X_UNIT_DP).dp

/** 表示方法ごとの棒の太さ・群の中の間隔・群どうしの間隔。 */
private class ColumnLayout(val thickness: Float, val innerSpacing: Float, val collectionSpacing: Float)

private fun columnLayout(mode: BarMode): ColumnLayout = when (mode) {
    BarMode.Grouped -> {
        val groupWidth = 3 * BarChartSpec.GROUPED_BAR_WIDTH + 2 * BarChartSpec.BAR_SPACE
        ColumnLayout(BarChartSpec.GROUPED_BAR_WIDTH, BarChartSpec.BAR_SPACE, 1f - groupWidth)
    }
    BarMode.Stacked -> ColumnLayout(BarChartSpec.STACKED_BAR_WIDTH, 0f, 1f - BarChartSpec.STACKED_BAR_WIDTH)
}

/**
 * Vico 3.3.1 の棒グラフ。グループと積み上げは ColumnCartesianLayer の MergeMode で切り替える。
 * Vico は区画ごとに形（Shape）を当てるので、積み上げで角丸をそのまま使うと途中の区画まで丸くなる。
 * MPAndroidChart 4.x に合わせ、表示方法と系列ごとに形を変える。
 */
@Composable
fun BarChartScreen(modifier: Modifier = Modifier) {
    var mode by rememberSaveable { mutableStateOf(BarMode.Grouped) }
    val sales = BarChartSpec.sales
    val modelProducer = remember { CartesianChartModelProducer() }
    // y の範囲は transaction を流したときに計算される。表示方法だけを変えても、データが同じだと計算し直されないので、
    // 表示方法を ExtraStore に入れてモデルごと差し替える
    LaunchedEffect(sales, mode) {
        modelProducer.runTransaction {
            columnModel { ChartFormat.PRODUCT_LABELS.indices.forEach { product -> series(sales.map { it.values[product] }) } }
            extras { it[BarModeKey] = mode }
        }
    }

    val layout = columnLayout(mode)
    val colors = ChartColors.series.take(ChartFormat.PRODUCT_LABELS.size)
    // MPAndroidChart 4.x に合わせる：グループは 4 隅とも丸め、積み上げは一番上の区画の上側だけを丸める
    val radius = BarChartSpec.CORNER_RADIUS_DP.dp
    val columns = colors.mapIndexed { index, color ->
        val shape = when {
            mode == BarMode.Grouped -> RoundedCornerShape(radius)
            index == colors.lastIndex -> RoundedCornerShape(topStart = radius, topEnd = radius)
            else -> RectangleShape
        }
        rememberLineComponent(fill = Fill(color), thickness = ratioToDp(layout.thickness), shape = shape)
    }
    val range = BarChartSpec.range(mode)
    // Vico は両端に「群の幅の半分」しか余白を取らない。MPAndroidChart（両端に半区間）に合わせて間隔の半分を足す。
    // 右に軸がないときの余白（MPAndroidChart の最小余白）も足す
    val layerPadding = remember(layout) {
        val halfSpacing = ratioToDp(layout.collectionSpacing) / 2
        CartesianLayerPadding(
            scalableStart = halfSpacing,
            scalableEnd = halfSpacing,
            unscalableEnd = ChartSpec.NO_AXIS_SIDE_PADDING_DP.dp,
        )
    }

    Column(modifier) {
        BarModeSelector(mode, onModeChange = { mode = it })
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(columns),
                    columnCollectionSpacing = ratioToDp(layout.collectionSpacing),
                    mergeMode = remember(mode, layout) {
                        { _ ->
                            when (mode) {
                                BarMode.Grouped -> ColumnCartesianLayer.MergeMode.Grouped(columnSpacing = ratioToDp(layout.innerSpacing))
                                BarMode.Stacked -> ColumnCartesianLayer.MergeMode.Stacked
                            }
                        }
                    },
                    rangeProvider = remember(range) { range.toRangeProvider() },
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = rememberChartAxisLabel(),
                    valueFormatter = SalesFormatter,
                    guideline = rememberChartGridLine(),
                    itemPlacer = remember(range) { range.toItemPlacer() },
                    size = FixedYAxisSize,
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = rememberChartAxisLabel(),
                    valueFormatter = MonthFormatter,
                    guideline = null,
                    itemPlacer = MonthItemPlacer,
                ),
                layerPadding = { layerPadding },
                legend = rememberChartLegend(colors.zip(ChartFormat.PRODUCT_LABELS)),
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            scrollState = rememberFixedScrollState(),
            zoomState = rememberFitZoomState(),
        )
    }
}
