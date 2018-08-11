package io.github.sjakthol.stoptimes.activity.departures;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import com.android.volley.*;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.activity.BaseActivity;
import io.github.sjakthol.stoptimes.activity.generic.LoadingFragment;
import io.github.sjakthol.stoptimes.activity.generic.MessageFragment;
import io.github.sjakthol.stoptimes.activity.generic.NoConnectionFragment;
import io.github.sjakthol.stoptimes.activity.generic.UnexpectedErrorFragment;
import io.github.sjakthol.stoptimes.digitransit.DigitransitApi;
import io.github.sjakthol.stoptimes.digitransit.models.CitybikeStatus;
import io.github.sjakthol.stoptimes.digitransit.models.Departure;
import io.github.sjakthol.stoptimes.utils.Helpers;
import io.github.sjakthol.stoptimes.utils.Logger;
import io.github.sjakthol.stoptimes.utils.VolleyWrapper;
import org.json.JSONException;

import java.util.Vector;

public class DepartureListActivity extends BaseActivity implements
    NoConnectionFragment.OnConnectionAvailable,
    DepartureListFragment.Hooks,
    DigitransitApi.DepartureResponseListener
{
    private static final String TAG = DepartureListActivity.class.getSimpleName();
    private static final String FRAG_DEPARTURE_LIST = "FRAG_DEPARTURE_LIST";

    public static final String EXTRA_STOP_ID = "EXTRA_STOP_ID";
    public static final String EXTRA_STOP_TYPE = "EXTRA_STOP_TYPE";
    public static final String EXTRA_STOP_NAME = "EXTRA_STOP_NAME";

    /**
     * The ID of the stop shown in this activity.
     */
    private String mStopId;

    /**
     * A singleton DepartureListFragment used to display
     * the departures.
     */
    private DepartureListFragment mDepartureList;

    /**
     * The type of the stop (STOP vs STATION)
     */
    private String mLocationType;

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

        // Get the singleton DepartureListFragment or create a new empty one.
        mDepartureList = (DepartureListFragment) getFragment(FRAG_DEPARTURE_LIST);
        if (mDepartureList == null) {
            mDepartureList = new DepartureListFragment();
        }

        mStopId = getIntent().getStringExtra(EXTRA_STOP_ID);
        mLocationType = getIntent().getStringExtra(EXTRA_STOP_TYPE);

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

    /**
     * Digitransit API hooks
     **/
    @Override
    public void onDeparturesAvailable(Vector<Departure> departures) {
        ensureDepartureListIsAdded();

        if (departures.isEmpty()) {
            Logger.i(TAG, "Got empty departure list; showing notice");
            MessageFragment frag = MessageFragment.withMessage(
                R.string.departures_empty_title,
                R.string.departures_empty_desc
            );

            setFragment(frag);

            return;
        }

        Logger.i(TAG, "Updating DepartureListFragment");
        mDepartureList.setDepartureList(departures);
        mDepartureList.updateFinished();
    }

    private void ensureDepartureListIsAdded() {
        if (!mDepartureList.isAdded()) {
            Logger.i(TAG, "Showing DepartureListFragment after initial update");
            setFragment(mDepartureList, FRAG_DEPARTURE_LIST);
        }
    }

    @Override
    public void onDepartureLoadError(VolleyError error) {
        Logger.e(TAG, "Digitransit API returned failure", error);

        if (!mDepartureList.isAdded()) {
            // The departure list is not shown; show an error screen.
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

        // Notify the fragment that update has finished
        mDepartureList.updateFinished();
    }

    @Override
    public void onCitybikeStatusAvailable(CitybikeStatus status) {
        Logger.d(TAG, "Got citybike status update: %s", status);

        ensureDepartureListIsAdded();
        mDepartureList.setCitybikeStatus(status);
        mDepartureList.updateFinished();
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
                    public void onClick(View v) {
                        updateDepartures();
                    }
                }
        ).show();
    }

    /**
     * DepartureListFragment hooks
     **/
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

        if (mLocationType.equals("CITYBIKE_STATION")) {
            updateCitybikeStatus();
            return;
        }

        Logger.i(TAG, "Fetching departures from Digitransit");
        try {
            DigitransitApi.getDepartures(this, mStopId, mLocationType, getNumDepartures(), getShowArrivalsToTerminus(), this);
        } catch (JSONException e) {
            // This can happen if getNumDepartures() is infinity or NaN. If
            // it does, YOLO!
            Logger.wtf(TAG, "getNumDepartures() == %s", getNumDepartures());
            Logger.wtf(TAG, "Bad JSON!", e);
        }
    }

    private void updateCitybikeStatus() {
        Logger.d(TAG, "Updating status of citybike station %s", mStopId);
        try {
            DigitransitApi.getCitybikeStationStatus(this, mStopId, this);
        } catch (JSONException e) {
            Logger.wtf(TAG, "Bad JSON!", e);
        }
    }

    /**
     * Shows the loading screen or refresh spinner for the duration of the update.
     */
    private void showLoadingIndicator() {
        if (mDepartureList.isAdded()) {
            Logger.d(TAG, "Showing spinner since list is shown");
            mDepartureList.updateStarted();
        } else {
            Logger.d(TAG, "Showing loading screen since list is hidden");
            String msg = getResources().getString(
                mLocationType.equals("CITYBIKE_STATION") ?
                    R.string.loading_citybike_status :
                    R.string.loading_departures);
            setFragment(LoadingFragment.createWithMessage(msg));
        }
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

    private boolean getShowArrivalsToTerminus() {
        String key = getString(R.string.pref_key_show_arrivals_to_terminus);
        return PreferenceManager
            .getDefaultSharedPreferences(this)
            .getBoolean(key, false);
    }
}
