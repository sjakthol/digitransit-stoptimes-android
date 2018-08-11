package io.github.sjakthol.stoptimes.db.task;

import android.database.Cursor;
import io.github.sjakthol.stoptimes.db.StopListContract;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;

/**
 * A common base class for tasks that query stops from the database. This class defines the
 * columns each such query should return.
 */
abstract class QueryStopsDatabaseTask<T> extends DatabaseTask<T, Cursor> {

    static final String BUNDLE_CITYBIKES = "BUNDLE_CITYBIKES";

    /**
     * The list of columns all stop queries should return
     */
    static final String[] STOP_QUERY_COLUMNS = {
        StopListContract.Stop.STOPS_TABLE_NAME + "._rowid_ as " + StopListContract.Stop._ID,
        StopListContract.Stop.COLUMN_NAME_GTFS_ID,
        StopListContract.Stop.COLUMN_NAME_NAME,
        StopListContract.Stop.COLUMN_NAME_CODE,
        StopListContract.Stop.COLUMN_NAME_LON,
        StopListContract.Stop.COLUMN_NAME_LAT,
        StopListContract.Stop.COLUMN_NAME_PLATFORM_CODE,
        StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE,
        StopListContract.Stop.COLUMN_NAME_LOCATION_TYPE,
        StopListContract.Stop.COLUMN_NAME_PARENT_STATION,
        StopListContract.Stop.COLUMN_NAME_IS_FAVORITE,
    };

    /**
     * The list of columns all station queries should return
     */
    static final String[] STATION_QUERY_COLUMNS = {
            StopListContract.Stop.STATIONS_TABLE_NAME + "._rowid_ as " + StopListContract.Stop._ID,
            StopListContract.Stop.COLUMN_NAME_GTFS_ID,
            StopListContract.Stop.COLUMN_NAME_NAME,
            StopListContract.Stop.COLUMN_NAME_CODE,
            StopListContract.Stop.COLUMN_NAME_LON,
            StopListContract.Stop.COLUMN_NAME_LAT,
            StopListContract.Stop.COLUMN_NAME_PLATFORM_CODE,
            StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE,
            StopListContract.Stop.COLUMN_NAME_LOCATION_TYPE,
            StopListContract.Stop.COLUMN_NAME_PARENT_STATION,
            StopListContract.Stop.COLUMN_NAME_IS_FAVORITE,
    };

    /**
     * Create a DatabaseTask for the given StopListDatabaseHelper.
     *
     * @param dbHelper the database helper instance
     */
    QueryStopsDatabaseTask(StopListDatabaseHelper dbHelper) {
        super(dbHelper);
    }
}
