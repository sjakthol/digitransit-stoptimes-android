package io.github.sjakthol.stoptimes.activity.departures;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import com.android.volley.*;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.activity.BaseActivity;
import io.github.sjakthol.stoptimes.activity.generic.LoadingFragment;
import io.github.sjakthol.stoptimes.activity.generic.NoConnectionFragment;
import io.github.sjakthol.stoptimes.activity.generic.UnexpectedErrorFragment;
import io.github.sjakthol.stoptimes.digitransit.DigitransitApi;
import io.github.sjakthol.stoptimes.digitransit.models.Departure;
import io.github.sjakthol.stoptimes.utils.VolleyWrapper;
import io.github.sjakthol.stoptimes.utils.Helpers;
import io.github.sjakthol.stoptimes.utils.Logger;

import java.util.Vector;

public class DepartureListActivity
        extends BaseActivity
        implements  NoConnectionFragment.OnConnectionAvailable,
                    DepartureListFragment.Hooks,
                    DigitransitApi.DepartureResponseListener
{
    private static final String TAG = DepartureListActivity.class.getSimpleName();
    private static final String FRAG_DEPARTURE_LIST = "FRAG_DEPARTURE_LIST";

    public static final String EXTRA_STOP_ID = "EXTRA_STOP_ID";
    public static final String EXTRA_STOP_NAME = "EXTRA_STOP_NAME";

    /**
     * The ID of the stop shown in this activity.
     */
    private String mStopId;

    /**
     * A flag used to determine if the view is already showing departures. Affects
     * the error handling of departure updates.
     */
    private boolean mHasLoadedDepartures;

    public DepartureListActivity() {
        super(R.id.departure_list_content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate()");

        setContentView(R.layout.activity_stop_departures);
        setToolbar(R.id.toolbar);

        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle(getIntent().getStringExtra(EXTRA_STOP_NAME));
            toolbar.setDisplayHomeAsUpEnabled(true);
        }

        mStopId = getIntent().getStringExtra(EXTRA_STOP_ID);
        mHasLoadedDepartures = false;

        // Update will be triggered by onResume()
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume()");

        updateDepartures();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Logger.d(TAG, "onOptionsItemSelected()");
        switch (item.getItemId()) {
            case android.R.id.home:
                // Treat the home as back
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.d(TAG, "onStop()");

        VolleyWrapper.getInstance(this).getRequestQueue().cancelAll(this);
    }

    @Override
    public void onConnectionAvailable() {
        Logger.i(TAG, "Got network connection; triggering departure update");
        updateDepartures();
    }

    /** Digitransit API hooks **/
    @Override
    public void onDeparturesAvailable(Vector<Departure> departures) {
        DepartureListFragment frag = getDepartureList();
        if (frag == null) {
            Logger.i(TAG, "No departure list found; creating new one");
            frag = DepartureListFragment.withDepartures(departures);
            setFragment(frag, FRAG_DEPARTURE_LIST);
        } else {
            Logger.i(TAG, "Updating existing fragment");
            frag.setDepartureList(departures);
            frag.updateFinished();
        }

        mHasLoadedDepartures = true;
    }

    @Override
    public void onDepartureLoadError(VolleyError error) {
        Logger.e(TAG, "Digitransit API returned failure", error);

        if (!mHasLoadedDepartures) {
            // Nothing in the UI; show an error screen.
            Fragment frag = error instanceof NoConnectionError ?
                    new NoConnectionFragment() :
                    new UnexpectedErrorFragment();
            setFragment(frag);
            return;
        }

        // Departures shown; keep them and show the error in a snackbar.
        if (error instanceof NoConnectionError) {
            showUpdateErrorSnackbar(R.string.departure_list_update_no_connection_failure);
        } else if (error instanceof TimeoutError) {
            showUpdateErrorSnackbar(R.string.departure_list_update_timeout);
        } else if (error instanceof ServerError || error instanceof ParseError) {
            showUpdateErrorSnackbar(R.string.departure_list_update_server_error);
        } else {
            showUpdateErrorSnackbar(R.string.departure_list_update_unexpected_failure);
        }

        // In case we ended up keeping the old list around, remove the loading indicator
        DepartureListFragment frag = getDepartureList();
        if (frag != null) {
            frag.updateFinished();
        }
    }

    /**
     * Shows a Snackbar with the given error message and a 'Retry' button that
     * triggers a new departure update.
     *
     * @param message the message to show
     */
    private void showUpdateErrorSnackbar(@StringRes int message) {
        Helpers.snackbarWithRetry(findViewById(android.R.id.content), message,
                new View.OnClickListener() {
                    public void onClick(View v) { updateDepartures(); }
                }
        ).show();
    }

    /** DepartureListFragment hooks **/
    @Override
    public void onLoadMoreDepartures() {
        // TODO: Implement once supported
    }

    @Override
    public void onDepartureUpdate() {
        Logger.i(TAG, "Update requested; performing it");
        updateDepartures();
    }

    /**
     * Triggers a departure list update.
     */
    private void updateDepartures() {
        showLoadingIndicator();

        Logger.i(TAG, "Fetching departures from Digitransit");
        DigitransitApi.getDepartures(this, mStopId, getNumDepartures(), this);
    }

    /**
     * Shows the loading screen or refresh spinner for the duration of the update.
     */
    private void showLoadingIndicator() {
        if (mHasLoadedDepartures) {
            Logger.d(TAG, "Showing SwipeRefresh spinner for non-initial load");
            DepartureListFragment list = getDepartureList();

            if (list == null) {
                throw new AssertionError("Departures loaded but the list not found");
            } else {
                list.updateStarted();
            }
        } else {
            Logger.d(TAG, "Showing loading screen for initial load");
            String msg = getResources().getString(R.string.loading_departures);
            setFragment(LoadingFragment.createWithMessage(msg));
        }
    }

    /**
     * Find the departure list fragment (if any).
     *
     * @return an existing fragment or null if not found
     */
    private @Nullable DepartureListFragment getDepartureList() {
        return (DepartureListFragment) getFragment(FRAG_DEPARTURE_LIST);
    }

    /**
     * Retrieve the number of departures to shown from prefs.
     *
     * @return the number of departures to fetch
     */
    private int getNumDepartures() {
        String key = getString(R.string.pref_key_departures_num_results);
        String limit = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(key, "20");

        try {
            return Integer.valueOf(limit);
        } catch (NumberFormatException e) {
            Logger.w(TAG, "getNumDepartures(): Invalid limit %s", limit);
            return 10;
        }
    }
}
