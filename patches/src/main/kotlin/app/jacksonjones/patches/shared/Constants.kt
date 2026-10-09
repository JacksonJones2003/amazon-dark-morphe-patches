package app.jacksonjones.patches.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_AMAZON_SHOPPING = Compatibility(
        name = "Amazon Shopping",
        packageName = "com.amazon.mShop.android.shopping",
        // Amazon smile orange.
        appIconColor = 0xFF9900,
        targets = listOf(
            // The patch does not rely on any obfuscated or version specific code,
            // so it is expected to apply to any app version.
            AppTarget(
                version = null,
                isExperimental = true
            )
        )
    )
}
