package com.datadoghealth.cardiodino;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.datadoghealth.cardiodino.core.UniBus;
import com.datadoghealth.cardiodino.util.HR;
import com.datadoghealth.cardiodino.util.SharedPrefs;
import com.squareup.otto.Subscribe;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by root on 6/12/15.
 */
public class AStart extends Activity {

    // hr anchor
    private int startingHr = -1;

    // game level adjusters
    private int numberTargets;
    private final int[] targetNums = new int[]{6, 10, 16};
    private final int[] fudges = new int[]{6, 8, 10};

    // view layout
    private static int      FUDGE_LIMIT;        // on average, how much does hr increase or decrease
    private static int      TOLERANCE    = 3;    // potential deviation from previous
    private static int      VIEW_MARGIN  = 200;   // dp above and below maximum target
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
    private int level;
    private int remaining;
    private static final String TIMER_FORMAT = "%02d:%02d";
    private Handler handler = new Handler();
    private long startTime;
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (done) return;
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds %60;
            timerTextView.setText(String.format(TIMER_FORMAT, minutes, seconds));
            handler.postDelayed(this, 500);
        }
    };
    private boolean done = false;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // extract game mode
        Intent intent = getIntent();
        level = intent.getIntExtra(Levels.EXTRA_LEVEL, 0);
        FUDGE_LIMIT = fudges[level-1];
        numberTargets = targetNums[level-1];

        // rollout bus
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

        // place ball and pulse
        hrTextView.setX((screenWidth/2)-(ballHeight/2));

        // set score and start timer
        remaining = numberTargets;
        scoreTextView.setText(""+remaining);
        startTime = System.currentTimeMillis();
        handler.postDelayed(timerRunnable, 0);
    }



    @Subscribe
    public void receivedHR(HR hr)
    {
        if (startingHr<0){
            if (hr.hr<40) return;
            startingHr = hr.hr;
            newTarget();
        }
        moveCurrent(hr.hr);
        if (lastTrendUp == (hr.hr>target)) { // XNOR change target
            hitTarget();
        }
    }

    public void hitTarget() {
        if (remaining == 1) {
            done(System.currentTimeMillis() - startTime);
        }
        scoreTextView.setText("Targets remaining: " + (--remaining));
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
        } else if (pos > screenHeight-(ballHeight)) { // hit bottom screen bound
            pos = screenHeight-(ballHeight);
        } else if (pos < (ballHeight/2)) { // hit top screen bound
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

    public String trash = "dd_trash";

    public void done(long time) {
        done = true; int sc1; int sc2; int sc3;
        int seconds = (int) (time / 1000);
        int minutes = seconds / 60;
        seconds = seconds %60;
        String myScoreStr = String.format(TIMER_FORMAT, minutes, seconds);
        int myScore = scoreFromString(myScoreStr);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String[] sc;
        switch(level){
            case 1:
                sc = prefs.getString(SharedPrefs.HS_EASY_1,"").split("|");
                if (sc.length>1) {
                    String score = sc[1];
                    if (myScore < scoreFromString(score)) {
                        dialog(true, SharedPrefs.HS_EASY_1, SharedPrefs.HS_EASY_2, SharedPrefs.HS_EASY_3);
                        break;
                    } else {
                        sc = prefs.getString(SharedPrefs.HS_EASY_2,"").split("|");
                        if (sc.length>1) {
                            score = sc[1];
                            if (myScore<scoreFromString(score)) {
                                dialog(true, SharedPrefs.HS_EASY_2, SharedPrefs.HS_EASY_3, trash);
                                break;
                            } else {
                                sc = prefs.getString(SharedPrefs.HS_EASY_3,"").split("|");
                                if (sc.length>1) {
                                    score = sc[1];
                                    if (myScore < scoreFromString(score)){
                                        dialog(true, SharedPrefs.HS_EASY_3, trash, trash);
                                        break;
                                    } else {
                                        dialog(false, trash, trash, trash); break;
                                    }
                                } else {
                                    dialog(true, SharedPrefs.HS_EASY_3, trash, trash); break;
                                }
                            }
                        } else {
                            dialog(true, SharedPrefs.HS_EASY_2, trash, trash); break;
                        }
                    }
                } else {
                    dialog(true, SharedPrefs.HS_EASY_1, trash, trash); break;
                }
            case 2:
                sc = prefs.getString(SharedPrefs.HS_MEDI_1,"").split("|");
                if (sc.length>1) {
                    String score = sc[1];
                    if (myScore < scoreFromString(score)) {
                        dialog(true, SharedPrefs.HS_MEDI_1, SharedPrefs.HS_MEDI_2, SharedPrefs.HS_MEDI_3);
                        break;
                    } else {
                        sc = prefs.getString(SharedPrefs.HS_MEDI_2,"").split("|");
                        if (sc.length>1) {
                            score = sc[1];
                            if (myScore<scoreFromString(score)) {
                                dialog(true, SharedPrefs.HS_MEDI_2, SharedPrefs.HS_MEDI_3, trash);
                                break;
                            } else {
                                sc = prefs.getString(SharedPrefs.HS_MEDI_3,"").split("|");
                                if (sc.length>1) {
                                    score = sc[1];
                                    if (myScore < scoreFromString(score)){
                                        dialog(true, SharedPrefs.HS_MEDI_3, trash, trash);
                                        break;
                                    } else {
                                        dialog(false, trash, trash, trash); break;
                                    }
                                } else {
                                    dialog(true, SharedPrefs.HS_MEDI_3, trash, trash); break;
                                }
                            }
                        } else {
                            dialog(true, SharedPrefs.HS_MEDI_2, trash, trash); break;
                        }
                    }
                } else {
                    dialog(true, SharedPrefs.HS_MEDI_1, trash, trash); break;
                }
            case 3:
                sc = prefs.getString(SharedPrefs.HS_HARD_1,"").split("|");
                if (sc.length>1) {
                    String score = sc[1];
                    if (myScore < scoreFromString(score)) {
                        dialog(true, SharedPrefs.HS_HARD_1, SharedPrefs.HS_HARD_2, SharedPrefs.HS_HARD_3);
                        break;
                    } else {
                        sc = prefs.getString(SharedPrefs.HS_HARD_2,"").split("|");
                        if (sc.length>1) {
                            score = sc[1];
                            if (myScore<scoreFromString(score)) {
                                dialog(true, SharedPrefs.HS_HARD_2, SharedPrefs.HS_HARD_3, trash);
                                break;
                            } else {
                                sc = prefs.getString(SharedPrefs.HS_HARD_3,"").split("|");
                                if (sc.length>1) {
                                    score = sc[1];
                                    if (myScore < scoreFromString(score)){
                                        dialog(true, SharedPrefs.HS_HARD_3, trash, trash);
                                        break;
                                    } else {
                                        dialog(false, trash, trash, trash); break;
                                    }
                                } else {
                                    dialog(true, SharedPrefs.HS_HARD_3, trash, trash); break;
                                }
                            }
                        } else {
                            dialog(true, SharedPrefs.HS_HARD_2, trash, trash); break;
                        }
                    }
                } else {
                    dialog(true, SharedPrefs.HS_HARD_1, trash, trash); break;
                }
        }

    }

    public void dialog(boolean newHighScore, String which, String ds1, String ds2) {
        if (newHighScore) { // display dialog where name is entered

        } else { // display dialog without name entry

        }

    }

    public void updatePrefs(String newscore, String which, String ds1, String ds2) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        String a = prefs.getString(which, "");
        String b = prefs.getString(ds1, "");
        editor.putString(which, newscore);
        if (ds1!="" && a!="") editor.putString(ds1, a);
        if (ds2!="" && b!="") editor.putString(ds2, b);
        editor.apply();
    }


    public int scoreFromString(String s) {
        int minutes = Integer.parseInt(s.substring(0,2));
        int seconds = Integer.parseInt(s.substring(3));
        int score = 60*minutes+seconds;
        return score;
    }
}
