package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.provider.Settings;
import android.widget.Toast;

import java.net.Inet4Address;

/**
 * The More menu's ADB row: network debugging on or off through Settings.Global.ADB_ENABLED, which needs
 * WRITE_SECURE_SETTINGS granted over ADB once, and the Info dialog with the TV's own address. On the Mi TV
 * Stick switching it on opens port 5555 again; closing it fully there also takes the Developer options master switch
 * and a reboot.
 */
final class AdbSwitch {
    private AdbSwitch() {
    }

    static boolean isOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0) != 0;
    }

    /** Flips ADB; false (with a toast naming the one-time grant) when the permission is missing. */
    static boolean toggle(Context context) {
        try {
            Settings.Global.putInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, isOn(context) ? 0 : 1);
            return true;
        } catch (SecurityException e) {
            Toast.makeText(context, R.string.adb_no_permission, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    static void showInfo(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.adb_info_title)
                .setMessage(activity.getString(R.string.adb_info_text, address(activity)))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /** The TV's IPv4 address on the network it is on; a placeholder when there is none. */
    private static String address(Context context) {
        ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
        Network net = cm != null ? cm.getActiveNetwork() : null;
        LinkProperties link = net != null ? cm.getLinkProperties(net) : null;
        if (link != null) {
            for (LinkAddress a : link.getLinkAddresses()) {
                if (a.getAddress() instanceof Inet4Address) return a.getAddress().getHostAddress();
            }
        }
        return "<tv ip>";
    }
}
