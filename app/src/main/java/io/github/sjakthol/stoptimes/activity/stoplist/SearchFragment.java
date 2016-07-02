package io.github.sjakthol.stoptimes.activity.stoplist;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.utils.Logger;

/**
 * A fragment that handles searches for the StopListFragment.
 */
@SuppressWarnings("WeakerAccess") // XXX: Fragments need to be public but linter tries to make this package-local.
public class SearchFragment
    extends Fragment
    implements
        SearchView.OnQueryTextListener,
        View.OnClickListener,
        SearchView.OnCloseListener
{
    private static final String TAG = SearchFragment.class.getSimpleName();

    /**
     * The activity that handles the search events.
     */
    private StopListSearchHandler mHandler;

    /**
     * The initial query string to use. If not null, the search opens automatically.
     */
    private String mInitialQuery;

    /**
     * The related SearchView element.
     */
    private SearchView mSearchView;

    /**
     * Interface used to notify SearchFragment customers about search events.
     */
    interface StopListSearchHandler {
        /**
         * Called when the search field is focused.
         */
        void onSearchBegin();

        /**
         * Called whenever the search changes.
         *
         * @param query the updated query string
         * @return true if the search was handled, false if default
         *         action should be taken
         */
        boolean onSearchChanged(String query);

        /**
         * Called when search has finished.
         *
         * @return true if the event was handled, false if default action
         *         should be taken
         */
        boolean onSearchFinished();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate()");

        // We have a menu to show.
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.d(TAG, "onAttach()");

        // Set the handler.
        try {
            mHandler = (StopListSearchHandler) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement StopListSearchHandler");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Logger.d(TAG, "onCreateOptionsMenu()");

        inflater.inflate(R.menu.menu_search, menu);

        MenuItem search = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(search);
        mSearchView.setOnCloseListener(this);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnSearchClickListener(this);

        if (!TextUtils.isEmpty(mInitialQuery)) {
            Logger.i(TAG, "Got initial query '%s'; opening search", mInitialQuery);
            mSearchView.setIconified(false);
            mSearchView.setQuery(mInitialQuery, false);
        }

        mInitialQuery = null;
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        Logger.d(TAG, "onDestroyOptionsMenu()");

        mSearchView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logger.d(TAG, "onDetach()");

        mHandler = null;
    }

    /**
     * Set the initial query to given value.
     *
     * @param query the new initial query
     */
    void setInitialQuery(String query) {
        Logger.d(TAG, "Setting initial query to '%s'", query);
        mInitialQuery = query;
    }

    /**
     * Close search.
     *
     * @return true if search was open and it was closed, false otherwise.
     */
    boolean closeSearch() {
        if (mSearchView != null && !mSearchView.isIconified()) {
            Logger.i(TAG, "closeSearch() called when search was open; closing");
            mSearchView.setIconified(true);
            return true;
        }

        Logger.i(TAG, "closeSearch() called when search was closed; no-op");
        return false;
    }

    /**
     * Handles the first click on the search view.
     *
     * @param v the clicked view
     */
    @Override
    public void onClick(View v) {
        mHandler.onSearchBegin();
    }

    /**
     * Handles the changes to the query string.
     *
     * @param newText the new query string
     * @return false to show suggestions, true to handle everything yourself
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        return mHandler.onSearchChanged(newText);
    }

    /**
     * Handles search submit button press.
     *
     * @param query the final search query
     * @return false to use the default action, true to handle everything yourself
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return mHandler.onSearchChanged(query);
    }

    /**
     * Handler for the search close event.
     *
     * @return false to clear and close the search, true if you handled this yourself
     */
    @Override
    public boolean onClose() {
        return mHandler.onSearchFinished();
    }
}
