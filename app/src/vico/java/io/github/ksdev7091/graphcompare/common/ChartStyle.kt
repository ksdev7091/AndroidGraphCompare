package io.github.ksdev7091.graphcompare.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.VicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.AdditionScope
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.HorizontalLegend
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import io.github.ksdev7091.graphcompare.core.AxisRange
import io.github.ksdev7091.graphcompare.core.ChartSpec

/** 軸の幅は既定ではラベルの長さで決まる。3 実装で描画領域の左右の端を揃えるために固定する。 */
val FixedYAxisSize = BaseAxis.Size.Fixed(ChartSpec.Y_AXIS_WIDTH_DP.dp)

/** 軸の範囲は自動計算に任せず、3 実装で揃える。Vico では範囲は軸ではなくレイヤーに設定する。 */
fun AxisRange.toRangeProvider(): CartesianLayerRangeProvider =
    CartesianLayerRangeProvider.fixed(minY = min.toDouble(), maxY = max.toDouble())

/** 目盛りの間隔。MPAndroidChart は目盛りの「数」、Vico は「間隔」で指定する。 */
fun AxisRange.toItemPlacer(): VerticalAxis.ItemPlacer = VerticalAxis.ItemPlacer.step({ step.toDouble() })

/** x 軸のラベルを [step] 個おきに置く。Vico は端のラベルが切れないよう既定で両端に余白を足すが、MPAndroidChart にはないので切る。 */
fun xLabelPlacer(step: Int): HorizontalAxis.ItemPlacer =
    HorizontalAxis.ItemPlacer.aligned(spacing = { step }, addExtremeLabelPadding = false)

@Composable
fun rememberChartAxisLabel(): TextComponent = rememberAxisLabelComponent(
    style = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = ChartSpec.AXIS_TEXT_SIZE_SP.sp),
)

/** 補助線。Vico の既定は点線なので、MPAndroidChart に合わせて実線にする。 */
@Composable
fun rememberChartGridLine(): LineComponent = rememberAxisGuidelineComponent(
    fill = Fill(MaterialTheme.colorScheme.outlineVariant),
    thickness = ChartSpec.GRID_LINE_WIDTH_DP.dp,
    shape = RectangleShape,
)

/**
 * 凡例。MPAndroidChart と違って既定では出ないので、系列ごとに自分で並べる。
 * Vico の凡例は Canvas の左端から並ぶので、y 軸の幅だけずらして描画領域の左端に合わせる。
 */
@Composable
fun rememberChartLegend(items: List<Pair<Color, String>>): HorizontalLegend<CartesianMeasuringContext, CartesianDrawingContext> {
    val label = rememberTextComponent(
        style = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = ChartSpec.LEGEND_TEXT_SIZE_SP.sp),
    )
    val addItems: AdditionScope<LegendItem>.(ExtraStore) -> Unit = remember(items, label) {
        {
            items.forEach { (color, text) -> add(LegendItem(ShapeComponent(Fill(color), RectangleShape), label, text)) }
        }
    }
    return rememberHorizontalLegend(
        items = addItems,
        iconSize = ChartSpec.LEGEND_ICON_SIZE_DP.dp,
        iconLabelSpacing = ChartSpec.LEGEND_ICON_LABEL_SPACING_DP.dp,
        columnSpacing = ChartSpec.LEGEND_ITEM_SPACING_DP.dp,
        padding = Insets(start = ChartSpec.Y_AXIS_WIDTH_DP.dp, top = 4.dp),
    )
}

/** この比較ではタップ以外の操作をそろえるため、スクロールは止める。 */
@Composable
fun rememberFixedScrollState(): VicoScrollState = rememberVicoScrollState(scrollEnabled = false)

/**
 * Vico は x の間隔を dp で決め、はみ出した分はスクロールで見せる。スクロールを止めると画面外が見えなくなるので、
 * 全体が収まる倍率（Zoom.Content）に固定する。MPAndroidChart は範囲を画面幅に合わせて描くので、この指定は要らない。
 */
@Composable
fun rememberFitZoomState(): VicoZoomState =
    rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content, minZoom = Zoom.Content)
