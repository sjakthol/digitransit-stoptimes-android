package io.github.sjakthol.stoptimes.activity.stoplist;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.activity.BaseActivity;
import io.github.sjakthol.stoptimes.activity.departures.DepartureListActivity;
import io.github.sjakthol.stoptimes.activity.generic.MessageFragment;
import io.github.sjakthol.stoptimes.activity.generic.UnexpectedErrorFragment;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
import io.github.sjakthol.stoptimes.db.task.UpdateFavoriteStatusTask;
import io.github.sjakthol.stoptimes.digitransit.models.Stop;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import io.github.sjakthol.stoptimes.utils.Helpers;
import io.github.sjakthol.stoptimes.utils.Logger;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public abstract class StopListActivityBase extends BaseActivity
        implements StopListAdapter.ActionHandler, OnLocationUpdatedListener {
    private static final String TAG = StopListActivityBase.class.getSimpleName();
    private static final String FRAG_LIST = "FRAG_LIST";
    private static final long LOCATION_LIFETIME = 60 * 1000 * 1000 * 1000L;

    /**
     * The current user location.
     */
    private static Location sCurrentLocation = null;
    private static long sLocationTimestamp = 0;

    public static Location getCachedLocation() {
        if (System.nanoTime() - sLocationTimestamp > LOCATION_LIFETIME) {
            return null;
        }

        return sCurrentLocation;
    }

    /**
     * The database helper to use for accessing the database.
     */
    StopListDatabaseHelper mDatabaseHelper;
    StopListFragment mStopList;
    boolean mIsStopped;

    /**
     * Create a new BaseActivity with the given container for fragments.
     *
     * @param fragmentContainer a resource ID for the fragment container in the activity
     */
    StopListActivityBase(@IdRes int fragmentContainer) {
        super(fragmentContainer);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stop_list);
        setToolbar(R.id.toolbar);

        // Initialize the database connection
        mDatabaseHelper = new StopListDatabaseHelper(this);

        // Create the stop list fragment
        mStopList = (StopListFragment) getFragment(FRAG_LIST);
        if (mStopList == null) {
            mStopList = new StopListFragment();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Helpers.shouldTrackLocation(this)) {
            Logger.i(TAG, "Starting to track user location");
            SmartLocation.with(this).location().start(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Helpers.shouldTrackLocation(this)) {
            Logger.i(TAG, "Stoppping location tracking");
            SmartLocation.with(this).location().stop();
        }
    }

    @Override
    public void onLocationUpdated(Location location) {

        sLocationTimestamp = System.currentTimeMillis();
        sCurrentLocation = location;

        if (mStopList != null) {
            mStopList.onLocationUpdated(location);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsStopped = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsStopped = false;
    }

    @Override
    public void onStopSelected(Stop stop) {
        Logger.i(TAG, "Showing details for stop '%s'", stop.getId());
        Intent i = new Intent(this, DepartureListActivity.class);
        i.putExtra(DepartureListActivity.EXTRA_STOP_ID, stop.getId());
        i.putExtra(DepartureListActivity.EXTRA_STOP_NAME, stop.formatStopName(getResources()));
        startActivity(i);
    }

    @Override
    public void onFavoriteStatusChanged(Stop stop, boolean isFavorite) {
        Logger.i(TAG, "Stop %s favorite state changed to %b", stop.getId(), isFavorite);
        new UpdateFavoriteStatusTask(mDatabaseHelper) {
            @Override
            protected void onPostExecute(AsyncTaskResult<Void> voidAsyncTaskResult) {
                super.onPostExecute(voidAsyncTaskResult);
            }
        }.execute(stop.getId(), isFavorite);
    }

    void handleDatabaseQueryTaskResult(AsyncTaskResult<Cursor> res) {
        if (mIsStopped) {
            Logger.w(TAG, "Ignoring database task result after onStop()");
            return;
        }

        if (!res.isSuccess()) {
            Logger.e(TAG, "Task failed due to unknown exception", res.getError());
            setFragment(new UnexpectedErrorFragment());
            return;
        }

        Cursor result = res.getResult();
        if (result.getCount() == 0) {
            Logger.i(TAG, "Result empty; requsting message to be shown");
            showEmptyListMessage();
            return;
        }

        Logger.i(TAG, "Got %d stops; using cursor %s", result.getCount(), result);
        showStopList(result);
    }

    /**
     * Shows the stop list with the given cursor.
     *
     * @param cursor the cursor to show
     */
    void showStopList(@Nullable Cursor cursor) {
        Logger.i(TAG, "Showing StopList with cursor %s", cursor);
        if (!mStopList.isAdded()) {
            Logger.i(TAG, "Replacing content with the stop list");
            setFragment(mStopList, FRAG_LIST);
        }

        mStopList.setCursor(cursor);
    }

    /**
     * Show an explanation message.
     *
     * @param title message title
     * @param desc elaborate description
     */
    void showMessage(@StringRes int title, @StringRes int desc) {
        MessageFragment frag = MessageFragment.withMessage(title, desc);
        setFragment(frag);
    }

    /**
     * A method used to display a message if the result is empty.
     */
    abstract void showEmptyListMessage();
}
