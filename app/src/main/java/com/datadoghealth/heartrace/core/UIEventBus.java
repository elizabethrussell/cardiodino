package com.datadoghealth.heartrace.core;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Send object from anywhere -> post to UI thread
 * Created by Elizabeth on 3/23/2015.
 */
public class UIEventBus extends Bus {
    private final Handler uiThread = new Handler(Looper.getMainLooper());

    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
        } else {
            uiThread.post(new Runnable() {
                @Override
                public void run() {
                    post(event);
                }
            });
        }
    }
}
