package us.elizr.heartrace.gameplay

import android.app.Activity
import android.os.Bundle
import java.util.*
import javax.inject.Inject
import android.support.v4.view.ViewCompat.setY
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.activity_gameplay.*
import us.elizr.heartrace.R
import us.elizr.heartrace.gameplay.GameplayParams.Companion.FUDGE_LIMIT
import us.elizr.heartrace.gameplay.GameplayParams.Companion.TOLERANCE
import android.R.attr.x
import android.R.attr.y
import android.graphics.Point
import android.view.Display
import us.elizr.heartrace.core.MyApp
import us.elizr.heartrace.gameplay.GameplayParams.Companion.LOW_HANDICAP


/**
 * Created by elizabethrussell on 3/17/18.
 */
class GameplayView: Activity(), GameplayViewInterface {

    // view layout
    private val VIEW_MARGIN = 200   // dp above and below maximum target

    private var screenHeight: Float = 0f
    private var screenWidth: Int = 0
    private var ballHeight: Float = 0f


    @Inject
    lateinit var presenter: GameplayPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameplay)
        MyApp.appComponent.inject(this)
        presenter.assignView(this)

        // get display dimensions
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenHeight = size.y.toFloat()
        screenWidth = size.x
        ballHeight = getResources().getDimension(R.dimen.ball_height)

        // center ball in screen
        text_gameplay_ball.setX(((screenWidth/2)-(ballHeight/2)).toFloat())
    }

    override fun updateBallPosition(hr: Int, reference: Int) {
        text_gameplay_ball.text = hr.toString()
        val coordinate = hrToYCoordinate(hr, reference)
        text_gameplay_ball.animate().y(coordinate)
    }

    private fun hrToYCoordinate(hr: Int, reference: Int):Float {
        var pos: Float
        val m = (2 * VIEW_MARGIN - screenHeight) / (FUDGE_LIMIT * (1 + 1 / LOW_HANDICAP) + 2 * TOLERANCE)
        val b = VIEW_MARGIN - m * (reference + FUDGE_LIMIT + TOLERANCE)
        pos = (m * hr + b).toFloat()
        if (pos > screenHeight - ballHeight) { // hit bottom screen bound
            pos = (screenHeight - ballHeight)
        } else if (pos < (ballHeight/2)) { // hit top screen bound
            pos = (ballHeight / 2)
        }
        return (pos - ballHeight / 2)
    }

    override fun updateTargetPosition(targetHr: Int, reference: Int) {
        // update target view
        view_gameplay_target.setLayoutParams(RelativeLayout.LayoutParams(screenWidth, 10))
        val coordinate = targetToYCoordinate(targetHr, reference)
        view_gameplay_target.y = coordinate

        // update target text
        text_gameplay_target.text = targetHr.toString()
        text_gameplay_target.y = coordinate - 100
    }

    private fun targetToYCoordinate(hr: Int, reference: Int):Float {
        val m = (2 * VIEW_MARGIN - screenHeight) / (FUDGE_LIMIT * (1 + 1 / LOW_HANDICAP) + 2 * TOLERANCE)
        val b = VIEW_MARGIN - m * (reference + FUDGE_LIMIT + TOLERANCE)
        return (m * hr + b)
    }

}