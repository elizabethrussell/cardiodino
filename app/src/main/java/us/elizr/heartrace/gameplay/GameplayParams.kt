package us.elizr.heartrace.gameplay

/**
 * Created by elizabethrussell on 3/17/18.
 */
class GameplayParams {
    companion object {
        const val FUDGE_LIMIT = 8       // on average, how much does hr increase or decrease.  6 is easy, 10 is hard
        const val LOW_HANDICAP = 3      // it is harder to decrease hr so adjust target bounds for this
        const val TOLERANCE = 3         // potential deviation from previous
    }
}