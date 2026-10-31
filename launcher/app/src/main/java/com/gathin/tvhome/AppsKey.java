package com.gathin.tvhome;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * The remote's Apps button (KEYCODE_ALL_APPS; the 9-dot key on the Mi TV Stick remote, whose Home key sends HOME)
 * opens an app picked in the More menu instead of Home. An empty pick keeps Home; {@link #NONE} makes the button do
 * nothing.
 */
final class AppsKey {
    private static final String KEY = "apps_key_app";
    /** The pick that makes the button do nothing. */
    static final String NONE = "-";

    private AppsKey() {
    }

    /** The picked package, config.xml apps_key_default until one is picked; "" for Home. */
    static String app(Context context) {
        return context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE)
                .getString(KEY, context.getString(R.string.apps_key_default));
    }

    static void set(Context context, String pkg) {
        context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE).edit().putString(KEY, pkg).apply();
    }

    static Intent intent(Context context, String pkg) {
        if (pkg.isEmpty() || NONE.equals(pkg)) return null;
        PackageManager pm = context.getPackageManager();
        Intent open = pm.getLeanbackLaunchIntentForPackage(pkg);
        return open != null ? open : pm.getLaunchIntentForPackage(pkg);
    }

    /**
     * Opens the picked app, or does nothing for {@link #NONE}; false when there is none to open (Home picked, or the
     * app is gone or disabled).
     */
    static boolean launch(Context context) {
        String pkg = app(context);
        if (NONE.equals(pkg)) return true;
        Intent open = intent(context, pkg);
        if (open == null) return false;
        try {
            context.startActivity(open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            return false;
        }
    }
}
