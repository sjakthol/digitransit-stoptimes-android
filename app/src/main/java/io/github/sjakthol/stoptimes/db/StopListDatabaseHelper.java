package io.github.sjakthol.stoptimes.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.github.sjakthol.stoptimes.utils.Helpers;
import io.github.sjakthol.stoptimes.utils.Logger;

/**
 * A helper for accessing the stop list database.
 */
public class StopListDatabaseHelper extends SQLiteOpenHelper {
    private final String TAG = getClass().getSimpleName();
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "stops.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ", ";
    private static final String SQL_CREATE_STOPS_TABLE =
        "CREATE TABLE IF NOT EXISTS " + StopListContract.Stop.STOPS_TABLE_NAME + " (" +
            StopListContract.Stop.COLUMN_NAME_GTFS_ID + TEXT_TYPE + " PRIMARY KEY NOT NULL " + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_CODE + TEXT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_LAT + REAL_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_LON + REAL_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE + INT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_LOCATION_TYPE + TEXT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_PLATFORM_CODE + TEXT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_PARENT_STATION + TEXT_TYPE + COMMA_SEP +
            " FOREIGN KEY(" + StopListContract.Stop.COLUMN_NAME_PARENT_STATION + ") REFERENCES " +
                StopListContract.Stop.STATIONS_TABLE_NAME + "(" + StopListContract.Stop.COLUMN_NAME_GTFS_ID + ")" +
        " )";

    private static final String SQL_CREATE_STATIONS_TABLE =
        "CREATE TABLE IF NOT EXISTS " + StopListContract.Stop.STATIONS_TABLE_NAME + " (" +
            StopListContract.Stop.COLUMN_NAME_GTFS_ID + TEXT_TYPE + " PRIMARY KEY NOT NULL " + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_CODE + TEXT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_LAT + REAL_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_LON + REAL_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE + INT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_LOCATION_TYPE + TEXT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_PLATFORM_CODE + TEXT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_PARENT_STATION + TEXT_TYPE +
        " )";

    private static final String SQL_CREATE_FAVORITES_TABLE =
        "CREATE TABLE IF NOT EXISTS " + StopListContract.Stop.FAVORITES_TABLE_NAME + " (" +
            StopListContract.Stop.COLUMN_NAME_GTFS_ID + TEXT_TYPE + " PRIMARY KEY NOT NULL " + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_IS_FAVORITE + INT_TYPE + " DEFAULT 1" + COMMA_SEP +
            " FOREIGN KEY(" + StopListContract.Stop.COLUMN_NAME_GTFS_ID + ") REFERENCES " +
                StopListContract.Stop.STOPS_TABLE_NAME + "(" + StopListContract.Stop.COLUMN_NAME_GTFS_ID + ")" +
    " )";

    private static final String SQL_CREATE_DEPARTURE_FILTERS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + StopListContract.Stop.DEPARTURE_FILTERS_TABLE_NAME + " (" +
                    StopListContract.Stop.COLUMN_NAME_GTFS_ID + TEXT_TYPE + " NOT NULL " + COMMA_SEP +
                    StopListContract.Stop.COLUMN_NAME_ROUTE + TEXT_TYPE + " NOT NULL " + COMMA_SEP +
                    StopListContract.Stop.COLUMN_NAME_HEADSIGN + TEXT_TYPE + " NOT NULL " +
            " )";
    /**
     * Create a new StopListDatabaseHelper for the given application context.
     *
     * @param context an application context to tie this database
     */
    public StopListDatabaseHelper(Context context) {
        // In-memory db for tests
        super(context, Helpers.isInJUnitTest() ? null : DATABASE_NAME, null, DATABASE_VERSION);

        Logger.i(TAG, "Using database at %s", this.getDatabaseName());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.i(TAG, "Creating stop database");
        db.execSQL(SQL_CREATE_STATIONS_TABLE);
        db.execSQL(SQL_CREATE_STOPS_TABLE);
        db.execSQL(SQL_CREATE_FAVORITES_TABLE);
        db.execSQL(SQL_CREATE_DEPARTURE_FILTERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.i(TAG, "Upgrading db from v%d to v%d", oldVersion, newVersion);

        if (oldVersion == 1 && newVersion == 2) {
            // Just drop the old table with the old schema
            db.execSQL("DROP TABLE " + StopListContract.Stop.STOPS_TABLE_NAME);

            // ...and make it look like we just created the table
            this.onCreate(db);
        }

        if (oldVersion == 2 && newVersion == 3) {
            // Add new table
            db.execSQL(SQL_CREATE_DEPARTURE_FILTERS_TABLE);
        }
    }
}