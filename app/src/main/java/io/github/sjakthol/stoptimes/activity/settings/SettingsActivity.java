package io.github.sjakthol.stoptimes.activity.settings;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.activity.BaseActivity;
import io.github.sjakthol.stoptimes.utils.Helpers;
import io.github.sjakthol.stoptimes.utils.Logger;

public class SettingsActivity
        extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
                   ActivityCompat.OnRequestPermissionsResultCallback
{
    private static final String TAG = SettingsActivity.class.getSimpleName();

    public SettingsActivity() {
        super(R.id.main_content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        setToolbar();

        // Display the fragment as the main content.
        replaceSettingsFragment();

        // Listen for changes so that we can request permissions on the spot.
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Logger.d(TAG, "Pref %s changed", key);

        if (key.equals(getString(R.string.pref_key_use_location)) && sharedPreferences.getBoolean(key, false)) {
            requestLocationPermissions();
        }
    }

    /**
     * Replaces the activity contents with the SettingsFragment.
     */
    private void replaceSettingsFragment() {
        getFragmentManager().beginTransaction()
            .replace(R.id.main_content, new SettingsFragment())
            .commit();
    }

    private void requestLocationPermissions() {
        Logger.i(TAG, "requestLocationPermissions(): Checking if permission is provided");

        if (Helpers.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Logger.i(TAG, "Location access already provided.");
            return;
        }

        Logger.i(TAG, "Permission not provided; prompting");
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 0) {
            Logger.w(TAG, "Unexpected request code %d", requestCode);
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Logger.i(TAG, "Access to location granted.");
        } else {
            Logger.w(TAG, "Access to location denied; reverting location setting");

            // No access; disable the use_location setting
            Helpers.maybeDisableUseLocation(this);

            // To refresh the settings UI
            replaceSettingsFragment();
        }
    }
}
