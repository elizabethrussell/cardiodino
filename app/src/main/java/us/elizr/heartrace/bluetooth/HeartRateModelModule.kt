package us.elizr.heartrace.bluetooth

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by elizabethrussell on 3/19/18.
 */
@Module
class HeartRateModelModule {
    @Provides
    @Singleton
    internal fun provideHrModel(): HeartRateModel {
        return HeartRateModel()
    }
}