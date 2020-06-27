package app.ekaralamov.sdr.driver.permissions

import android.content.pm.PackageManager

internal fun PackageManager.resolveAppName(packageName: String) =
    getApplicationInfo(packageName, 0).loadLabel(this)
