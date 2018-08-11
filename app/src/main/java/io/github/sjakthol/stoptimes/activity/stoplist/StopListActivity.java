package io.github.sjakthol.stoptimes.activity.stoplist;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.activity.generic.LoadingFragment;
import io.github.sjakthol.stoptimes.activity.settings.SettingsActivity;
import io.github.sjakthol.stoptimes.activity.update.StopDatabaseUpdateActivity;
import io.github.sjakthol.stoptimes.db.task.GetFavoriteStopsTask;
import io.github.sjakthol.stoptimes.db.task.GetNearbyStopsTask;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import io.github.sjakthol.stoptimes.utils.Helpers;
import io.github.sjakthol.stoptimes.utils.Logger;

/**
 * An activity that displays a list of stops.
 */
public class StopListActivity
    extends StopListActivityBase implements OnMenuTabClickListener {
    private static final String TAG = StopListActivity.class.getSimpleName();
    private static final String BUNDLE_STOP_LIST_SOURCE = "BUNDLE_STOP_LIST_SOURCE";
    private BottomBar mBottomBar;

    /**
     * List of possible sources for the stop list in this activity.
     * - FAVORITES: The list of favorite stops
     */
    private enum StopListSource {
        FAVORITES, NEARBY,
    }

    /**
     * The source for the stop list (FAVORITES).
     */
    private StopListSource mStopListSource = StopListSource.FAVORITES;

    /**
     * Creates a new activity and passes the correct container element to the parent class.
     */
    public StopListActivity() {
        super(R.id.stop_list_content);
    }

    /**
     * Create a new stop list activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "Creating new StopListActivity");

        // Setup the bottom bar
        setupBottomBar(savedInstanceState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Logger.d(TAG, "onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.menu_stop_search, menu);

        MenuItem search = menu.findItem(R.id.menu_search);
        final SearchView view = (SearchView) search.getActionView();
        view.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.i(TAG, "Starting search");
                view.setIconified(true);
                startActivity(new Intent(getBaseContext(), StopSearchActivity.class));
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mStopListSource = (StopListSource)
            savedInstanceState.getSerializable(BUNDLE_STOP_LIST_SOURCE);
    }

    /**
     * Save the current list source.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(BUNDLE_STOP_LIST_SOURCE, mStopListSource);
        if (mBottomBar != null) {
            mBottomBar.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume()");

        if (shouldDownloadDatabase()) {
            Logger.i(TAG, "Downloading stop database");
            startActivity(new Intent(this, StopDatabaseUpdateActivity.class));
            return;
        }

        if (!Helpers.shouldTrackLocation(this)) {
            mStopListSource = StopListSource.FAVORITES;
            selectFavoritesTab();
        }

        updateStopListFragment();
    }

    private void updateStopListFragment() {
        Logger.i(TAG, "Updating stopListFragment with source='%s'", mStopListSource);

        switch (mStopListSource) {
            case FAVORITES:
                new GetFavoriteStopsTask(mDatabaseHelper) {
                    @Override
                    protected void onPostExecute(AsyncTaskResult<Cursor> res) {
                        super.onPostExecute(res);
                        handleDatabaseQueryTaskResult(res);
                    }
                }.execute(getShowCitybikeStations());
                break;
            case NEARBY:
                if (getLocation() == null) {
                    showAcquiringLocationMessage();
                } else {
                    updateNearbyStops(getLocation());
                }
        }
    }

    @Override
    public void onLocationUpdated(Location location) {
        super.onLocationUpdated(location);

        if (mStopListSource == StopListSource.NEARBY) {
            updateNearbyStops(location);
        }
    }

    private void updateNearbyStops(Location location) {
        new GetNearbyStopsTask(mDatabaseHelper) {
            @Override
            protected void onPostExecute(AsyncTaskResult<Cursor> res) {
                super.onPostExecute(res);
                handleDatabaseQueryTaskResult(res);
            }
        }.execute(location, getNumStops(), getShowCitybikeStations());
    }

    private void showAcquiringLocationMessage() {
        String msg = getResources().getString(R.string.acquiring_location);
        setFragment(LoadingFragment.createWithMessage(msg));
    }

    /**
     * Show the empty list message based on the current source.
     */
    void showEmptyListMessage() {
        switch (mStopListSource) {
            case FAVORITES:
                showMessage(
                    R.string.stoplist_empty_favorites_title,
                    R.string.stoplist_empty_favorites_description
                );
        }
    }

    /**
     * Check if the stop list database needs to be downloaded.
     *
     * @return true if download is required, false otherwise
     */
    private boolean shouldDownloadDatabase() {
        String key = getResources().getString(R.string.stopdb_last_update);
        long lastUpdate = PreferenceManager
            .getDefaultSharedPreferences(this)
            .getLong(key, -1);

        // Update if update has not been performed
        return lastUpdate == -1;
    }

    /**
     * Sets up the bottom navigation bar. Should be called from onCreate().
     *
     * @param savedInstanceState the saved instance state
     */
    private void setupBottomBar(Bundle savedInstanceState) {
        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItems(R.menu.menu_stoplist_bottom);
        mBottomBar.setOnMenuTabClickListener(this);
    }

    @Override
    public void onMenuTabSelected(@IdRes int menuItemId) {
        if (mIsStopped) {
            // Don't do anything if the activity is not running
            return;
        }

        switch (menuItemId) {
            case R.id.bottom_favorites:
                mStopListSource = StopListSource.FAVORITES;
                break;

            case R.id.bottom_nearby:
                if (!Helpers.shouldTrackLocation(this)) {
                    Logger.i(TAG, "Location disabled; showing message");
                    Helpers.snackbarWithAction(
                        findViewById(R.id.stop_list_content),
                        R.string.location_disabled,
                        R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(getBaseContext(), SettingsActivity.class));
                            }
                        }
                    ).show();

                    selectFavoritesTab();

                    return;
                }

                if (!getLocationController().state().locationServicesEnabled()) {
                    showLocationDisabledDialog();
                }

                mStopListSource = StopListSource.NEARBY;
                break;
        }

        Logger.i(TAG, "Changing stop list source in %s", mStopListSource);
        updateStopListFragment();
    }

    private void selectFavoritesTab() {
        mBottomBar.selectTabAtPosition(0, true);
    }

    @Override
    public void onMenuTabReSelected(@IdRes int menuItemId) {
        // No-op
    }

    /**
     * Show a dialog that informs the user the location services are disabled.
     */
    private void showLocationDisabledDialog() {
        AlertDialog alert = new AlertDialog.Builder(this)
            .setTitle(R.string.location_disabled_dialog_title)
            .setMessage(R.string.location_disabled_dialog_message)
            .setCancelable(false)
            .setNegativeButton(R.string.location_disabled_dialog_stop_using_it, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    Helpers.setUseLocation(StopListActivity.this, false);
                    StopListActivity.this.recreate();
                }
            })
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            }).create();

        alert.show();
    }

    /**
     * Check if citybike stations should be included.
     *
     * @return true / false
     */
    private boolean getShowCitybikeStations() {
        String key = getString(R.string.pref_key_show_citybikes);
        return PreferenceManager
            .getDefaultSharedPreferences(this)
            .getBoolean(key, true);
    }

}
