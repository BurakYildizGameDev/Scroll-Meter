package com.scrollmeter.app.service.filter

/**
 * Filter responsible for ignoring system UI, launchers, input methods, and the app itself
 * to ensure non-user navigation does not pollute distance metrics.
 */
object PackageBlacklist {

    private val BLACKLISTED_PACKAGES = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.apps.nexuslauncher",
        "com.android.launcher",
        "com.android.launcher3",
        "com.miui.home",
        "com.mi.android.globallauncher",
        "com.sec.android.app.launcher",
        "com.oppo.launcher",
        "com.huawei.android.launcher",
        "com.oneplus.launcher",
        "com.google.android.inputmethod.latin",
        "com.samsung.android.honeyboard",
        "com.android.settings",
        "com.google.android.packageinstaller",
        "com.android.packageinstaller",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.google.android.feedback",
        "com.google.android.backuptransport",
        "com.google.android.permissioncontroller",
        "com.android.permissioncontroller",
        "com.android.phone",
        "com.android.server.telecom",
        "com.miui.securitycenter",
        "com.miui.cleanmaster",
        "com.miui.powerkeeper",
        "com.miui.securityadd",
        "com.miui.guardprovider",
        "com.scrollmeter.app" // Exclude Scroll-Meter itself
    )

    /**
     * Returns true if the package should be ignored from scroll measurement.
     */
    fun isBlacklisted(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return true
        val lower = packageName.trim().lowercase()
        if (lower == "android" || lower.startsWith("android.")) return true
        if (lower in BLACKLISTED_PACKAGES) return true
        if (lower.startsWith("com.android.systemui")) return true
        if (lower.startsWith("com.android.providers.")) return true
        if (lower.startsWith("com.android.server.")) return true
        if (lower.startsWith("com.android.internal.")) return true
        if (lower.startsWith("com.google.android.gms")) return true
        if (lower.startsWith("com.google.android.gsf")) return true
        if (lower.startsWith("com.google.android.overlay.")) return true
        if (lower.startsWith("com.google.android.ext.")) return true
        if (lower.contains("launcher") || lower.contains("keyguard")) return true
        return false
    }
}
