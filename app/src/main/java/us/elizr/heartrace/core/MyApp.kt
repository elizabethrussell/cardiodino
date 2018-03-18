package us.elizr.heartrace.core

import android.app.Application
import android.content.Intent
import android.util.Log
import javax.inject.Inject

/**
 * Created by elizabethrussell on 3/16/18.
 */

class MyApp : Application() {


    override fun onCreate() {
        super.onCreate()
        Log.i("MyApp", "onCreate")
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
    }

    companion object {

        @JvmStatic lateinit var appComponent: AppComponent
    }
}