package app.jacksonjones.patches.amazon.darkmode

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.logging.Logger

// Must match the style names looked up by the extension.
private const val STYLE_WEBVIEW_DARK = "amazon_dark_mode_webview_dark"
private const val STYLE_WEBVIEW_LIGHT = "amazon_dark_mode_webview_light"

private const val ATTR_IS_LIGHT_THEME = "android:isLightTheme"
private const val ATTR_FORCE_DARK_ALLOWED = "android:forceDarkAllowed"

private fun Element.childElements(tagName: String) =
    (0 until childNodes.length)
        .map { childNodes.item(it) }
        .filterIsInstance<Element>()
        .filter { it.tagName == tagName }

private fun Document.createStyleItem(name: String, value: String) =
    createElement("item").apply {
        setAttribute("name", name)
        appendChild(createTextNode(value))
    }

internal fun darkModeResourcePatch(darkenNativeUi: () -> Boolean) = resourcePatch {
    execute {
        val logger = Logger.getLogger("DarkModeResourcePatch")

        // Theme overlays the extension applies to the context of each WebView.
        // WebView only darkens web pages if the theme it lives in declares itself as not light.
        document("res/values/styles.xml").use { document ->
            mapOf(
                STYLE_WEBVIEW_DARK to "false",
                STYLE_WEBVIEW_LIGHT to "true",
            ).forEach { (styleName, isLightTheme) ->
                val style = document.createElement("style").apply {
                    setAttribute("name", styleName)
                    appendChild(document.createStyleItem(ATTR_IS_LIGHT_THEME, isLightTheme))
                }
                document.documentElement.appendChild(style)
            }
        }

        if (!darkenNativeUi()) return@execute

        // Collect the themes used by the app windows.
        val themeNames = mutableSetOf<String>()
        document("AndroidManifest.xml").use { document ->
            listOf("application", "activity", "activity-alias").forEach { tag ->
                val nodes = document.getElementsByTagName(tag)
                for (i in 0 until nodes.length) {
                    val theme = (nodes.item(i) as Element).getAttribute("android:theme")
                    if (theme.startsWith("@style/")) {
                        themeNames += theme.removePrefix("@style/")
                    }
                }
            }
        }

        // Allow Android to automatically darken the native views of these themes
        // while the system dark theme is turned on. A style declared in a qualified
        // values directory replaces the default declaration, so all of them are changed.
        var changedStyles = 0
        get("res").listFiles { file -> file.isDirectory && file.name.startsWith("values") }
            ?.forEach { valuesDirectory ->
                val stylesFile = valuesDirectory.resolve("styles.xml")
                if (!stylesFile.exists()) return@forEach

                document("res/${valuesDirectory.name}/styles.xml").use { document ->
                    document.documentElement.childElements("style")
                        .filter { it.getAttribute("name") in themeNames }
                        .forEach { style ->
                            val items = style.childElements("item")
                            // Do not override an explicit choice made by the app.
                            if (items.any { it.getAttribute("name") == ATTR_FORCE_DARK_ALLOWED }) {
                                return@forEach
                            }

                            style.appendChild(document.createStyleItem(ATTR_FORCE_DARK_ALLOWED, "true"))
                            changedStyles++
                        }
                }
            }

        if (changedStyles == 0) {
            logger.warning("Found no app themes to darken. Only web pages will use dark mode.")
        }
    }
}
