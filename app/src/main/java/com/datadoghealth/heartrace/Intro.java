package com.datadoghealth.heartrace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Elizabeth on 7/2/2015.
 */
public class Intro extends Activity {
    public final int[] targetNums = new int[]{6, 10, 16};
    private int lvl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // prep view components
        Typeface font = Typeface.createFromAsset(getAssets(), "SigmarOne.ttf");
        Intent intent = getIntent();
        lvl = intent.getIntExtra(Levels.EXTRA_LEVEL, 0);
        int numTargets = targetNums[lvl-1];
        TextView targetView = (TextView)findViewById(R.id.intro_number_targets);
        targetView.setText(String.valueOf(numTargets));
        //targetView.setTypeface(font);
        ImageView im = (ImageView)findViewById(R.id.intro_image);
        im.setColorFilter(getResources().getColor(R.color.red_heartrace));
        TextView goView = (TextView)findViewById(R.id.intro_4);
        goView.setTypeface(font);
        final Context c = this;
        goView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(c, AStart.class);
                intent.putExtra(Levels.EXTRA_LEVEL,lvl);
                startActivity(intent);
            }
        });

    }
}
