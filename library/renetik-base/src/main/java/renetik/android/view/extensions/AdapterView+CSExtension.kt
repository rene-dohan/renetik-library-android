package renetik.android.view.extensions

import android.os.SystemClock.uptimeMillis
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.obtain
import android.widget.Adapter
import android.widget.AdapterView

fun <T : Adapter> AdapterView<T>.scrollToTop() {
    afterLayout {
        setSelection(0)
        dispatchTouchEvent(obtain(uptimeMillis(), uptimeMillis(), ACTION_CANCEL, 0f, 0f, 0))
    }
}
