package com.datadoghealth.cardiodino;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.datadoghealth.cardiodino.bluetooth.BluetoothScan;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;
import java.util.Timer;

/**
 * Created by root on 6/30/15.
 */
public class StartScreen extends Activity {
    private double x = 1d;
    private final Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        // init textviews with their fonts
        TextView title = (TextView) findViewById(R.id.home_title);
        Typeface font = Typeface.createFromAsset(getAssets(), "SigmarOne.ttf");
        title.setTypeface(font);

        TextView play = (TextView) findViewById(R.id.home_play);
        final Context c = this;
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c, BluetoothScan.class);
                startActivity(intent);
            }
        });
        //play.setTypeface(font);
        TextView highScore = (TextView) findViewById(R.id.home_high_scores);
        //highScore.setTypeface(font);

        // init graphview
        GraphView graph = (GraphView) findViewById(R.id.home_plot);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(mod);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-4);
        graph.getViewport().setMaxY(12);



        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);

        final LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        series.setColor(getResources().getColor(R.color.red_heartrace));
        graph.addSeries(series);

        Runnable timer = new Runnable() {
            @Override
            public void run() {
                boolean scroll = (x>mod);
                series.appendData(new DataPoint(x++, getNextPoint()),scroll, mod);
                handler.postDelayed(this, 20);
            }
        };
        handler.postDelayed(timer, 1000);

    }


    double a = 10;
    double b = a+10;
    double c = b+3;
    double d = c+3;
    double e = d+3;
    double f = e+2;
    double g = f+5;
    double h = g+5;
    double i = h+10;
    private int mod = (int)i+30;
    private double getNextPoint() {
        int adjx = (int)x%mod;
        if (adjx >= a && adjx <b) {
            return (Math.sin((adjx-a)*(Math.PI/(b-a))));
        }
        if (adjx >= h && adjx <i) {
            return (1.5*Math.sin((adjx-h)*(Math.PI/(i-h))));
        }
        if (adjx >= c && adjx <d) {
            return ((.8/(c-d))*(adjx-c));
        }
        if (adjx >=d && adjx <e) {
            return ((-10.8/(d-e))*(adjx-e)+10);
        }
        if (adjx >=e && adjx <f) {
            return ((14/(e-f))*(adjx-e)+10);
        }
        if (adjx >=f && adjx <g) {
            return ((-4/(f-g))*(adjx-g));
        }
        return 0;
    }


    private DataPoint[] generateData() {
        Random r = new Random();
        int count = 30;
        DataPoint[] values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = i;
            double f = r.nextDouble()*0.15+0.3;
            double y = Math.sin(i*f+2) + r.nextDouble()*0.3;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }
}
