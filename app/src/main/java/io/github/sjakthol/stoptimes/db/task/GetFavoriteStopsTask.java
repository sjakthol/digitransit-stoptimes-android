package io.github.sjakthol.stoptimes.db.task;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import io.github.sjakthol.stoptimes.db.StopListContract;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;

/**
 * A task that returns the list of favorite stops.
 */
public class GetFavoriteStopsTask extends QueryStopsDatabaseTask<Bundle> {
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
        " WHERE " + StopListContract.Stop.COLUMN_NAME_LOCATION_TYPE + " != ? " +
        " ORDER BY " +
            StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE + ", " +
            StopListContract.Stop.COLUMN_NAME_NAME;


    @Override
    public Cursor runTask(SQLiteDatabase db, Bundle... params) {
        Bundle data = params[0];
        boolean citybikes = data.getBoolean(BUNDLE_CITYBIKES);
        String[] args = {citybikes ? "" : "CITYBIKE_STATION"};
        return db.rawQuery(GET_FAVORITES_SQL, args);
    }

    /**
     * Get favorite stops.
     *
     * @param includeCitybikes whether to include citybikes or not
     */
    public AsyncTask<Bundle, Void, AsyncTaskResult<Cursor>> execute(boolean includeCitybikes) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(BUNDLE_CITYBIKES, includeCitybikes);;
        return this.execute(bundle);
    }
}
