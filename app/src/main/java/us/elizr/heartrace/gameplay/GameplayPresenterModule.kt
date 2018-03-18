package us.elizr.heartrace.gameplay

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by elizabethrussell on 3/17/18.
 */
@Module
class GameplayPresenterModule {
    @Provides
    @Singleton
    internal fun provideGameplayPresenter(): GameplayPresenter {
        return GameplayPresenter()
    }
}