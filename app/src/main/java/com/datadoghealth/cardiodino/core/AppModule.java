package com.datadoghealth.cardiodino.core;

import android.content.Context;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for dependency injection of global dependencies
 * Created by Elizabeth on 3/23/2015.
 */

@Module(library=true,
        injects={
                AppClass.class,
        })

public class AppModule {
    private final AppClass application;

    AppModule(AppClass application) {this.application=application;}

    @Provides
    @Singleton
    public Context providesApplicationContext() {return application;}



}
