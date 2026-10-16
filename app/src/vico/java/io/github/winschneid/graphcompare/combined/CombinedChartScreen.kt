package io.github.winschneid.graphcompare.combined

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.BaseAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import io.github.winschneid.graphcompare.core.ChartFormat
import io.github.winschneid.graphcompare.core.CombinedChartSpec
import io.github.winschneid.graphcompare.core.CombinedPoint
import io.github.winschneid.graphcompare.core.SampleData
import io.github.winschneid.graphcompare.core.ui.ChartColors
import io.github.winschneid.graphcompare.core.ui.CombinedSelectionText
import kotlin.math.roundToInt

private val MonthFormatter = CartesianValueFormatter { _, value, _ -> ChartFormat.month(value) }
private val SalesFormatter = CartesianValueFormatter { _, value, _ -> ChartFormat.sales(value) }
private val PercentFormatter = CartesianValueFormatter { _, value, _ -> ChartFormat.percent(value) }

// 軸の範囲と目盛りは自動計算に任せず、3 実装で揃える。Vico では範囲はレイヤー、目盛りは軸に設定する
private val SalesRange = CartesianLayerRangeProvider.fixed(
    minY = CombinedChartSpec.SALES_MIN.toDouble(),
    maxY = CombinedChartSpec.SALES_MAX.toDouble(),
)
private val GrowthRange = CartesianLayerRangeProvider.fixed(
    minY = CombinedChartSpec.GROWTH_MIN.toDouble(),
    maxY = CombinedChartSpec.GROWTH_MAX.toDouble(),
)
private val SalesItemPlacer = VerticalAxis.ItemPlacer.step({ CombinedChartSpec.SALES_STEP.toDouble() })
private val GrowthItemPlacer = VerticalAxis.ItemPlacer.step({ CombinedChartSpec.GROWTH_STEP.toDouble() })
// Vico は端のラベル（1月・12月）が切れないよう、既定で両端に余白を足す。MPAndroidChart にはないので切る
private val MonthItemPlacer = HorizontalAxis.ItemPlacer.aligned(
    spacing = { CombinedChartSpec.MONTH_LABEL_STEP },
    addExtremeLabelPadding = false,
)

// 軸の幅は既定ではラベルの長さで決まる。3 実装で描画領域の左右の端を揃えるために固定する
private val YAxisSize = BaseAxis.Size.Fixed(CombinedChartSpec.Y_AXIS_WIDTH_DP.dp)

// 棒の太さ：MPAndroidChart は「x の 1 区間に対する割合」、Vico は「太さと間隔を dp」で指定する。
// 同じ割合になるよう、太さ・間隔・折れ線の点の間隔を CombinedChartSpec から決める。
private val BarThickness = CombinedChartSpec.BAR_THICKNESS_DP.dp
private val BarSpacing = CombinedChartSpec.BAR_SPACING_DP.dp
private val XSpacing = BarThickness + BarSpacing
private val PointSize = 8.dp

// Vico は両端に「棒の太さの半分」しか余白を取らない。MPAndroidChart（両端に半区間）に合わせて間隔の半分を足す
private val LayerPadding = CartesianLayerPadding(scalableStart = BarSpacing / 2, scalableEnd = BarSpacing / 2)

/**
 * Vico 3.3.1 の複合グラフ。棒（ColumnCartesianLayer）と折れ線（LineCartesianLayer）を
 * 1 つの CartesianChart に重ね、それぞれを左右の軸に結びつける。
 * データは ModelProducer に数値だけを渡すので、マーカーでは x 座標から元のデータを引き直す。
 */
