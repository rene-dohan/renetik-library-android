package renetik.android.controller.common

import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import renetik.android.base.CSApplication.Companion.application
import renetik.android.base.CSLayoutRes.Companion.layout
import renetik.android.controller.R
import renetik.android.controller.base.CSViewController
import renetik.android.controller.extensions.*
import renetik.android.view.extensions.gone
import renetik.android.view.extensions.text

class CSDebugTextController(val parent: CSViewController<out ViewGroup>,
                            val title: String = "Application Log",
                            val debugText: String) :
    CSViewController<View>(parent, layout(R.layout.cs_log_panel)), CSNavigationItem {

    private val logText = textView(R.id.CSLog_LogText)

    init {
        menuItem("Send to developer").onClick { onSendLogClick() }.alwaysAsAction()
        menuItem("Scroll to bottom")
            .onClick { scrollView(R.id.CSLog_TextScroll).scrollToBottom() }.neverAsAction()
    }

    override fun onCreate() {
        super.onCreate()
        textView(R.id.CSLog_Title).text(title)
        floatingButton(R.id.CSLog_Reload).gone()
        logText.text(debugText)
    }

    private fun onSendLogClick() {
        dialog("Send application log", "Enter target email")
            .showInput("Target email", application.store.getString(sendLogMailKey, "")) { dialog ->
                application.store.save(sendLogMailKey, dialog.inputText)
                sendMail(dialog.inputText,
                    application.name +
                            " This is log from application sent as email attachment for application developer",
                    logText.text())
            }
    }
}

private fun TextView.scrollToBottom() {
    post { scrollTo(0, bottom) }
}

private fun ScrollView.scrollToBottom() {
    post { fullScroll(View.FOCUS_DOWN) }
}
