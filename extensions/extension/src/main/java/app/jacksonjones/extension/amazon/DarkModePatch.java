package app.jacksonjones.extension.amazon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

@SuppressWarnings({"unused", "deprecation"})
public class DarkModePatch {
    private static final String TAG = "AmazonDarkMode";

    // Must match the style names added by the resource patch.
    private static final String STYLE_WEBVIEW_DARK = "amazon_dark_mode_webview_dark";
    private static final String STYLE_WEBVIEW_LIGHT = "amazon_dark_mode_webview_light";

    private static boolean isNightMode(Configuration configuration) {
        return (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * WebView uses dark mode only if the theme of its context is not a light theme.
     * The app has only light themes, so the theme is changed to match the system dark theme.
     */
    @SuppressLint("DiscouragedApi")
    private static void applyTheme(Context context, boolean nightMode) {
        try {
            int style = context.getResources().getIdentifier(
                    nightMode ? STYLE_WEBVIEW_DARK : STYLE_WEBVIEW_LIGHT,
                    "style",
                    context.getPackageName()
            );
            if (style == 0) {
                Log.e(TAG, "Could not find WebView theme style");
                return;
            }

            context.getTheme().applyStyle(style, true);
        } catch (Exception ex) {
            Log.e(TAG, "applyTheme failure", ex);
        }
    }

    /**
     * Called before the WebView super constructor.
     */
    public static Context prepareContext(Context context) {
        if (context != null) {
            applyTheme(context, isNightMode(context.getResources().getConfiguration()));
        }

        return context;
    }

    public static void applyDarkMode(WebView webView) {
        try {
            applyDarkMode(webView, webView.getContext().getResources().getConfiguration());
        } catch (Exception ex) {
            Log.e(TAG, "applyDarkMode failure", ex);
        }
    }

    public static void applyDarkMode(WebView webView, Configuration configuration) {
        try {
            final boolean nightMode = isNightMode(configuration);
            applyTheme(webView.getContext(), nightMode);

            WebSettings settings = webView.getSettings();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                settings.setAlgorithmicDarkeningAllowed(nightMode);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Has no effect if the app targets Android 13+ but is running on Android 10 - 12.
                settings.setForceDark(nightMode
                        ? WebSettings.FORCE_DARK_ON
                        : WebSettings.FORCE_DARK_OFF);
            }
        } catch (Exception ex) {
            Log.e(TAG, "applyDarkMode failure", ex);
        }
    }
}
