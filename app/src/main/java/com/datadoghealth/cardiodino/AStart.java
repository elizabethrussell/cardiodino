package com.datadoghealth.cardiodino;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.datadoghealth.cardiodino.core.UniBus;
import com.datadoghealth.cardiodino.util.HR;
import com.squareup.otto.Subscribe;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by root on 6/12/15.
 */
public class AStart extends Activity {

    // hr anchor
    private int startingHr = -1;

    // view layout
    private static int      FUDGE_LIMIT  = 8;   // on average, how much does hr increase or decrease
    private static int      TOLERANCE    = 3;    // potential deviation from previous
    private static int      VIEW_MARGIN  = 100;   // dp above and below maximum target
    private static double   LOW_HANDICAP = 3;    // it's harder to decrease hr so adjust target bounds for this
    private        int      ballHeight;

    // target stuff
    private boolean lastTrendUp= false;
    private int target;
    private Random rand;

    // views stuff
    private int         screenHeight;
    private int         screenWidth;
    private TextView    hrTextView;
    private View        targetView;
    private TextView    targetTextView;
    private TextView    timerTextView;
    private TextView    scoreTextView;

    // fun
    private int score;
    private static final String TIMER_FORMAT = "%02d:%02d";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        UniBus.get().register(this);

        // gather display dimensions
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenHeight = size.y;
        screenWidth = size.x;

        // assign elements
        hrTextView     = (TextView) findViewById(R.id.start_text_hr);
        ballHeight     = (int)      getResources().getDimension(R.dimen.ball_height);
        targetView     =            findViewById(R.id.start_target_area);
        targetTextView = (TextView) findViewById(R.id.start_text_target);
        timerTextView  = (TextView) findViewById(R.id.start_text_timer);
        scoreTextView  = (TextView) findViewById(R.id.start_text_score);

        // place ball
        hrTextView.setX((screenWidth/2)-(ballHeight/2));

        // set score and start timer
        score = 0;
        scoreTextView.setText("Score: "+score);


        /*new CountDownTimer(300000,1000) {
            public void onTick(long millisUntilFinished) {
                timerTextView.setText(""+String.format(TIMER_FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                timerTextView.setText("DONE");
            }
        };*/



        Log.i("Pos params", "screenheight: "+screenHeight+" ballHeight: "+ballHeight);

    }



    @Subscribe
    public void receivedHR(HR hr)
    {
        if (startingHr<0){
            startingHr = hr.hr;
            newTarget();
        }
        moveCurrent(hr.hr);
        if (lastTrendUp == (hr.hr>target)) { // XNOR change target
            hitTarget();
        }
    }

    public void hitTarget() {
        scoreTextView.setText("Score: "+ (++score));
        newTarget();
    }


    // Shuffle views to reflect new goal
    public void newTarget(){
        changeTarget();
        targetView.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth, 10));
        int targetAddress = targetToPos(target);
        targetView.setY(targetAddress);
        targetTextView.setText(String.valueOf(target));
        targetTextView.setY(targetAddress -100);

        Log.i("new target", "target: " + target + " position: " + targetAddress);
        lastTrendUp ^= true;

    }


    // update view for heart rate
    public void moveCurrent(int current) {
        hrTextView.setText(String.valueOf(current));
        hrTextView.animate().y(hrToPos(current));

    }

    public int hrToPos(int current) {
        int pos;
        double m = (((2*VIEW_MARGIN)-screenHeight)/((FUDGE_LIMIT*(1+(1/LOW_HANDICAP)))+2*TOLERANCE));
        double b = (VIEW_MARGIN-(m*(startingHr+FUDGE_LIMIT+TOLERANCE)));
        pos = (int)(m*current+b);
        if (lastTrendUp == (current>target)) {  // hit or exceeded target
            pos = (int) (m*target+b);
        } else if (pos > screenHeight-(ballHeight/2)) { // hit screen top bound
            pos = screenHeight-(ballHeight/2);
        } else if (pos < (ballHeight/2)) {
            pos = ballHeight/2;
        }
        int adjustedPos =  (pos-(ballHeight/2));
        return adjustedPos;
    }

    public int targetToPos(int tar) {
        int pos;
        double m = (((2*VIEW_MARGIN)-screenHeight)/((FUDGE_LIMIT*(1+(1/LOW_HANDICAP)))+2*TOLERANCE));
        double b = (VIEW_MARGIN-(m*(startingHr+FUDGE_LIMIT+TOLERANCE)));
        pos = (int)(m*tar+b);
        return pos;
    }


    public int changeTarget() {
        if (rand==null) {
            rand = new Random(System.currentTimeMillis());
        }
        int tol = rand.nextInt(TOLERANCE*2+1) - TOLERANCE;
        int add = lastTrendUp ? (int)(-(FUDGE_LIMIT/LOW_HANDICAP)-tol) : FUDGE_LIMIT+tol;
        target = startingHr +add;
        Log.i("target calc", "tol: "+tol+" add: "+add+" target: "+target);
        return target;
    }
}
