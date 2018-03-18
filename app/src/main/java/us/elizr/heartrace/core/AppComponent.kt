package us.elizr.heartrace.core

import dagger.Component
import us.elizr.heartrace.bluetooth.BLEConnectingView
import us.elizr.heartrace.bluetooth.BLEConnectingPresenter
import us.elizr.heartrace.bluetooth.BLEConnectingPresenterModule
import us.elizr.heartrace.gameplay.GameplayPresenter
import us.elizr.heartrace.gameplay.GameplayPresenterModule
import us.elizr.heartrace.gameplay.GameplayView
import javax.inject.Singleton

/**
 * Created by elizabethrussell on 3/16/18.
 */

@Singleton
@Component(modules = arrayOf(AppModule::class, BLEConnectingPresenterModule::class, GameplayPresenterModule::class))
interface AppComponent {
    fun inject(target: BLEConnectingPresenter)
    fun inject(target: BLEConnectingView)
    fun inject(target: GameplayPresenter)
    fun inject(target: GameplayView)
}
