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
                Intent intent = new Intent(c, Game.class);
                startActivity(intent);
            }
        });

        ImageView imMedium = (ImageView)findViewById(R.id.mode_medium);
        imEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c, Game.class);
                startActivity(intent);
            }
        });

        ImageView imHard = (ImageView)findViewById(R.id.mode_hard);
        imEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c, Game.class);
                startActivity(intent);
            }
        });


    }
}
