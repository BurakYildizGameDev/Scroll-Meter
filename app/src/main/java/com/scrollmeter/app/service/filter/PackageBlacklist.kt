package com.scrollmeter.app.service.filter

/**
 * Filter responsible for ignoring system UI, launchers, input methods, and the app itself
 * to ensure non-user navigation does not pollute distance metrics.
 */
object PackageBlacklist {

    private val BLACKLISTED_PACKAGES = setOf(
        "com.android.systemui",
        "com.google.android.apps.nexuslauncher",
        "com.android.launcher",
        "com.android.launcher3",
        "com.miui.home",
        "com.sec.android.app.launcher",
        "com.oppo.launcher",
        "com.huawei.android.launcher",
        "com.oneplus.launcher",
        "com.google.android.inputmethod.latin",
        "com.samsung.android.honeyboard",
        "com.android.settings",
        "com.google.android.packageinstaller",
        "com.android.packageinstaller",
        "com.scrollmeter.app" // Exclude Scroll-Meter itself
    )

    /**
     * Returns true if the package should be ignored from scroll measurement.
     */
    fun isBlacklisted(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return true
        return packageName in BLACKLISTED_PACKAGES
    }
}
