package io.github.sjakthol.stoptimes.db.task;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import io.github.sjakthol.stoptimes.db.StopListContract;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;

/**
 * A task that returns the list of favorite stops.
 */
public class GetFavoriteStopsTask extends QueryStopsDatabaseTask<Void> {
    /**
     * Create a DatabaseTask for the given StopListDatabaseHelper.
     *
     * @param dbHelper the database helper instance
     */
    public GetFavoriteStopsTask(StopListDatabaseHelper dbHelper) {
        super(dbHelper);
    }

    private static final String GET_FAVORITES_SQL =
        "SELECT * FROM (" +
            "SELECT " +
                TextUtils.join(", ", STOP_QUERY_COLUMNS) +
            " FROM " +
                StopListContract.Stop.STOPS_TABLE_NAME +
            " NATURAL INNER JOIN " +
                StopListContract.Stop.FAVORITES_TABLE_NAME +
            " UNION " +
            "SELECT " +
                TextUtils.join(", ", STATION_QUERY_COLUMNS) +
            " FROM " +
                StopListContract.Stop.STATIONS_TABLE_NAME +
            " NATURAL INNER JOIN " +
                StopListContract.Stop.FAVORITES_TABLE_NAME +
        ")" +
        " ORDER BY " +
            StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE + ", " +
            StopListContract.Stop.COLUMN_NAME_NAME;


    @Override
    public Cursor runTask(SQLiteDatabase db, Void... params) {
        return db.rawQuery(GET_FAVORITES_SQL, null);
    }
}
