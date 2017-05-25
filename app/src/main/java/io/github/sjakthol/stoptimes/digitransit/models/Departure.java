package io.github.sjakthol.stoptimes.digitransit.models;

import android.content.res.Resources;
import android.text.TextUtils;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.utils.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

public final class Departure {
    private static final String TAG = Departure.class.getSimpleName();
    private final String mHeadsign;
    private final String mRoute;
    private final VehicleType mRouteType;
    private final boolean mRealtime;
    private final Timestamp mRealtimeDeparture;
    private final Timestamp mScheduledDeparture;
    private final String mPlatform;

    private Departure(String route, VehicleType routeType, String sign, String platform, Timestamp sDep, Timestamp rtDep, boolean isRt) {
        mRoute = fixRoute(route, routeType);

        mRouteType = routeType;
        mHeadsign = sign;
        mPlatform = TextUtils.isEmpty(platform) || platform.equals("null") ? null : platform;
        mScheduledDeparture = sDep;
        mRealtimeDeparture = rtDep;
        mRealtime = isRt;
    }

    /**
     * Parses the given JSON object into a Departure object. The object should have following form:
     * {
     *   "realtime": false,
     *   "serviceDay": 1465851600,
     *   "scheduledDeparture": 64620,
     *   "realtimeDeparture": 64620,
     *   "trip": {
     *     "route": {
     *       "shortName": "A"
     *       "type": "RAIL"
     *     },
     *     "tripHeadsign": "Helsinki"
     *   }
     * }
     *
     * }
     * @param obj the JSON object to parse
     * @return a new Departure object
     * @throws JSONException if the JSON is not valid, unexpected or is missing some fields
     */
    public static Departure fromJsonObject(JSONObject obj) throws JSONException {
        JSONObject trip = obj.getJSONObject("trip");
        JSONObject route = trip.getJSONObject("route");
        long day = obj.getLong("serviceDay");
        long scheduled = obj.getLong("scheduledDeparture");
        long realtime = obj.getLong("realtimeDeparture");

        JSONObject stopInfo = obj.getJSONObject("stop");
        String platform = stopInfo.getString("platformCode");

        return new Departure(
            route.getString("shortName"),
            routeToVehicleType(route.getString("type")),
            trip.getString("tripHeadsign"),
            platform,
            new Timestamp((day + scheduled) * 1000),
            new Timestamp((day + realtime) * 1000),
            obj.getBoolean("realtime")
        );
    }

    /**
     * Ensures that the route is not null / empty.
     *
     * @param route the route code
     * @param routeType the type of the route
     * @return @param route if route is not null, "null" or ""; otherwise "M" for SUBWAY and "?" for others.
     */
    private String fixRoute(String route, VehicleType routeType) {
        if (TextUtils.isEmpty(route) || route.equals("null")) {
            // In some cases route name can be null (e.g. subway).
            if (routeType == VehicleType.SUBWAY) {
                return "M";
            } else {
                return "?";
            }
        }

        // Route was defined; use it
        return route;
    }

    /**
     * Convert the strings BUS, TRAM, RAIL and SUBWAY into corresponding route
     * type. Unknown type is converted to BUS.
     *
     * @param type the route type string
     *
     * @return a route type constant
     */
    static VehicleType routeToVehicleType(String type) {
        // These types are almost extended GTFS route types with some exceptions.
        // Ref: https://developers.google.com/transit/gtfs/reference/extended-route-types
        int i;
        try {
            i = Integer.parseInt(type);
        } catch (NumberFormatException e) {
            Logger.w(TAG, "Failed to parse raw vehicle type as an integer: '%s'", type);
            return VehicleType.BUS;
        }

        if (i == 0) {
            // 0 means tram
            return VehicleType.TRAM;
        } else if (i == 1) {
            // 1 means subway
            return VehicleType.SUBWAY;
        } else if (i >= 100 && i < 200){
            // 100 -> 199 means some kind of a train
            return VehicleType.COMMUTER_TRAIN;
        } else if (i >= 700 && i < 800) {
            // 700 -> 800 means bus service
            return VehicleType.BUS;
        } else {
            // Values not seen in the wild (default to bus)
            Logger.w(TAG, "Unknown route type %s", i);
            return VehicleType.BUS;
        }
    }

    /**
     * Get the route code of this departure (e.g. 550 or U)
     *
     * @return the route code
     */
    public String getRoute() {
        return mRoute;
    }

    /**
     * Get a color corresponding the given route based on the route type (HSL colors).
     *
     * @return a color resource reference
     */
    public int getRouteColor() {
        return getRouteColor(mRouteType);
    }

    /**
     * Get a vehicle color for the route of this departure (HSL colors).
     *
     * @param type a route type
     *
     * @return a color resource
     */
    @SuppressWarnings("WeakerAccess")
    int getRouteColor(VehicleType type) {
        switch (type) {
            case BUS:
                return R.color.hsl_bus;
            case TRAM:
                return R.color.hsl_tram;
            case COMMUTER_TRAIN:
                return R.color.hsl_commuter_train;
            case SUBWAY:
                return R.color.hsl_subway;
            default:
                throw new RuntimeException("Non-conclusive switch!");
        }
    }

    /**
     * Get the headsign i.e. the destination of this departure.
     *
     * @return headsign
     */
    public String getHeadsign() {
        return mHeadsign;
    }

    /**
     * Get the time the departure is scheduled to happen.
     *
     * @return timestamp
     */
    public Timestamp getScheduledDeparture() {
        return mScheduledDeparture;
    }

    /**
     * Get the time the departure is about to happen based on realtime information. If realtime information is not
     * available this will be the same as scheduled departure.
     *
     * @return realtime departure timestamp
     */
    public Timestamp getRealtimeDeparture() {
        return mRealtimeDeparture;
    }

    /**
     * Check if the information is realtime.
     *
     * @return true if getRealtimeDeparture() contains realtime prediction about the departure; false otherwise
     */
    public boolean isRealtime() {
        return mRealtime;
    }

    /**
     * Get the track or platform code (if any).
     *
     * @return the track / platform code or null
     */
    public String getPlatform() {
        return mPlatform;
    }

    public String formatPlatformCode(Resources res) {
        if (mRouteType == VehicleType.COMMUTER_TRAIN) {
            // For trains, call the platform "Track"
            return res.getString(R.string.platform_code_train, getPlatform());
        }

        // For others call it 'Platform'
        return res.getString(R.string.platform_code_default, getPlatform());
    }
}
