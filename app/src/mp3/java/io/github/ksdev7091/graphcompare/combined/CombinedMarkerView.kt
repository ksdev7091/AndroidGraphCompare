package io.github.ksdev7091.graphcompare.combined

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import io.github.ksdev7091.graphcompare.R
import io.github.ksdev7091.graphcompare.core.ChartFormat
import io.github.ksdev7091.graphcompare.core.CombinedPoint

/** タップした月の値を出すマーカー。3.x では XML レイアウトを持つ View を継承して作る。 */
@SuppressLint("ViewConstructor")
class CombinedMarkerView(context: Context) : MarkerView(context, R.layout.marker_combined) {
    private val label: TextView = findViewById(R.id.marker_label)

    override fun refreshContent(e: Entry, highlight: Highlight) {
        // Entry の data は Object なので、取り出すときにキャストが要る
        (e.data as? CombinedPoint)?.let { label.text = ChartFormat.markerText(it) }
        super.refreshContent(e, highlight)
    }

    // タップした位置の上に中央揃えで出す（画面端でのはみ出しは MarkerView が調整する）
    override fun getOffset(): MPPointF =
        MPPointF(-width / 2f, -height - Utils.convertDpToPixel(12f))
}
