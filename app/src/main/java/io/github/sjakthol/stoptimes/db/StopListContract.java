package io.github.sjakthol.stoptimes.db;

import android.provider.BaseColumns;

/**
 * Constants used for the stop database.
 */
public final class StopListContract {
    public static abstract class Stop implements BaseColumns {
        public static final String STOPS_TABLE_NAME = "stops";
        public static final String COLUMN_NAME_GTFS_ID = "gtfs_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_CODE = "code";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LON = "lon";
        public static final String COLUMN_NAME_PLATFORM_CODE = "platform";
        public static final String COLUMN_NAME_VEHICLE_TYPE = "vehicle_type";
        public static final String COLUMN_NAME_LOCATION_TYPE = "location_type";
        public static final String COLUMN_NAME_IS_FAVORITE = "is_favorite";
        public static final String COLUMN_NAME_PARENT_STATION = "parent_station";
        public static final String FAVORITES_TABLE_NAME = "favorites";
        public static final String STATIONS_TABLE_NAME = "stations";
    }
}