@Composable
fun CombinedChartScreen(modifier: Modifier = Modifier) {
    val points = SampleData.combined
    val modelProducer = remember { CartesianChartModelProducer() }
    var selected by remember { mutableStateOf<CombinedPoint?>(null) }

    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            columnModel { series(points.map { it.sales }) }
            lineModel { series(points.map { it.growthRatePercent }) }
        }
    }

    val salesColor = ChartColors.series[0]
    val growthColor = ChartColors.series[1]
    val textColor = MaterialTheme.colorScheme.onSurface
    val axisLabel = rememberAxisLabelComponent(style = TextStyle(color = textColor, fontSize = 10.sp))
    // 既定の補助線は点線なので、MPAndroidChart に合わせて実線にする
    val gridLine = rememberAxisGuidelineComponent(
        fill = Fill(MaterialTheme.colorScheme.outlineVariant),
        thickness = CombinedChartSpec.GRID_LINE_WIDTH_DP.dp,
        shape = RectangleShape,
    )
    val legendLabel = rememberTextComponent(
        style = TextStyle(color = textColor, fontSize = CombinedChartSpec.LEGEND_TEXT_SIZE_SP.sp),
    )

    val marker = rememberDefaultCartesianMarker(
        // Vico のマーカーは Canvas に描く TextComponent なので、Composable は置けない
        label = rememberTextComponent(
            style = TextStyle(color = ChartColors.markerText, fontSize = 12.sp, lineHeight = 16.sp),
            lineCount = 3,
            padding = Insets(10.dp, 6.dp),
            background = rememberShapeComponent(
                fill = Fill(ChartColors.markerBackground),
                shape = RoundedCornerShape(8.dp),
            ),
        ),
        valueFormatter = remember(points) {
            DefaultCartesianMarker.ValueFormatter { _, targets ->
                points.pointAt(targets.first().x)?.let(ChartFormat::markerText).orEmpty()
            }
        },
        guideline = rememberAxisGuidelineComponent(),
    )
    val markerListener = remember(points) {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                selected = points.pointAt(targets.first().x)
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                selected = points.pointAt(targets.first().x)
            }

            override fun onHidden(marker: CartesianMarker) {
                selected = null
            }
        }
    }

    Column(modifier) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(fill = Fill(salesColor), thickness = BarThickness),
                    ),
                    columnCollectionSpacing = BarSpacing,
                    rangeProvider = SalesRange,
                    verticalAxisPosition = Axis.Position.Vertical.Start,
                ),
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = remember(growthColor) { LineCartesianLayer.LineFill.single(Fill(growthColor)) },
                            stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 2.5.dp),
                            pointProvider = LineCartesianLayer.PointProvider.single(
                                LineCartesianLayer.Point(rememberShapeComponent(Fill(growthColor), CircleShape), size = PointSize),
                            ),
                        ),
                    ),
                    // 折れ線レイヤーの x 間隔は「点の大きさ + pointSpacing」。棒より広いとそちらに合わせて
                    // 全体が縮み、棒が細くなるので、棒の x 間隔と同じになるように点の大きさを引く
                    pointSpacing = XSpacing - PointSize,
                    rangeProvider = GrowthRange,
                    verticalAxisPosition = Axis.Position.Vertical.End,
                ),
                startAxis = VerticalAxis.rememberStart(
                    label = axisLabel,
                    valueFormatter = SalesFormatter,
                    guideline = gridLine,
                    itemPlacer = SalesItemPlacer,
                    size = YAxisSize,
                ),
                // 既定では全部の軸が補助線を引くので、MPAndroidChart に合わせて左軸の横線だけ残す
                endAxis = VerticalAxis.rememberEnd(
                    label = axisLabel,
                    valueFormatter = PercentFormatter,
                    guideline = null,
                    itemPlacer = GrowthItemPlacer,
                    size = YAxisSize,
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = axisLabel,
                    valueFormatter = MonthFormatter,
                    guideline = null,
                    itemPlacer = MonthItemPlacer,
                ),
                marker = marker,
                markerVisibilityListener = markerListener,
                // MPAndroidChart と同じく、タップでマーカーを出したままにする（既定は押している間だけ）
                markerController = CartesianMarkerController.rememberToggleOnTap(),
                layerPadding = { LayerPadding },
                // MPAndroidChart と違い、凡例は既定では出ないので自分で並べる。
                // Vico の凡例は Canvas の左端から並ぶので、y 軸の幅だけずらして描画領域の左端に合わせる。
                // 並び順も MPAndroidChart（CombinedData の既定で 折れ線 → 棒）に合わせる
                legend = rememberHorizontalLegend(
                    items = {
                        add(LegendItem(ShapeComponent(Fill(growthColor), RectangleShape), legendLabel, ChartFormat.GROWTH_LABEL))
                        add(LegendItem(ShapeComponent(Fill(salesColor), RectangleShape), legendLabel, ChartFormat.SALES_LABEL))
                    },
                    iconSize = CombinedChartSpec.LEGEND_ICON_SIZE_DP.dp,
                    iconLabelSpacing = CombinedChartSpec.LEGEND_ICON_LABEL_SPACING_DP.dp,
                    columnSpacing = CombinedChartSpec.LEGEND_ITEM_SPACING_DP.dp,
                    padding = Insets(start = CombinedChartSpec.Y_AXIS_WIDTH_DP.dp, top = 4.dp),
                ),
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            // この画面ではタップの比較に絞るため、ズームとスクロールは止める。
            // Vico は棒の間隔が dp で決まり、既定では画面からはみ出した分をスクロールで見せるので、
            // 全体が収まる倍率（Zoom.Content）を指定する
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.Content, minZoom = Zoom.Content),
        )
        CombinedSelectionText(selected)
    }
}

private fun List<CombinedPoint>.pointAt(x: Double): CombinedPoint? = getOrNull(x.roundToInt())
