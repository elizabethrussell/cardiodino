package us.elizr.heartrace.gameplay

/**
 * Created by elizabethrussell on 3/17/18.
 */
interface GameplayViewInterface {
    fun updateBallPosition(hr: Int, reference: Int)
    fun updateTargetPosition(targetHr: Int, reference: Int)
}