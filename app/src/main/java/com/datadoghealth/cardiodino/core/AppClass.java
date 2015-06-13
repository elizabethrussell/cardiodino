package com.datadoghealth.cardiodino.core;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

/**
 * Basically globals
 *
 * ECR
 */
public class AppClass extends Application implements DaggerInjector {

    public static SharedPreferences sp;
    public static String uid;
    //public static Bus bus;

    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        //PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        uid = ('h' + Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));

        objectGraph = ObjectGraph.create(getModules().toArray());
        //bus = new Bus(ThreadEnforcer.ANY);


    }

    List<Object> getModules() {
        return Arrays.<Object>asList(new AppModule(this));
    }

    @Override
    public void inject(Object object) {
        objectGraph.inject(object);
    }
}


      