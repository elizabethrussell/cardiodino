package us.elizr.heartrace.gameplay

import us.elizr.heartrace.core.MyApp
import us.elizr.heartrace.gameplay.GameplayParams.Companion.TOLERANCE
import us.elizr.heartrace.gameplay.GameplayParams.Companion.FUDGE_LIMIT
import us.elizr.heartrace.gameplay.GameplayParams.Companion.LOW_HANDICAP
import java.util.*

/**
 * Created by elizabethrussell on 3/17/18.
 */
class GameplayPresenter {

    private var view : GameplayViewInterface? = null

    var startingHr: Int = -1
    var targetHr : Int = -1
    private var isHighTarget = false        // target can be high (above starting hr) or low

    val random = Random()
    init {
        MyApp.appComponent.inject(this)
    }

    fun assignView(view: GameplayViewInterface) {
        this.view = view
    }

    fun changeTarget() {
        val tolerance = random.nextInt(TOLERANCE*2+1) - TOLERANCE
        val add =  if (isHighTarget)  FUDGE_LIMIT + tolerance else (-(FUDGE_LIMIT/LOW_HANDICAP)-tolerance)
        targetHr =  (startingHr + add)
        isHighTarget = !isHighTarget
    }



}