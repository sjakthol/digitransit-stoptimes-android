package io.github.sjakthol.stoptimes.activity.stoplist;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.digitransit.models.Stop;
import io.github.sjakthol.stoptimes.utils.CursorRecyclerViewAdapter;
import io.github.sjakthol.stoptimes.utils.Helpers;
import io.github.sjakthol.stoptimes.utils.Logger;

/**
 * An adapter that renders stops in the stop list view.
 */
class StopListAdapter extends CursorRecyclerViewAdapter<StopListAdapter.ViewHolder> {
    private static final String TAG = StopListAdapter.class.getSimpleName();

    /**
     * A class that handles user actions on the stop list items. This is static so
     * that it is accessible from the static ViewHolder class.
     */
    static private ActionHandler mHandler;

    /**
     * The user's current location (if enabled) used to calculate distances to
     * the stops.
     */
    private Location mUserLocation;

    /**
     * An interface used to dispatch user actions to the activity.
     */
    interface ActionHandler {
        /**
         * Called when a stop in the list has been selected.
         *
         * @param stop the selected stop
         */
        void onStopSelected(Stop stop);

        /**
         * Called when favorite status of a stop changes
         *
         * @param stop the affected stop
         * @param isFavorite the new favorite status
         */
        void onFavoriteStatusChanged(Stop stop, boolean isFavorite);
    }

    /**
     * Create a new adapter with the given cursor.
     *
     * @param ctx application context
     * @param cursor cursor to adapt
     */
    StopListAdapter(Context ctx, Cursor cursor) {
        super(ctx, cursor);
    }

    /**
     * A class for holding the list item view.
     */
    static class ViewHolder
        extends RecyclerView.ViewHolder
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
    {
        private final TextView mStopCode;
        private final ImageView mStopIcon;
        private final CheckBox mFavoriteCheckbox;
        private final TextView mStopName;
        private final TextView mStopCity;
        private final TextView mDistance;

        /**
         * Create a new ViewHolder for the given root view.
         *
         * @param view the root view
         */
        ViewHolder(View view) {
            super(view);

            mStopIcon = view.findViewById(R.id.stop_icon);
            mFavoriteCheckbox = view.findViewById(R.id.stop_favorite);

            mStopName = view.findViewById(R.id.stop_name);
            mStopCode = view.findViewById(R.id.stop_code);
            mStopCity = view.findViewById(R.id.stop_city);
            mDistance = view.findViewById(R.id.stop_distance);

            getFavoriteCheckbox().setOnCheckedChangeListener(this);
            getRootView().setOnClickListener(this);
        }

        View getRootView() {
            return itemView;
        }
        ImageView getStopIcon() {
            return mStopIcon;
        }
        CheckBox getFavoriteCheckbox() {
            return mFavoriteCheckbox;
        }
        TextView getStopCity() {
            return mStopCity;
        }
        TextView getStopCode() {
            return mStopCode;
        }
        TextView getStopName() {
            return mStopName;
        }
        TextView getDistance() {
            return mDistance;
        }

        /**
         * Set the favorite state of the view. This method ensures that the change event is not emitted.
         *
         * @param checked the state of the checkbox
         */
        void setFavoriteCheckboxState(boolean checked) {
            // Remove the listener to avoid infinite callback loop
            getFavoriteCheckbox().setOnCheckedChangeListener(null);
            getFavoriteCheckbox().setChecked(checked);
            getFavoriteCheckbox().setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mHandler == null) {
                Logger.w(TAG, "Action handler not set; ignoring click action");
                return;
            }

            // Forward the click to the handler
            mHandler.onStopSelected((Stop) getRootView().getTag());
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mHandler == null) {
                Logger.w(TAG, "Action handler not set; ignoring favorite state change");
                return;
            }

            mHandler.onFavoriteStatusChanged((Stop) getRootView().getTag(), isChecked);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.fragment_stop_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        // Parse the data
        Stop stop = Stop.fromCursor(cursor);

        // The tag is used to hold a reference to the stop object which will be
        // passed to action handlers.
        viewHolder.getRootView().setTag(stop);

        // Format the stop name with the platform/track information
        viewHolder.getStopName().setText(stop.formatStopName(viewHolder.getRootView().getResources()));

        if (stop.getCode().equals("null")) {
            // The stop doesn't have a code which means we cannot
            // determine the city it resides in.

            String stationType = stop.getStationType(viewHolder.getRootView().getResources());

            if (stationType == null) {
                // Hide the labes for stops.
                viewHolder.getStopCode().setVisibility(View.GONE);
                viewHolder.getStopCity().setVisibility(View.GONE);
            } else {
                // Stations don't have a code we could use to determine the
                // city it belongs to. Instead, show a label that identifies
                // it as a station in the first text box and hide the second
                viewHolder.getStopCity().setVisibility(View.VISIBLE);
                viewHolder.getStopCity().setText(stationType);

                // Hide the second label
                viewHolder.getStopCode().setVisibility(View.GONE);
            }
        } else {
            // We have a code, show them
            viewHolder.getStopCode().setText(stop.getCode());
            viewHolder.getStopCity().setText(stop.getCity());
            viewHolder.getStopCode().setVisibility(View.VISIBLE);
            viewHolder.getStopCity().setVisibility(View.VISIBLE);
        }

        viewHolder.setFavoriteCheckboxState(stop.isFavorite());
        viewHolder.getStopIcon().setImageResource(stop.getVehicleTypeIcon());

        if (mUserLocation == null) {
            // No location available; hide the distance field
            viewHolder.getDistance().setVisibility(View.GONE);
        } else {
            double distance = mUserLocation.distanceTo(stop.getLocation());
            String distanceText = Helpers.formatDistance(distance);

            viewHolder.getDistance().setVisibility(View.VISIBLE);
            viewHolder.getDistance().setText(distanceText);
        }
    }

    /**
     * Make the given handler handle clicks to the stop list items.
     *
     * @param handler the handler
     */
    void setActionHandler(ActionHandler handler) {
        StopListAdapter.mHandler = handler;
    }

    /**
     * Set the user's current location. This will cause a reflow of the list.
     *
     * @param userLocation the location
     */
    void setUserLocation(Location userLocation) {
        mUserLocation = userLocation;

        notifyDataSetChanged();
    }
}
