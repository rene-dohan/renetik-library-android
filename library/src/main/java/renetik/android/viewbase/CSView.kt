package renetik.android.viewbase

import android.content.Context
import android.view.LayoutInflater.from
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import renetik.android.extensions.notNull
import renetik.android.extensions.view.fade
import renetik.android.extensions.view.hide
import renetik.android.extensions.view.show
import renetik.android.lang.CSLang.NO
import renetik.android.view.list.CSListController

open class CSView<ViewType : View>(context: Context) : CSContextController(context) {

    private var parentGroup: ViewGroup? = null
    private var layoutId: CSLayoutId? = null
    private var viewId: Int? = null
    val view: ViewType by lazy {
        val view = layoutId?.let { inflate(it.id) } ?: let { createView()!! }
        view.tag = this
        view
    }

    fun inflate(layoutId: Int): ViewType {
        return (if (parentGroup.notNull) from(this).inflate(layoutId, parentGroup, NO)
        else from(this).inflate(layoutId, null)) as ViewType
    }

    protected open fun createView(): ViewType? = null

    constructor(parent: Context, layoutId: CSLayoutId? = null) : this(parent) {
        this.layoutId = layoutId
    }

    constructor(parent: ViewGroup, layoutId: CSLayoutId) : this(parent.context, layoutId) {
        this.parentGroup = parent
    }

    constructor(parent: CSListController<*, *>, layoutId: CSLayoutId)
            : this(parent.view as ViewGroup, layoutId)

    constructor(parent: CSView<*>, viewId: Int) : this(parent) {
        this.viewId = viewId
    }

    fun fade(fadeIn: Boolean) = view.fade(fadeIn)
    open fun show(): CSView<ViewType> = apply { view.show() }
    open fun hide(): CSView<ViewType> = apply { view.hide() }
    val hasParent get() = view.parent.notNull

    fun hideKeyboard() {
        var view = rootActivity!!.getCurrentFocus() ?: view
        service<InputMethodManager>(Context.INPUT_METHOD_SERVICE)
                .hideSoftInputFromWindow(view.rootView.windowToken, 0)
    }

    fun showKeyboard(view: View, flag: Int) =
            service<InputMethodManager>(Context.INPUT_METHOD_SERVICE).showSoftInput(view, flag)

    override fun onDestroy() {
        super.onDestroy()
        view.tag = null
    }
}