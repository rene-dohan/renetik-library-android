package renetik.android.view

import android.text.method.ScrollingMovementMethod
import android.view.View
import renetik.android.R
import renetik.android.R.id
import renetik.android.extensions.dialog
import renetik.android.extensions.floatingButton
import renetik.android.extensions.sendMail
import renetik.android.extensions.textView
import renetik.android.extensions.view.onClick
import renetik.android.extensions.view.title
import renetik.android.model.application
import renetik.android.view.base.CSViewController
import renetik.android.view.base.layout

const val sendLogMailKey = "send_log_mail"

class CSLogController(val navigation: CSNavigationController) :
        CSViewController<View>(navigation, layout(R.layout.cs_log)), CSNavigationItem {

    private val logText = textView(id.CSLog_LogText).apply { movementMethod = ScrollingMovementMethod() }

    init {
        menu("Send to developer").onClick { onSendLogClick() }
    }

    override fun onCreate() {
        super.onCreate()
        floatingButton(R.id.CSLog_Reload).onClick { loadText() }
        loadText()
    }

    private fun loadText() = logText.title(application.logger.logString())

    private fun onSendLogClick() {
        navigation.dialog("Send application log", "Enter target email")
                .showInput("Target email", application.store.loadString(sendLogMailKey, "")) { dialog ->
                    application.store.put(sendLogMailKey, dialog.inputValue())
                    sendMail(dialog.inputValue(), application.name +
                            " This is log from application sent as email attachment for application developer"
                            , logText.title())
                }
    }
}