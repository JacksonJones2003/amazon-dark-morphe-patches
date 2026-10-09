package app.jacksonjones.extension.amazon;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Replaces every WebView of the app, including the super class of app classes that extend WebView.
 * The constructors must match all public constructors of WebView.
 */
@SuppressWarnings({"unused", "deprecation"})
public class DarkWebView extends WebView {

    public DarkWebView(Context context) {
        super(DarkModePatch.prepareContext(context));
        DarkModePatch.applyDarkMode(this);
    }

    public DarkWebView(Context context, AttributeSet attrs) {
        super(DarkModePatch.prepareContext(context), attrs);
        DarkModePatch.applyDarkMode(this);
    }

    public DarkWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(DarkModePatch.prepareContext(context), attrs, defStyleAttr);
        DarkModePatch.applyDarkMode(this);
    }

    public DarkWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(DarkModePatch.prepareContext(context), attrs, defStyleAttr, defStyleRes);
        DarkModePatch.applyDarkMode(this);
    }

    public DarkWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(DarkModePatch.prepareContext(context), attrs, defStyleAttr, privateBrowsing);
        DarkModePatch.applyDarkMode(this);
    }

    @Override
    protected void onAttachedToWindow() {
        // The WebView can be created with one context and later be moved to another.
        DarkModePatch.applyDarkMode(this);
        super.onAttachedToWindow();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        // Update before WebView reads the theme again.
        DarkModePatch.applyDarkMode(this, newConfig);
        super.onConfigurationChanged(newConfig);
    }
}
