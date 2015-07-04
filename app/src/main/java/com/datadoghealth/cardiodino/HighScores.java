package com.datadoghealth.cardiodino;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import com.datadoghealth.cardiodino.util.SharedPrefs;

/**
 * Created by Elizabeth on 7/3/2015.
 */
public class HighScores extends Activity {
    private TextView easy;
    private TextView medium;
    private TextView hard;

    private TextView hs1;
    private TextView hs2;
    private TextView hs3;

    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_high_scores);

        easy = (TextView)findViewById(R.id.high_easy);
        medium = (TextView)findViewById(R.id.high_medium);
        hard = (TextView)findViewById(R.id.high_hard);

        hs1 = (TextView)findViewById(R.id.high_score_1);
        hs2 = (TextView)findViewById(R.id.high_score_2);
        hs3 = (TextView)findViewById(R.id.high_score_3);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        hs1.setText(stringScoreFormat(prefs.getString(SharedPrefs.HS_EASY_1, "")));
        hs2.setText(stringScoreFormat(prefs.getString(SharedPrefs.HS_EASY_2, "")));
        hs3.setText(stringScoreFormat(prefs.getString(SharedPrefs.HS_EASY_3, "")));

        easy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easy.setTextColor(getResources().getColor(R.color.red_heartrace));
                medium.setTextColor(getResources().getColor(R.color.gray_heartrace));
                hard.setTextColor(getResources().getColor(R.color.gray_heartrace));

                hs1.setText(stringScoreFormat(prefs.getString(SharedPrefs.HS_EASY_1, "")));
                hs2.setText(stringScoreFormat(prefs.getString(SharedPrefs.HS_EASY_2, "")));
                hs3.setText(stringScoreFormat(prefs.getString(SharedPrefs.HS_EASY_3, "")));

            }
        });

        medium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easy.setTextColor(getResources().getColor(R.color.gray_heartrace));
                medium.setTextColor(getResources().getColor(R.color.red_heartrace));
                hard.setTextColor(getResources().getColor(R.color.gray_heartrace));

                hs1.setText(stringScoreFormat(prefs.getString(SharedPrefs.HS_MEDI_1, "")));
                hs2.setText(stringScoreFormat(prefs.getString(SharedPrefs.HS_MEDI_2, "")));
                hs3.setText(stringScoreFormat(prefs.getString(SharedPrefs.HS_MEDI_3, "")));
            }
        });

        hard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easy.setTextColor(getResources().getColor(R.color.gray_heartrace));
                medium.setTextColor(getResources().getColor(R.color.gray_heartrace));
                hard.setTextColor(getResources().getColor(R.color.red_heartrace));

                hs1.setText(stringScoreFormat(prefs.getString(SharedPrefs.HS_HARD_1, "")));
                hs2.setText(stringScoreFormat(prefs.getString(SharedPrefs.HS_HARD_2, "")));
                hs3.setText(stringScoreFormat(prefs.getString(SharedPrefs.HS_HARD_3, "")));
            }
        });
    }

    public String stringScoreFormat(String s) {
        String[] ss;
        if (s.isEmpty()) {
            ss = new String[] {"??","??:??"};
        } else {
            ss = s.split("|");
        }
        String s2 = ss[1]+"        "+ss[0];
        return s2;
    }
}
