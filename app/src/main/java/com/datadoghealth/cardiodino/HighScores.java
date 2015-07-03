package com.datadoghealth.cardiodino;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Elizabeth on 7/3/2015.
 */
public class HighScores extends Activity {
    private TextView easy;
    private TextView medium;
    private TextView hard;

    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_high_scores);

        easy = (TextView)findViewById(R.id.high_easy);
        medium = (TextView)findViewById(R.id.high_medium);
        hard = (TextView)findViewById(R.id.high_hard);

        easy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easy.setTextColor(getResources().getColor(R.color.red_heartrace));
                medium.setTextColor(getResources().getColor(R.color.gray_heartrace));
                hard.setTextColor(getResources().getColor(R.color.gray_heartrace));
            }
        });

        medium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easy.setTextColor(getResources().getColor(R.color.gray_heartrace));
                medium.setTextColor(getResources().getColor(R.color.red_heartrace));
                hard.setTextColor(getResources().getColor(R.color.gray_heartrace));
            }
        });

        hard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easy.setTextColor(getResources().getColor(R.color.gray_heartrace));
                medium.setTextColor(getResources().getColor(R.color.gray_heartrace));
                hard.setTextColor(getResources().getColor(R.color.red_heartrace));
            }
        });
    }
}
