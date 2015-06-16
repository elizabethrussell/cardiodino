package com.datadoghealth.cardiodino;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.datadoghealth.cardiodino.core.UniBus;
import com.datadoghealth.cardiodino.util.HR;
import com.squareup.otto.Subscribe;

import java.util.Random;

/**
 * Created by root on 6/12/15.
 */
public class AStart extends Activity {

    private static int FUDGE_LIMIT  = 12;   //on average, how much does hr increase or decrease
    private static int TOLERANCE    = 6;    //potential deviation from previous

    private static double SCREEN_RE_TARGET = .25; // how much space of screen is above/below target
    private static double DIST_TO_TARG = .667;  // how much of remaining screen between current and target initially

    private int startingHr;
    private boolean lastTrendUp= true;
    private int lastStart = -1;
    private int target;
    private Random rand;
    private int prevpos = -1;

    // views stuff
    private int         screenHeight;
    private TextView    hrTextView;
    private View        targetView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        UniBus.get().register(this);

        // gather display dimensions
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenHeight = size.y;
        Log.i("ScreenHeight", String.valueOf(screenHeight));

        // assign elements
        hrTextView = (TextView)findViewById(R.id.start_text_hr);
        hrTextView.setY(screenHeight / 2);
        targetView = findViewById(R.id.start_target_area);
        targetView.setLayoutParams(new LinearLayout.LayoutParams(size.x,screenHeight/4));
        targetView.setY(0);
    }



    @Subscribe
    public void receivedHR(HR hr)
    {
        if (lastStart<0) {
            lastStart = hr.hr;
            target = lastStart+10;
        }
        moveCurrent(hr.hr);
        /*if ((lastTrendUp == (hr.hr>target))||(lastStart<0)) { // XNOR change target
            newTarget();
        }*/
    }


    // Shuffle views to reflect new goal
    public void newTarget(){
        // gen location of target
        


        // draw target
        View v = findViewById(R.id.start_target_area);

    }


    // update view for heart rate
    public void moveCurrent(int current){
        hrTextView.setText(String.valueOf(current));
        int pos;
        
        if (lastTrendUp) {
            double q = screenHeight*SCREEN_RE_TARGET;
            double m = (2.0*q / ((double)(lastStart-target)));
            double b = (q*(1-(2.0*target/(lastStart-target))));
            pos = (int)(m*current+b);
            if (current > target) {
                pos = (int)(m*target+b);
            }
            if (pos >screenHeight) {
                pos = screenHeight;
            }
        } else {
            pos = screenHeight/2;
        }


        if (prevpos<0) {
            prevpos=pos;
        } else {
            hrTextView.animate().translationY(pos - prevpos);
            Log.i("Translation", String.valueOf(pos - prevpos));
            prevpos = pos;
        }
    }


    public int changeTarget() {
        if (rand==null) {
            rand = new Random(System.currentTimeMillis());
        }
        int tol = rand.nextInt(TOLERANCE) - (TOLERANCE/2);
        int add = lastTrendUp ? -FUDGE_LIMIT-tol : FUDGE_LIMIT+tol;
        target +=add;
        return target;
    }
}
