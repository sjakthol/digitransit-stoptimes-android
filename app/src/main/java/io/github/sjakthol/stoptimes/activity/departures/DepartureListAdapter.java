package io.github.sjakthol.stoptimes.activity.departures;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.digitransit.models.CitybikeStatus;
import io.github.sjakthol.stoptimes.digitransit.models.Departure;
import io.github.sjakthol.stoptimes.digitransit.models.DepartureFilter;
import io.github.sjakthol.stoptimes.utils.Logger;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Vector;

/**
 * An adapter for the departure list.
 */
class DepartureListAdapter extends RecyclerView.Adapter<DepartureListAdapter.ViewHolder> {
    private static final String TAG = DepartureListAdapter.class.getSimpleName();

    /**
     * The list of departures this adapter adapts.
     */
    private Vector<Departure> mDepartures;
    private Vector<Departure> mDeparturesAfterFiltering;

    /**
     * The set that contains all applied departure filters.
     */
    private HashSet<DepartureFilter> mDepartureFilters;

    /**
     * A flags for filters.
     */
    private boolean mAreFiltersEnabled = true;
    private boolean mEditFilterMode = false;

    /**
     * The list of citybike statuses this adapter adapts.
     */
    private Vector<CitybikeStatus> mCitybikeStatuses;

    /**
     * The default text colors.
     */
    private ColorStateList mDefaultPrimaryTextColors;
    private ColorStateList mDefaultSecondaryTextColors;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final CheckBox mFilterCheckbox;
        private final ImageView mRoute;
        private final TextView mHeadsign;
        private final TextView mPlatform;
        private final TextView mTime;

        private final DepartureListAdapter mAdapter;

        ViewHolder(View itemView, DepartureListAdapter adapter) {
            super(itemView);

            mRoute = itemView.findViewById(R.id.departure_route);
            mHeadsign = itemView.findViewById(R.id.departure_headsign);
            mTime = itemView.findViewById(R.id.departure_time);
            mPlatform = itemView.findViewById(R.id.departure_platform);
            mFilterCheckbox = itemView.findViewById(R.id.departure_filter_checkbox);
            mFilterCheckbox.setOnClickListener(this);

            mAdapter = adapter;
        }

        ImageView getRoute() {
            return mRoute;
        }
        TextView getHeadsign() {
            return mHeadsign;
        }
        TextView getTime() {
            return mTime;
        }
        TextView getPlatform() {
            return mPlatform;
        }
        CheckBox getFilterCheckbox() {
            return mFilterCheckbox;
        }

