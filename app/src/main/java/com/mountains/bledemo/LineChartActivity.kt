package com.mountains.bledemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mountains.bledemo.weiget.HistogramEntity
import kotlinx.android.synthetic.main.acitivy_line_chart.*
import java.util.*

class LineChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivy_line_chart)

        val datas = mutableListOf<HistogramEntity>()
        for (i in 0 .. 60){
            val random = Random()
            val value = (random.nextDouble() * 100 + 30).toInt()
            val histogramEntity = HistogramEntity(value, i * 1440L)
            datas.add(histogramEntity)
        }
        histogramView.loadData(datas)
    }
}