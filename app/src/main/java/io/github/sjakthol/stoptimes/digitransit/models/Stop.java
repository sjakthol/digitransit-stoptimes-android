package io.github.sjakthol.stoptimes.digitransit.models;

import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.db.StopListContract;
import io.github.sjakthol.stoptimes.utils.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Stop {
    private static final String TAG = Stop.class.getSimpleName();
    private final String mId;
    private final String mName;
    private final String mCode;
    private final double mLat;
    private final double mLon;
    private final boolean mIsFavorite;
    private final VehicleType mVehicleType;
    private final String mPlatform;
    private final String mLocationType;
    private final String mParentStation;

    /**
     * Create new stop
     *
     * @param id the id of the stop
     * @param name the name of the stop
     * @param code the code of the stop
     * @param lat the latitude of the stop position
     * @param lon the longitude of the stop position
     * @param platform the platform code
     * @param vehicleType the type of the vehicle that mainly uses this stop
     * @param locationType the type of this stop
     * @param parentStation the gtfsId of the parent station (if any)
     * @param is_favorite if this stop is favorite or not
     */
    Stop(String id, String name, String code, double lat,
         double lon, String platform, int vehicleType,
         String locationType, String parentStation, boolean is_favorite)
    {
        mId = id;
        mName = name;
        mCode = code;
        mLat = lat;
        mLon = lon;
        mPlatform = platform;
        mVehicleType = codeToType(vehicleType);
        mLocationType = locationType;
        mParentStation = parentStation;
        mIsFavorite = is_favorite;
    }

    /**
     * Construct a stop from cursor that contains the following columns: gtfs_id, name, code, lat, lon, is_favorite.
     *
     * @param cursor the cursor to use
     *
     * @return a new Stop object
     */
    @NonNull
    public static Stop fromCursor(@NonNull Cursor cursor) {
        return new Stop(
            cursor.getString(cursor.getColumnIndex(StopListContract.Stop.COLUMN_NAME_GTFS_ID)),
            cursor.getString(cursor.getColumnIndex(StopListContract.Stop.COLUMN_NAME_NAME)),
            cursor.getString(cursor.getColumnIndex(StopListContract.Stop.COLUMN_NAME_CODE)),
            cursor.getDouble(cursor.getColumnIndex(StopListContract.Stop.COLUMN_NAME_LAT)),
            cursor.getDouble(cursor.getColumnIndex(StopListContract.Stop.COLUMN_NAME_LON)),
            cursor.getString(cursor.getColumnIndex(StopListContract.Stop.COLUMN_NAME_PLATFORM_CODE)),
            cursor.getInt(cursor.getColumnIndex(StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE)),
            cursor.getString(cursor.getColumnIndex(StopListContract.Stop.COLUMN_NAME_LOCATION_TYPE)),
            cursor.getString(cursor.getColumnIndex(StopListContract.Stop.COLUMN_NAME_PARENT_STATION)),
            cursor.getLong(cursor.getColumnIndex(StopListContract.Stop.COLUMN_NAME_IS_FAVORITE)) == 1
        );
    }

    /**
     * Construct a stop object from JSONObject.
     *
     * @param stop a JSONObject of form
     *             { gtfsId: String,
     *               name: String,
     *               code: String,
     *               lat: Double,
     *               lon: Double,
     *               platformCode: String,
     *               locationType: String,
     *               vehicleType: Int }
     *
     * @return a Stop object
     */
    public static Stop fromJson(@NonNull JSONObject stop) throws JSONException {
        JSONObject parentStation = stop.optJSONObject("parentStation");
        return new Stop(
            stop.getString("gtfsId"),
            stop.getString("name"),
            stop.optString("code", ""),
            stop.getDouble("lat"),
            stop.getDouble("lon"),
            stop.optString("platformCode", null),
            stop.getInt("vehicleType"),
            stop.getString("locationType"),
            parentStation != null ? parentStation.getString("gtfsId") : null,
            false
        );
    }

    /**
     * Convert the given vehicle type code to a corresponding constant. Known types: 0 TRAM, 1 SUBWAY, 3 BUS and
     * 109 COMMUTER_TRAIN.
     *
     * @param code the code to convert
     * @return vehicle type
     */
    @SuppressWarnings("WeakerAccess")
    public static VehicleType codeToType(int code) {
        switch (code) {
            case 0:
                return VehicleType.TRAM;
            case 1:
                return VehicleType.SUBWAY;
            case 3:
                return VehicleType.BUS;
            case 109:
                return VehicleType.COMMUTER_TRAIN;
            default:
                Logger.w(TAG, "Unknown vehicle type code '%d'", code);
                return VehicleType.BUS;
        }
    }

    public static int typeToCode(VehicleType type) {
        switch (type) {
            case TRAM:
                return 0;
            case SUBWAY:
                return 1;
            case BUS:
                return 3;
            case COMMUTER_TRAIN:
                return 109;
            default:
                Logger.w(TAG, "Unknown vehicle type '%s'", type);
                return 3;
        }
    }

    /**
     * @return GTFS ID of this stop
     */
    public String getId() {
        return mId;
    }

    /**
     * @return name of this stop
     */
    public String getName() {
        return mName;
    }

    /**
     * @return code of this stop
     */
    public String getCode() {
        return mCode;
    }

    /**
     * Check if this stop is favorite.
     *
     * @return true if favorite, false otherwise
     */
    public boolean isFavorite() {
        return mIsFavorite;
    }

    /**
     * The location of this stop.
     *
     * @return location
     */
    public Location getLocation() {
        Location loc = new Location("HSL");
        loc.setLatitude(mLat);
        loc.setLongitude(mLon);
        return loc;
    }

    /**
     * Get the city this stop resides in
     *
     * @return an ID of a string resource that contains the city name
     */
    public @StringRes int getCity() {
        if (getCode().matches("^\\d+$")) {
            return R.string.stop_location_helsinki;
        } else if (getCode().startsWith("E")) {
            return R.string.stop_location_espoo;
        } else if (getCode().startsWith("V")) {
            return R.string.stop_location_vantaa;
        } else if (getCode().startsWith("Ke")) {
            return R.string.stop_location_kerava;
        } else if (getCode().startsWith("Si")) {
            return R.string.stop_location_vantaa;
        } else if (getCode().startsWith("Ki")) {
            return R.string.stop_location_knummi;
        } else if (getCode().startsWith("Mä")) {
            return R.string.stop_location_mantsala;
        } else if (getCode().startsWith("Jä")) {
            return R.string.stop_location_jarvenpaa;
        } else if (getCode().startsWith("La")) {
            return R.string.stop_location_lahti;
        } else if (getCode().startsWith("Hy")) {
            return R.string.stop_location_hyvinkaa;
        } else if (getCode().startsWith("Ka")) {
            return R.string.stop_location_kauniainen;
        } else if (getCode().startsWith("Nu")) {
            return R.string.stop_location_nurmijarvi;
        } else if (getCode().startsWith("Pn")) {
            return R.string.stop_location_pornainen;
        } else if (getCode().startsWith("Po")) {
            return R.string.stop_location_porvoo;
        } else if (getCode().startsWith("Ri")) {
            return R.string.stop_location_riihimaki;
        } else if (getCode().startsWith("Si")) {
            return R.string.stop_location_sipoo;
        } else if (getCode().startsWith("Tu")) {
            return R.string.stop_location_tuusula;
        } else {
            Logger.w(TAG, "Unknown location in code '%s'", getCode());
            return R.string.stop_location_unknown;
        }
    }

    /**
     * The type of the vehicle that departs from this station.
     *
     * @return the vehicle type
     */
    @SuppressWarnings("WeakerAccess")
    public VehicleType getVehicleType() {
        return mVehicleType;
    }

    /**
     * An icon for the vehicle type that uses this stop.
     *
     * @return the a drawable ID for the stop icon
     */
    public int getVehicleTypeIcon() {
        return getVehicleTypeIcon(getVehicleType());
    }

    /**
     * Get a drawable icon for the given vehicle type. If the type is unknown, a bus is used.
     *
     * @param vehicleType an integer vehicle type
     *
     * @return an id of a drawable for the icon
     */
    @SuppressWarnings("WeakerAccess")
    public static int getVehicleTypeIcon(VehicleType vehicleType) {
        switch (vehicleType) {
            case TRAM:
                return R.drawable.ic_tram;
            case SUBWAY:
                return R.drawable.ic_subway;
            case BUS:
                return R.drawable.ic_bus;
            case COMMUTER_TRAIN:
                return R.drawable.ic_train;
            default:
                Logger.w(TAG, "Unknown vehicle type '%d'", vehicleType);
                return R.drawable.ic_bus;
        }
    }

    public String formatStopName(Resources res) {
        if (TextUtils.isEmpty(getPlatform()) || getPlatform().equals("null")) {
            // No platform code, name only
            return getName();
        }

        if (getVehicleType() == VehicleType.COMMUTER_TRAIN) {
            // For trains, call the platform "Track"
            return res.getString(R.string.stop_name_with_track, getName(), getPlatform());
        }

        // For others call it 'Platform'
        return res.getString(R.string.stop_name_with_platform, getName(), getPlatform());
    }

    /**
     * The platform code for this stop (if any).
     *
     * @return platform code or null
     */
    @Nullable
    @SuppressWarnings("WeakerAccess")
    public String getPlatform() {
        return mPlatform;
    }

    /**
     * Get the type of this stop. Either 'STOP' or 'STATION'.
     *
     * @return the location type
     */
    public String getLocationType() {
        return mLocationType;
    }

    /**
     * The GTFS ID of the parent station.
     *
     * @return the id or null if no parent station exist for
     * this stop.
     */
    public String getParentStation() {
        return mParentStation;
    }

    /**
     * The type of the station (if this is a station).
     *
     * @return a string that can be used in the UI to show the type
     * of the station
     */
    public String getStationType(Resources res) {
        if (!getLocationType().equals("STATION")) {
            return null;
        }

        switch (getVehicleType()) {
            case COMMUTER_TRAIN:
                return res.getString(R.string.station_train);
            case SUBWAY:
                return res.getString(R.string.station_subway);
            default:
                return res.getString(R.string.station_generic);
        }
    }
}
