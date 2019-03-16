package io.github.sjakthol.stoptimes.activity.departures;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.digitransit.models.DepartureFilter;
import io.github.sjakthol.stoptimes.digitransit.models.CitybikeStatus;
import io.github.sjakthol.stoptimes.digitransit.models.Departure;
import io.github.sjakthol.stoptimes.utils.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import static io.github.sjakthol.stoptimes.activity.departures.DepartureListActivity.EXTRA_STOP_ID;
import static io.github.sjakthol.stoptimes.activity.departures.DepartureListActivity.EXTRA_STOP_TYPE;

/**
 * A fragment that shows a list of departures from a Stop.
 */
public class DepartureListFragment extends Fragment {
    private static final String TAG = DepartureListFragment.class.getSimpleName();
    private static final int REFRESH_DELAY_MILLIS = 60000;

    /**
     * The activity that handles departure updates.
     */
    private Hooks mListener;

    /**
     * Adapter for the list.
     */
    private DepartureListAdapter mAdapter;

    /**
     * A widget that implements the swipe-to-update behavior.
     */
    private SwipeRefreshLayout mRefreshLayout;

    /**
     * A runnable object that requests the departure list to be updated periodically.
     */
    private Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            mListener.onDepartureUpdate();

            // Schedule the next update.
            mHandler.postDelayed(mRefreshRunnable, REFRESH_DELAY_MILLIS);
        }
    };

    /**
     * A handler used to dispatch the update runnable.
     */
    private Handler mHandler;

    private boolean mEditFilterMode = false;

    public DepartureListFragment() {
        mAdapter = new DepartureListAdapter();
    }

    /**
     * Create a new DepartureListFragment with the given list of departures.
     *
     * @param departures the departures to populate the list with
     * @return a new DepartureListFragment
     */
    static DepartureListFragment withDepartures(Vector<Departure> departures) {
        DepartureListFragment frag = new DepartureListFragment();
        frag.setDepartureList(departures);
        return frag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.d(TAG, "onAttach()");

        if (context instanceof Hooks) {
            mListener = (Hooks) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement Hooks");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView()");
        View layout = inflater.inflate(R.layout.fragment_departure_list, container, false);

        // Refresh support
        mRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.departure_list_swipe_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mListener.onDepartureUpdate();
            }
        });

        RecyclerView recycler = (RecyclerView) layout.findViewById(R.id.departure_list_recycler);
        recycler.setAdapter(mAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        setHasOptionsMenu(true);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume()");

        mHandler = new Handler();
        mHandler.postDelayed(mRefreshRunnable, REFRESH_DELAY_MILLIS);
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d(TAG, "onPause()");

        mHandler.removeCallbacks(mRefreshRunnable);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logger.d(TAG, "onDetach()");

        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        Logger.d(TAG, "Creating menu items");
        inflater.inflate(R.menu.menu_departure_list, menu);

        MenuItem edit = menu.findItem(R.id.action_edit_filters);
        edit.setVisible(!getActivity().getIntent().getStringExtra(EXTRA_STOP_TYPE).equals("CITYBIKE_STATION"));

        MenuItem save = menu.findItem(R.id.action_save);
        save.setVisible(mEditFilterMode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Logger.d(TAG, "onOptionsItemSelected()");
        switch (item.getItemId()) {
            case R.id.action_edit_filters:
                mEditFilterMode = true;
                mAdapter.setFilterUpdateMode(true);
                getActivity().invalidateOptionsMenu();

                return true;

            case R.id.action_save:
                mListener.onSaveDepartureFilters(mAdapter.getDepartureFilters());

                mEditFilterMode = false;
                mAdapter.setFilterUpdateMode(false);
                getActivity().invalidateOptionsMenu();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Update the departure list that is shown in this fragment.
     *
     * @param departures the list of departures
     */
    void setDepartureList(Vector<Departure> departures) {
        Logger.i(TAG, "Changing departure list to %s", departures);
        mAdapter.setDepartureList(departures);
    }

    /**
     * Update the departure list filters for the fragment.
     *
     * @param cursor cursor to read the filters from
     */
    void setDepartureFiltersFromCursor(Cursor cursor) {
        Logger.i(TAG, "Found %d filters", cursor.getCount());
        HashSet<DepartureFilter> f = new HashSet<>();
        try {
            while (cursor.moveToNext()) {
                f.add(DepartureFilter.fromCursor(cursor));
            }
        } finally {
            cursor.close();
        }

        mAdapter.setDepartureFilters(f);
    }

    /**
     * Update the departure list to show cityibike status.
     *
     * @param status the status to show
     */
    void setCitybikeStatus(CitybikeStatus status) {
        Logger.i(TAG, "Changing departure list to status %s", status);
        mAdapter.setCitybikeStatus(status);
    }

    /**
     * Notify the fragment that an update has started. updateFinished() MUST be called once the
     * update finishes or a loading indicator is shown on the screen indefinitely.
     */
    void updateStarted() {
        if (mRefreshLayout == null) {
            Logger.w(TAG, "updateStarted() called with null mRefreshLayout!");
            return;
        }

        Logger.d(TAG, "Update has started; showing spinner");
        mRefreshLayout.setRefreshing(true);
    }

    /**
     * Notify the fragment that any pending update has finished.
     */
    void updateFinished() {
        if (mRefreshLayout == null) {
            Logger.w(TAG, "updateFinished() called with null mRefreshLayout!");
            return;
        }

        Logger.d(TAG, "Clearing refresh indicator");
        mRefreshLayout.setRefreshing(false);

    }

    /**
     * An interface used to notify the parent about reaching the end-of-departures.
     */
    interface Hooks {
        /**
         * Called when more departures are needed.
         */
        void onLoadMoreDepartures();

        /**
         * Called when update is requested.
         */
        void onDepartureUpdate();

        /**
         * Called when departure filters need to be saved.
         */
        void onSaveDepartureFilters(HashSet<DepartureFilter> filters);
    }
}
