package renetik.android.controller.extensions

import android.os.Build.VERSION.SDK_INT
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import renetik.android.controller.base.CSActivityView
import renetik.android.java.extensions.collections.list
import renetik.android.framework.math.CSMath.randomInt
import renetik.android.primitives.isSet

fun CSActivityView<*>.requestPermissions(permissions: List<String>, onGranted: () -> Unit) {
    requestPermissions(permissions, onGranted, null)
}

fun CSActivityView<*>.requestPermissionsWithForce(permissions: List<String>, onGranted: () -> Unit) {
    requestPermissions(permissions, onGranted, { requestPermissionsWithForce(permissions, onGranted) })
}

fun CSActivityView<*>.requestPermissions(
    permissions: List<String>,
    onGranted: (() -> Unit)? = null, onNotGranted: (() -> Unit)? = null
) {
    if (SDK_INT < 23) {
        onGranted?.invoke()
        return
    }
    val deniedPermissions = getDeniedPermissions(permissions)
    if (deniedPermissions.isSet) {
        val requestCode = randomInt(0, 999)
        ActivityCompat.requestPermissions(activity(), deniedPermissions, requestCode)
        onRequestPermissionsResult.add { registration, results ->
            if (results.requestCode == requestCode) {
                registration.cancel()
                for (status in results.statuses) if (PERMISSION_GRANTED != status) {
                    onNotGranted?.invoke()
                    return@add
                }
                onGranted?.invoke()
            }
        }
    } else onGranted?.invoke()
}

fun CSActivityView<*>.getDeniedPermissions(permissions: List<String>): Array<String> {
    val deniedPermissions = list<String>()
    for (permission in permissions)
        if (isPermissionGranted(permission)) deniedPermissions.add(permission)
    return deniedPermissions.toTypedArray()
}

fun CSActivityView<*>.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED
}

