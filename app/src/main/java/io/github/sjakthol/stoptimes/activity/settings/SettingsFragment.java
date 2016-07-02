package io.github.sjakthol.stoptimes.activity.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.utils.Logger;

@SuppressWarnings("WeakerAccess")
public class SettingsFragment extends PreferenceFragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate()");

        addPreferencesFromResource(R.xml.preferences);
    }
}
