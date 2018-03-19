package us.elizr.heartrace.bluetooth
import io.reactivex.*
import io.reactivex.subjects.PublishSubject

/**
 * Created by elizabethrussell on 3/19/18.
 */
class HeartRateModel {
    private var subject: PublishSubject<Int> = PublishSubject.create()

    fun setHr(hr: Int) {
        subject.onNext(hr)
    }

    fun getHr(): Observable<Int> {
        return subject
    }

}