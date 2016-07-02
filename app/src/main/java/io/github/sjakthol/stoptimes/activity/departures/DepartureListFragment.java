package io.github.sjakthol.stoptimes.activity.departures;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.digitransit.models.Departure;
import io.github.sjakthol.stoptimes.utils.Logger;

import java.util.Vector;

/**
 * A fragment that shows a list of departures from a Stop.
 */
public class DepartureListFragment extends Fragment {
    private static final String TAG = DepartureListFragment.class.getSimpleName();
    private static final int REFRESH_DELAY_MILLIS = 60000;

    /**
     * The list of departures.
     */
    private Vector<Departure> mDepartures;

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
    private Runnable mRefreshRunnable;

    /**
     * A handler used to dispatch the update runnable.
     */
    private Handler mHandler;

    public DepartureListFragment() {}

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

        mAdapter = new DepartureListAdapter(mDepartures);

        RecyclerView recycler = (RecyclerView) layout.findViewById(R.id.departure_list_recycler);
        recycler.setAdapter(mAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                mListener.onDepartureUpdate();

                // Schedule the next update.
                mHandler.postDelayed(mRefreshRunnable, REFRESH_DELAY_MILLIS);
            }
        };

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

    /**
     * Update the departure list that is shown in this fragment.
     *
     * @param departures the list of departures
     */
    void setDepartureList(Vector<Departure> departures) {
        Logger.i(TAG, "Changing departure list");
        mDepartures = departures;

        if (mAdapter != null) {
            mAdapter.setDepartureList(departures);
        }
    }

    /**
     * Notify the fragment that an update has started. updateFinished() MUST be called once the
     * update finishes or a loading indicator is shown on the screen indefinitely.
     */
    void updateStarted() {
        if (mRefreshLayout != null) {
            Logger.d(TAG, "Update has started; showing spinner");
            mRefreshLayout.setRefreshing(true);
        }
    }

    /**
     * Notify the fragment that any pending update has finished.
     */
    void updateFinished() {
        if (mRefreshLayout != null) {
            Logger.d(TAG, "Clearing refresh indicator");
            mRefreshLayout.setRefreshing(false);
        }
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
    }
}