        @Override
        public void onClick(View view) {
            mAdapter.onFilterClicked(getAdapterPosition());
        }
    }

    @Override
    public int getItemCount() {
        if (mDeparturesAfterFiltering != null) {
            return mDeparturesAfterFiltering.size();
        } else if (mCitybikeStatuses != null) {
            return mCitybikeStatuses.size();
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_departure_list_item, parent, false);
        return new ViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context ctx = holder.itemView.getContext();

        // Save text colors before we start to modify them
        maybeSaveTextColors(holder);

        if (mDeparturesAfterFiltering != null) {
            bindDeparture(holder, position, ctx);
        } else if (mCitybikeStatuses != null) {
            bindCitybikeStatus(holder, position, ctx);
        }
    }

    private void bindDeparture(ViewHolder holder, int position, Context ctx) {
        Departure departure = mDeparturesAfterFiltering.elementAt(position);

        // Build the drawable avatar
        int color = ContextCompat.getColor(ctx, departure.getRouteColor());
        int size = ctx.getResources().getDimensionPixelSize(R.dimen.route_font_size);
        TextDrawable drawable = TextDrawable.builder()
            .beginConfig()
                .bold()
                .fontSize(size)
                .useFont(Typeface.DEFAULT)
            .endConfig()
            .buildRound(departure.getRoute(), color);

        holder.getRoute().setImageDrawable(drawable);
        holder.getHeadsign().setText(departure.getHeadsign());

        String platform = departure.getPlatform();
        if (platform == null) {
            holder.getPlatform().setVisibility(View.GONE);
        } else {
            holder.getPlatform().setVisibility(View.VISIBLE);
            holder.getPlatform().setText(departure.formatPlatformCode(holder.getPlatform().getResources()));
        }

        // Render the departure time.
        Timestamp time = departure.isRealtime() ?
            departure.getRealtimeDeparture() :
            departure.getScheduledDeparture();

        // If the departure is happening in near future, make it "Now".
        long delta = time.getTime() - System.currentTimeMillis();
        String leaves = (delta < 60000) ?
                ctx.getResources().getString(R.string.departure_now) :
                DateUtils.formatDateTime(ctx, time.getTime(), DateUtils.FORMAT_SHOW_TIME);

        holder.getTime().setText(leaves);

        if (mEditFilterMode) {
            holder.getFilterCheckbox().setVisibility(View.VISIBLE);
            holder.getFilterCheckbox().setChecked(doesDepartureMatchFilters(departure));
            holder.getTime().setVisibility(View.GONE);
        } else {
            holder.getFilterCheckbox().setVisibility(View.GONE);
            holder.getTime().setVisibility(View.VISIBLE);
        }

        // Highlight realtime departure times with a different color
        if (departure.isRealtime()) {
            int realtimeColor = ContextCompat.getColor(ctx, R.color.departure_realtime);

            holder.getTime().setTextColor(realtimeColor);
            holder.getHeadsign().setTextColor(realtimeColor);
            holder.getPlatform().setTextColor(realtimeColor);
        } else {
            holder.getTime().setTextColor(mDefaultPrimaryTextColors);
            holder.getHeadsign().setTextColor(mDefaultPrimaryTextColors);
            holder.getPlatform().setTextColor(mDefaultSecondaryTextColors);
        }
    }


    /**
     * Notify the adapter about change in the filter state of given entry.
     *
     * @param adapterPosition the position that changed filter state
     */
    private void onFilterClicked(int adapterPosition) {
        Departure d = mDeparturesAfterFiltering.get(adapterPosition);
        Logger.d(TAG, "Filter checkbox was clicked (route=%s, headsign=%s)",
                d.getRoute(), d.getHeadsign());

        DepartureFilter f = DepartureFilter.fromDeparture(d);
        if (mDepartureFilters.contains(f)) {
            Logger.d(TAG, "Removing filter from filter set: %s", f);
            mDepartureFilters.remove(f);
        } else {
            Logger.d(TAG, "Adding filter to filter set: %s", f);
            mDepartureFilters.add(f);
        }

        mDeparturesAfterFiltering = getDeparturesAfterFiltering();

        notifyDataSetChanged();
    }

    /**
     * Determine if given departure matches the current set of departure filters
     *
     * @param d the departure to check
     * @return true if filters match the departure, false otherwise
     */
    private boolean doesDepartureMatchFilters(Departure d) {
        for (DepartureFilter f : mDepartureFilters) {
            if (f.matchesDeparture(d)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get those departures that match current filters.
     *
     * @return list of departures that match filters
     */
    private Vector<Departure> getDeparturesAfterFiltering() {
        if (mEditFilterMode || !mAreFiltersEnabled) {
            // Filter aren't applied in the edit mode or when they are disabled
            return mDepartures;
        }

        if (mDepartureFilters == null || mDepartureFilters.size() == 0) {
            // No filters --> show everything
            return mDepartures;
        }

        if (mDepartures == null) {
            // No departures defined so cannot filter them
            return null;
        }

        Vector<Departure> deps = new Vector<>();
        for (Departure d : mDepartures) {
            if (doesDepartureMatchFilters(d)) {
                deps.add(d);
            }
        }

        if (deps.isEmpty()) {
            // Show all departures filters would've excluded them all
            return mDepartures;
        }

        return deps;
    }

    private void maybeSaveTextColors(ViewHolder holder) {
        if (mDefaultPrimaryTextColors == null) {
            mDefaultPrimaryTextColors = holder.getTime().getTextColors();
        }

        if (mDefaultSecondaryTextColors == null) {
            mDefaultSecondaryTextColors = holder.getPlatform().getTextColors();
        }
    }

    private void bindCitybikeStatus(ViewHolder holder, int position, Context ctx) {
        CitybikeStatus status = mCitybikeStatuses.elementAt(position);

        String availability = ctx.getResources().getString(
            R.string.citybike_station_availability, status.getBikesAvailable(), status.getSpaces());

        int stationAvailability = status.getStationAvailable() ?
            R.string.citybike_status_active : R.string.citybike_status_inactive;

        // Re-purpose existing text boxes & image view for this case
        holder.getRoute().setImageResource(R.drawable.ic_bike);
        holder.getHeadsign().setText(R.string.citybike_station);
        holder.getPlatform().setText(stationAvailability);
        holder.getTime().setText(availability);

        // Make sure everything is visible
        holder.getHeadsign().setVisibility(View.VISIBLE);
        holder.getTime().setVisibility(View.VISIBLE);
        holder.getPlatform().setVisibility(View.VISIBLE);

        int color;
        if (status.getBikesAvailable() > 5) {
            color = ContextCompat.getColor(ctx, R.color.citybike_station_good);
        } else if (status.getBikesAvailable() == 0) {
            color = ContextCompat.getColor(ctx, R.color.citybike_station_none);
        } else {
            color = ContextCompat.getColor(ctx, R.color.citybike_station_few);
        }

        holder.getTime().setTextColor(color);
    }

    /**
     * Fetch departures this view is holding onto.
     *
     * @return the vector of departures
     */
    Vector<Departure> getDepartures() {
        return mDepartures;
    }

    /**
     * Fetch departure filters this view is applying
     *
     * @return a set of departure filters
     */
    HashSet<DepartureFilter> getDepartureFilters() {
        return mDepartureFilters;
    }

    /**
     * Set the departures for this adapter.
     *
     * @param departures the new list of Departures to show.
     */
    void setDepartureList(Vector<Departure> departures) {
        mDepartures = departures;
        mDeparturesAfterFiltering = getDeparturesAfterFiltering();

        mCitybikeStatuses = null;
        notifyDataSetChanged();
    }

    void setDepartureFilters(HashSet<DepartureFilter> filters) {
        mDepartureFilters = filters;
        mDeparturesAfterFiltering = getDeparturesAfterFiltering();
        notifyDataSetChanged();
    }

    void setCitybikeStatus(CitybikeStatus status) {
        mDeparturesAfterFiltering = null;
        mDepartureFilters = null;
        mDepartures = null;

        mCitybikeStatuses = new Vector<>();
        mCitybikeStatuses.add(status);
        notifyDataSetChanged();
    }

    void setFilterUpdateMode(boolean mode) {
        mEditFilterMode = mode;
        mDeparturesAfterFiltering = getDeparturesAfterFiltering();
        notifyDataSetChanged();
    }

    void toggleFilters() {
        mAreFiltersEnabled = !mAreFiltersEnabled;
        mDeparturesAfterFiltering = getDeparturesAfterFiltering();
        notifyDataSetChanged();
    }
}
