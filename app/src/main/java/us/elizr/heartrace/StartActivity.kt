package us.elizr.heartrace

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_home.*
import android.content.Intent
import android.os.Handler
import android.support.v4.content.ContextCompat
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import us.elizr.heartrace.bluetooth.BLEConnectingView

class StartActivity : AppCompatActivity() {

    // math for my graphic
    val a = 10.0
    val b = a + 10
    val c = b + 3
    val d = c + 3
    val e = d + 3
    val f = e + 2
    val g = f + 5
    val h = g + 5
    val i = h + 10
    var x = 1.0
    private val mod = i.toInt() + 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Go to Bluetooth connection view on home click
        text_home_play.setOnClickListener{ _ ->
            val intent = Intent(this, BLEConnectingView::class.java)
            startActivity(intent)
        }

        text_home_high_scores.setOnClickListener { _ ->
            // TODO
        }

        // prepare ECG plot and view series
        graph_home_ecg.viewport.isXAxisBoundsManual = true
        graph_home_ecg.viewport.setMinX(0.0)
        graph_home_ecg.viewport.setMaxX(mod.toDouble())
        graph_home_ecg.viewport.isYAxisBoundsManual = true
        graph_home_ecg.viewport.setMinY(-4.0)
        graph_home_ecg.viewport.setMaxY(12.0)
        graph_home_ecg.gridLabelRenderer.isHorizontalLabelsVisible = false
        graph_home_ecg.gridLabelRenderer.isVerticalLabelsVisible = false
        graph_home_ecg.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.NONE

        val series = LineGraphSeries<DataPoint>()
        series.color = ContextCompat.getColor(this, R.color.red_heartrace)
        series.thickness = 8
        graph_home_ecg.addSeries(series)


        // paint ECG
        val handler = Handler()
        val graphDrawerRunnable = object: Runnable {
            override fun run() {
                series.appendData(DataPoint(x++, getNextPoint()), (x>mod), mod)
                handler.postDelayed(this, 20)
            }
        }
        handler.postDelayed(graphDrawerRunnable, 1000)
    }

    // incrementally compute a fake ecg waveform
    private fun getNextPoint(): Double {
        val adjx = x % mod
        if (adjx >= a && adjx < b) {
            return Math.sin((adjx - a) * (Math.PI / (b - a))) // P wave
        }
        if (adjx >= h && adjx < i) {
            return 1.5 * Math.sin((adjx - h) * (Math.PI / (i - h))) // T wave
        }

        // Slopes / straights for QRS complex
        if (adjx >= c && adjx < d) {
            return .8 / (c - d) * (adjx - c)
        }
        if (adjx >= d && adjx < e) {
            return -10.8 / (d - e) * (adjx - e) + 10
        }
        if (adjx >= e && adjx < f) {
            return 14 / (e - f) * (adjx - e) + 10
        }
        return if (adjx >= f && adjx < g) {
            -4 / (f - g) * (adjx - g)
        } else 0.0
    }

}
