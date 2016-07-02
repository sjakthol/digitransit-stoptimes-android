package io.github.sjakthol.stoptimes.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import io.github.sjakthol.stoptimes.R;

import java.util.Locale;

/**
 * A random collection of helper methods.
 */
public class Helpers {
    private static final String TAG = Helpers.class.getSimpleName();

    /**
     * Create a new Snackbar with a retry button.
     *
     * @param view a view for the Snackbar
     * @param msg the message to show
     * @param handler the event handler for the retry event
     * @return a new Snackbar
     */
    public static Snackbar snackbarWithRetry(View view, @StringRes int msg, View.OnClickListener handler) {
        Snackbar sb = Snackbar.make(view, msg, Snackbar.LENGTH_INDEFINITE);
        sb.setAction(R.string.retry, handler);
        return sb;
    }

    /**
     * Check if the given permission has been granted to the application.
     *
     * @param context the application context
     * @param permission the permission to check
     *
     * @return true if permission has been granted, false otherwise
     */
    public static boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if location tracking is enabled.
     *
     * @return true if location should be used, false otherwise
     */
    public static boolean shouldTrackLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_key_use_location), false);
    }

    /**
     * Disable location in the app if the setting is enabled but no permission is given.
     *
     * @param context the application context
     */
    public static void maybeDisableUseLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_key_use_location);

        boolean isEnabled = shouldTrackLocation(context);
        boolean hasPermission = Helpers.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

        if (isEnabled && !hasPermission) {
            Logger.i(TAG, "Location access revoked; disabling location usage");
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(key, false);
            editor.apply();
        }
    }

    /**
     * Format the given distance in meters to a human readable format.
     *
     * @param meters the distance to format
     *
     * @return the formatted distance
     */
    public static String formatDistance(double meters) {
        long rounded = Math.round(meters);
        if (rounded < 1000) {
            return String.format(Locale.getDefault(), "%dm", rounded);
        }

        double km = meters / 1000.0;
        if (km < 1000) {
            return String.format(Locale.getDefault(), "%.1fkm", km);
        }

        return String.format(Locale.getDefault(), "%dkm", Math.round(km));
    }

    /**
     * Check if we are inside a JUnit unit test.
     *
     * @return true if testing, false if not
     */
    public static boolean isInJUnitTest() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement frame : stack) {
            if (frame.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the device is connected to the internet.
     *
     * @param context a Context reference to get a ConnectivityManager
     * @return true if connected, false if not
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            // Probably not going to happen.
            Logger.w("NetUtil", "Could not get ConnectivityManager");
            return false;
        }

        // Get information about the active network.
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        return info != null && info.isConnected();
    }
}
