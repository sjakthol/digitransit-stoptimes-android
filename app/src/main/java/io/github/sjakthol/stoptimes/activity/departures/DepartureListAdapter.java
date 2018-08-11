package io.github.sjakthol.stoptimes.activity.departures;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.digitransit.models.CitybikeStatus;
import io.github.sjakthol.stoptimes.digitransit.models.Departure;

import java.sql.Timestamp;
import java.util.Vector;

/**
 * An adapter for the departure list.
 */
class DepartureListAdapter extends RecyclerView.Adapter<DepartureListAdapter.ViewHolder> {
    /**
     * The list of departures this adapter adapts.
     */
    private Vector<Departure> mDepartures;

    /**
     * The list of citybike statuses this adapter adapts.
     */
    private Vector<CitybikeStatus> mCitybikeStatuses;

    /**
     * The default text colors.
     */
    private ColorStateList mDefaultPrimaryTextColors;
    private ColorStateList mDefaultSecondaryTextColors;

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mRoute;
        private final TextView mHeadsign;
        private final TextView mPlatform;
        private final TextView mTime;

        ViewHolder(View itemView) {
            super(itemView);

            mRoute = (ImageView) itemView.findViewById(R.id.departure_route);
            mHeadsign = (TextView) itemView.findViewById(R.id.departure_headsign);
            mTime = (TextView) itemView.findViewById(R.id.departure_time);
            mPlatform = (TextView) itemView.findViewById(R.id.departure_platform);
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
    }

    @Override
    public int getItemCount() {
        if (mDepartures == null && mCitybikeStatuses == null) {
            return 0;
        } else if (mDepartures != null) {
            return mDepartures.size();
        } else if (mCitybikeStatuses != null) {
            return mCitybikeStatuses.size();
        } else {
            return 0;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_departure_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context ctx = holder.itemView.getContext();

        // Save text colors before we start to modify them
        maybeSaveTextColors(holder);

        if (mDepartures != null) {
            bindDeparture(holder, position, ctx);
        } else if (mCitybikeStatuses != null) {
            bindCitybikeStatus(holder, position, ctx);
        }
    }

    private void bindDeparture(ViewHolder holder, int position, Context ctx) {
        Departure departure = mDepartures.elementAt(position);

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
     * Set the departures for this adapter.
     *
     * @param departures the new list of Departures to show.
     */
    void setDepartureList(Vector<Departure> departures) {
        mDepartures = departures;
        mCitybikeStatuses = null;
        notifyDataSetChanged();
    }

    void setCitybikeStatus(CitybikeStatus status) {
        mDepartures = null;
        mCitybikeStatuses = new Vector<>();
        mCitybikeStatuses.add(status);
        notifyDataSetChanged();
    }
}
