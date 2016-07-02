package io.github.sjakthol.stoptimes.activity.stoplist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.activity.BaseActivity;
import io.github.sjakthol.stoptimes.activity.departures.DepartureListActivity;
import io.github.sjakthol.stoptimes.activity.generic.LoadingFragment;
import io.github.sjakthol.stoptimes.activity.generic.NoConnectionFragment;
import io.github.sjakthol.stoptimes.activity.generic.UnexpectedErrorFragment;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
import io.github.sjakthol.stoptimes.db.task.GetFavoriteStopsTask;
import io.github.sjakthol.stoptimes.db.task.QueryStopsTask;
import io.github.sjakthol.stoptimes.db.task.UpdateFavoriteStatusTask;
import io.github.sjakthol.stoptimes.digitransit.models.Stop;
import io.github.sjakthol.stoptimes.net.NetworkRequiredException;
import io.github.sjakthol.stoptimes.net.VolleyWrapper;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import io.github.sjakthol.stoptimes.utils.Logger;

import java.util.regex.Pattern;

/**
 * An activity that displays a list of stops.
 */
public class StopListActivity
    extends BaseActivity
    implements
        NoConnectionFragment.OnConnectionAvailable,
        SearchFragment.StopListSearchHandler,
        StopListAdapter.ActionHandler
{
    private static final String TAG = StopListActivity.class.getSimpleName();
    private static final String FRAG_STOP_LIST = "FRAG_STOP_LIST";
    private static final String FRAG_SEARCH = "FRAG_SEARCH";
    private static final String BUNDLE_STOP_LIST_SOURCE = "BUNDLE_STOP_LIST_SOURCE";
    private static final String BUNDLE_STOP_LIST_QUERY = "BUNDLE_STOP_LIST_QUERY";
    private static final StopListSource DEFAULT_STOP_LIST_SOURCE = StopListSource.FAVORITES;

    /**
     * List of possible sources for the stop list in this activity.
     * - FAVORITES: The list of favorite stops
     * - QUERY: The list of stops matching mQueryString
     */
    private enum StopListSource {
        FAVORITES,
        QUERY,
    }

    /**
     * The database helper to use for accessing the database.
     */
    private StopListDatabaseHelper mDatabaseHelper;

    /**
     * The source for the stop list (FAVORITES or QUERY).
     */
    private StopListSource mStopListSource;

    /**
     * The current query string.
     */
    private String mQueryString;

    /**
     * True if the activity has been stopped.
     */
    private boolean mIsStopped;

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

        setContentView(R.layout.activity_stop_list);
        setToolbar(R.id.toolbar);

        mDatabaseHelper = new StopListDatabaseHelper(this);

        if (savedInstanceState == null) {
            mStopListSource = DEFAULT_STOP_LIST_SOURCE;
            mQueryString = null;
        } else {
            mStopListSource = (StopListSource) savedInstanceState.getSerializable(BUNDLE_STOP_LIST_SOURCE);
            mQueryString = savedInstanceState.getString(BUNDLE_STOP_LIST_QUERY);
        }

        SearchFragment searchFragment = (SearchFragment) getFragment(FRAG_SEARCH);
        if (searchFragment == null) {
            Logger.i(TAG, "Creating new SearchFragment");
            searchFragment = new SearchFragment();
            getSupportFragmentManager()
                .beginTransaction()
                .add(searchFragment, FRAG_SEARCH)
                .commit();
        }

        searchFragment.setInitialQuery(mQueryString);

        Logger.i(TAG, "onCreate(): qs='%s', source='%s'", mQueryString, mStopListSource);

        // List in initialized in onResume()
    }

    /**
     * Try to close the search if back is pressed. If search is not open, nothing happens.
     */
    @Override
    public void onBackPressed() {
        SearchFragment frag = (SearchFragment) getFragment(FRAG_SEARCH);
        if (frag == null || !frag.closeSearch()) {
            super.onBackPressed();
        }
    }

    /**
     * Save the current list source and query.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(BUNDLE_STOP_LIST_QUERY, mQueryString);
        outState.putSerializable(BUNDLE_STOP_LIST_SOURCE, mStopListSource);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume()");

        mIsStopped = false;

        // Always show loading screen since this could be first load
        updateStopListFragment(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.d(TAG, "onStop()");

        mIsStopped = true;
        VolleyWrapper.getInstance(this).getRequestQueue().cancelAll(this);
    }

    @Override
    public void onSearchBegin() {
        Logger.i(TAG, "Search started");
        mStopListSource = StopListSource.QUERY;
        mQueryString = "";

        ensureEmptyListMessage();
        updateStopListFragment(false);
    }

    @Override
    public boolean onSearchChanged(String query) {
        Logger.i(TAG, "Search changed to '%s'", query);
        mQueryString = query;

        ensureEmptyListMessage();
        updateStopListFragment(false);
        return false;
    }

    @Override
    public boolean onSearchFinished() {
        Logger.i(TAG, "Search finished");
        mQueryString = null;
        mStopListSource = StopListSource.FAVORITES;

        ensureEmptyListMessage();
        updateStopListFragment(false);
        return false;
    }

    @Override
    public void onConnectionAvailable() {
        Logger.i(TAG, "Got internet connection");
        updateStopListFragment(true);
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
                updateStopListFragment(false);
            }
        }.execute(stop.getId(), isFavorite);
    }

    private void updateStopListFragment(boolean showLoading) {
        Logger.i(TAG, "Updating stopListFragment: qs='%s', source='%s'", mQueryString, mStopListSource);

        maybeShowLoadingIndicator(showLoading);

        switch (mStopListSource) {
            case FAVORITES:
                new GetFavoriteStopsTask(mDatabaseHelper) {
                    @Override
                    protected void onPostExecute(AsyncTaskResult<Cursor> res) {
                        super.onPostExecute(res);
                        handleDatabaseQueryTaskResult(res);
                    }
                }.execute();
                break;
            case QUERY:
                new QueryStopsTask(mDatabaseHelper) {
                    @Override
                    protected void onPostExecute(AsyncTaskResult<Cursor> res) {
                        super.onPostExecute(res);
                        handleDatabaseQueryTaskResult(res);
                    }
                }.execute(mQueryString, getNumStops());
                break;
        }
    }

    private String getNumStops() {
        String key = getString(R.string.pref_key_stoplist_num_results);
        String limit = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(key, "20");
        if (!Pattern.matches("\\d+", limit)) {
            Logger.w(TAG, "getNumStops(): Invalid limit %s", limit);
            limit = "20";
        }

        return limit;
    }

    private void maybeShowLoadingIndicator(boolean showLoading) {
        if (showLoading) {
            Logger.i(TAG, "Showing loading indicator");
            setFragment(LoadingFragment.createWithMessage(getResources().getString(R.string.loading_stops)));
        }
    }

    private void handleDatabaseQueryTaskResult(AsyncTaskResult<Cursor> res) {
        if (mIsStopped) {
            Logger.w(TAG, "Ignoring database task result after onStop()");
            return;
        }

        if (res.isSuccess()) {
            Logger.i(TAG, "Task was successful. Showing stop list");
            StopListFragment frag = (StopListFragment) getFragment(FRAG_STOP_LIST);
            if (frag == null) {
                Logger.d(TAG, "Creating new StopListFragment");
                frag = StopListFragment.newInstance(res.getResult());
                setFragment(frag, FRAG_STOP_LIST);
            } else {
                Logger.d(TAG, "StopListFragment already present; updating");
                frag.setCursor(res.getResult());
            }

            ensureEmptyListMessage(frag);

        } else if (res.getError() instanceof NetworkRequiredException) {
            Logger.i(TAG, "Task failed due to missing network connection");
            setFragment(new NoConnectionFragment());
        } else {
            Logger.e(TAG, "Task failed due to unknown exception", res.getError());
            setFragment(new UnexpectedErrorFragment());
        }
    }

    /**
     * Ensures that the empty list fragment in the UI has the correct empty message.
     */
    private void ensureEmptyListMessage() {
        StopListFragment frag = (StopListFragment) getFragment(FRAG_STOP_LIST);
        if (frag == null) {
            Logger.w(TAG, "StopListFragment null in ensureEmptyListMessage()!");
            return;
        }

        ensureEmptyListMessage(frag);
    }

    /**
     * Ensures that the fragment shows correct empty list texts for the given query state.
     *
     * @param frag a fragment reference
     */
    private void ensureEmptyListMessage(StopListFragment frag) {
        if (mStopListSource == StopListSource.FAVORITES) {
            Logger.d(TAG, "Setting empty texts for favorites");
            frag.setEmptyText(
                    getResources().getString(R.string.stoplist_empty_favorites_title),
                    getResources().getString(R.string.stoplist_empty_favorites_description)
            );
        } else if (mStopListSource == StopListSource.QUERY && TextUtils.isEmpty(mQueryString)) {
            Logger.d(TAG, "Setting empty texts for empty query");
            frag.setEmptyText(
                    getResources().getString(R.string.stoplist_empty_empty_query_title),
                    getResources().getString(R.string.stoplist_empty_empty_query_description)
            );
        } else if (mStopListSource == StopListSource.QUERY) {
            Logger.d(TAG, "Setting empty texts for non-empty query");
            frag.setEmptyText(
                    getResources().getString(R.string.stoplist_empty_query_title),
                    getResources().getString(R.string.stoplist_empty_query_description)
            );
        }
    }
}
