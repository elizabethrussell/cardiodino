package us.elizr.heartrace.bluetooth

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by elizabethrussell on 3/16/18.
 */
@Module
class BLEConnectingPresenterModule {
    @Provides
    @Singleton
    internal fun provideBlePresenter(): BLEConnectingPresenter {
        return BLEConnectingPresenter()
    }
}