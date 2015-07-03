package com.datadoghealth.cardiodino;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by root on 7/1/15.
 */
public class Levels extends Activity {
    public static final String EXTRA_LEVEL = "extra_level";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_mode);

        // init textviews with their fonts
        TextView title = (TextView) findViewById(R.id.levels_text);
        Typeface font = Typeface.createFromAsset(getAssets(), "SigmarOne.ttf");
        title.setTypeface(font);

        final Context c = this;

        // set up buttons
        ImageView imEasy = (ImageView)findViewById(R.id.mode_easy);
        imEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c, Intro.class);
                intent.putExtra(EXTRA_LEVEL, 1);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
            }
        });

        ImageView imMedium = (ImageView)findViewById(R.id.mode_medium);
        imMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c, Intro.class);
                intent.putExtra(EXTRA_LEVEL, 2);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
            }
        });

        ImageView imHard = (ImageView)findViewById(R.id.mode_hard);
        imHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c, Intro.class);
                intent.putExtra(EXTRA_LEVEL, 3);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_in, R.anim.left_slide_out);
            }
        });


    }
}
