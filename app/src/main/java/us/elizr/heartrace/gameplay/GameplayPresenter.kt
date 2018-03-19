package us.elizr.heartrace.gameplay

import android.util.Log
import org.reactivestreams.Subscriber
import us.elizr.heartrace.bluetooth.HeartRateModel
import us.elizr.heartrace.core.MyApp
import us.elizr.heartrace.gameplay.GameplayParams.Companion.TOLERANCE
import us.elizr.heartrace.gameplay.GameplayParams.Companion.FUDGE_LIMIT
import us.elizr.heartrace.gameplay.GameplayParams.Companion.LOW_HANDICAP
import java.util.*
import javax.inject.Inject

/**
 * Created by elizabethrussell on 3/17/18.
 */
class GameplayPresenter {

    private var view : GameplayViewInterface? = null

    @Inject
    lateinit var hrModel: HeartRateModel

    var startingHr: Int = -1
    var targetHr : Int = -1
    private var isHighTarget = false        // target can be high (above starting hr) or low

    val random = Random()
    init {
        Log.i("GameplayPresenter","initializing")
        MyApp.appComponent.inject(this)

        hrModel.getHr().subscribe( {
            hr ->
            handleHr(hr)
        })
    }

    fun assignView(view: GameplayViewInterface) {
        this.view = view
    }

    fun handleHr(hr: Int) {
        Log.i("GameplayPresenter","Handling hr: "+hr + " target: "+targetHr)
        if (hr != 0) {
            // initialize target using first valid hr measurement
            if (targetHr == -1) {
                startingHr = hr
                changeTarget()

            // check against target and update view
            } else {
                if (isHighTarget and (hr >= targetHr)) {
                    changeTarget()
                } else if (!isHighTarget and (hr <= targetHr)) {
                    changeTarget()
                } else {
                    view?.updateBallPosition(hr, startingHr)
                }

            }
        }
    }

    fun changeTarget() {
        isHighTarget = !isHighTarget
        val tolerance = random.nextInt(TOLERANCE*2+1) - TOLERANCE
        val add =  if (isHighTarget)  FUDGE_LIMIT + tolerance else (-(FUDGE_LIMIT/LOW_HANDICAP)-tolerance)
        targetHr =  (startingHr + add)
        view?.updateTargetPosition(targetHr, startingHr)
    }



}