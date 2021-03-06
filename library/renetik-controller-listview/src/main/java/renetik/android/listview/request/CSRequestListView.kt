package renetik.android.listview.request

import android.widget.AbsListView
import renetik.android.client.request.CSListServerData
import renetik.android.client.request.CSOperation
import renetik.android.controller.base.CSActivityView
import renetik.android.json.data.CSJsonMap
import renetik.android.listview.CSListView
import renetik.android.listview.CSRowView

open class CSRequestListView<RowType : CSJsonMap, ViewType : AbsListView>
    : CSListView<RowType, ViewType> {

    var onReload: ((progress: Boolean) -> CSOperation<CSListServerData<RowType>>)? = null

    constructor(parent: CSActivityView<*>, view: ViewType,
                createView: (CSListView<RowType, ViewType>).(Int) -> CSRowView<RowType>)
            : super(parent, view, createView)

    constructor(parent: CSActivityView<*>, listViewId: Int,
                createView: (CSListView<RowType, ViewType>).(Int) -> CSRowView<RowType>)
            : super(parent, listViewId, createView)

    fun reload(progress: Boolean) = onReload!!(progress).onSuccess { reload(it.list) }

    fun onReload(function: (progress: Boolean) -> CSOperation<CSListServerData<RowType>>) =
        apply { onReload = function }
}