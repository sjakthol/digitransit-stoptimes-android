package io.github.sjakthol.stoptimes.activity.stoplist;


import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.utils.Helpers;
import io.github.sjakthol.stoptimes.utils.Logger;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * A {@link Fragment} that displays a list of stops. An activity that embeds this
 * fragment MUST implement StopListAdapter.ActionHandler.
 */
public class StopListFragment extends Fragment {
    private static final String TAG = StopListFragment.class.getSimpleName();

    /**
     * The adapter for the list.
     */
    private StopListAdapter mAdapter;

    /**
     * A cursor containing the current result.
     */
    private Cursor mCursor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView()");

        View root = inflater.inflate(R.layout.fragment_stop_list, container, false);

        Logger.i(TAG, "Creating an adapter with cursor=%s", mCursor);
        mAdapter = new StopListAdapter(getActivity(), mCursor);

        // Setup the recycler
        RecyclerView recycler = (RecyclerView) root.findViewById(R.id.stop_list_recycler);
        recycler.setAdapter(mAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume()");

        // Set the activity as the list item action handler
        try {
            mAdapter.setActionHandler((StopListAdapter.ActionHandler) getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity() + " must implement StopListAdapter.ActionHandler");
        }

        maybeStartLocationTracking();
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d(TAG, "onPause()");

        // Remove the action handler
        mAdapter.setActionHandler(null);

        stopLocationTracking();
    }

    /**
     * Starts tracking user location if the corresponding setting is enabled.
     */
    private void maybeStartLocationTracking() {
        if (!Helpers.shouldTrackLocation(getContext())) {
            Logger.i(TAG, "maybeStartLocationTracking(): Location tracking disabled.");
            return;
        }

        Logger.i(TAG, "maybeStartLocationTracking(): Starting to track user location");
        SmartLocation.with(getContext()).location().start(new OnLocationUpdatedListener() {
            @Override
            public void onLocationUpdated(Location location) {
                Logger.d(TAG, "Location updated: %s", location);

                if (mAdapter != null) {
                    mAdapter.setUserLocation(location);
                }
            }
        });
    }

    /**
     * Stops tracking user location.
     */
    private void stopLocationTracking() {
        Logger.i(TAG, "Stopping location tracking.");
        SmartLocation.with(getContext()).location().stop();
    }

    /**
     * Set the cursor that contains the data to show in this view.
     *
     * @param cursor the cursor to view
     */
    void setCursor(Cursor cursor) {
        this.mCursor = cursor;

        if (mAdapter != null) {
            Logger.i(TAG, "Updating adapter with a new cursor");
            mAdapter.changeCursor(cursor);
        }
    }
}
