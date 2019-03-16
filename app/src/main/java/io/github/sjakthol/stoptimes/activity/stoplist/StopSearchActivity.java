package io.github.sjakthol.stoptimes.activity.stoplist;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.db.task.QueryStopsTask;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import io.github.sjakthol.stoptimes.utils.Logger;

public class StopSearchActivity extends StopListActivityBase
        implements SearchView.OnCloseListener, SearchView.OnQueryTextListener
{
    private static final String TAG = StopSearchActivity.class.getSimpleName();
    private static final String BUNDLE_QUERY = "BUNDLE_QUERY";

    private String mQueryString;

    public StopSearchActivity() {
        super(R.id.stop_list_content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate()");

        // Restore any saved query string
        if (savedInstanceState != null) {
            mQueryString = savedInstanceState.getString(BUNDLE_QUERY);
            Logger.i(TAG, "Restored query %s", mQueryString);
        }

        showSearchExplanation();

        BottomNavigationView bottomBar = findViewById(R.id.navigation_bottom);
        bottomBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stop_search, menu);

        MenuItem search = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnCloseListener(this);
        searchView.setOnQueryTextListener(this);

        // Expand the search view
        searchView.setIconified(false);

        if (mQueryString != null) {
            // If we have a query, this triggers a search
            searchView.setQuery(mQueryString, false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(BUNDLE_QUERY, mQueryString);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        performSearch(newText);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        performSearch(query);
        return true;
    }

    @Override
    public boolean onClose() {
        Logger.i(TAG, "Search closed; finishing");
        finish();
        return true;
    }

    /**
     * Perform a search with the given query.
     *
     * @param query the search query
     */
    @SuppressLint("StaticFieldLeak")
    private void performSearch(String query) {
        mQueryString = query;

        if (TextUtils.isEmpty(query)) {
            showSearchExplanation();
            return;
        }

        Logger.i(TAG, "performSearch(query='%s')", query);
        new QueryStopsTask(mDatabaseHelper) {
            @Override
            protected void onPostExecute(AsyncTaskResult<Cursor> res) {
                super.onPostExecute(res);
                handleDatabaseQueryTaskResult(res);
            }
        }.execute(query, getNumStops(), getShowCitybikeStations());
    }

    @Override
    void showEmptyListMessage() {
        showEmptyResultsExplanation();
    }

    /**
     * Show a message that explains how the search
     * functions.
     */
    private void showSearchExplanation() {
        showMessage(
            R.string.stoplist_search_title,
            R.string.stoplist_search_description
        );
    }

    /**
     * Show a message that explains there was no results.
     */
    private void showEmptyResultsExplanation() {
        showMessage(
            R.string.stoplist_search_empty_title,
            R.string.stoplist_search_empty_description
        );
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
