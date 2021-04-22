package renetik.android.widget

import android.widget.DatePicker
import renetik.android.java.util.calendar
import renetik.android.java.util.dateFrom

val DatePicker.date get() = calendar.dateFrom(year, month, dayOfMonth)!!