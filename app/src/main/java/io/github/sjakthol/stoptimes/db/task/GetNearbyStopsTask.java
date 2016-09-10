package io.github.sjakthol.stoptimes.db.task;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import io.github.sjakthol.stoptimes.db.StopListContract;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import io.github.sjakthol.stoptimes.utils.Logger;

public class GetNearbyStopsTask extends QueryStopsDatabaseTask<Bundle> {
    private static final String TAG = GetNearbyStopsTask.class.getSimpleName();
    private static final String BUNDLE_LOCATION = "BUNDLE_LOCATION";
    private static final String BUNDLE_LIMIT = "BUNDLE_LIMIT";

    private static final String SQL_QUERY =
        "SELECT * FROM (" +
            "SELECT " +
                TextUtils.join(", ", STOP_QUERY_COLUMNS) + ", " +
                "(? - lat) * (? - lat) + (? - lon) * (? - lon) as distance_estimate" +
            " FROM " +
                StopListContract.Stop.STOPS_TABLE_NAME +
            " NATURAL LEFT JOIN " +
                StopListContract.Stop.FAVORITES_TABLE_NAME +
            " UNION " +
            "SELECT " +
                TextUtils.join(", ", STATION_QUERY_COLUMNS) + ", " +
                "(? - lat) * (? - lat) + (? - lon) * (? - lon) as distance_estimate" +
            " FROM " +
                StopListContract.Stop.STATIONS_TABLE_NAME +
            " NATURAL LEFT JOIN " +
                StopListContract.Stop.FAVORITES_TABLE_NAME +
        ") " +
        " ORDER BY distance_estimate " +
        " LIMIT ?";

    /**
     * Create a GetNearbyStopsTask for the given StopListDatabaseHelper.
     *
     * @param dbHelper the database helper instance
     */
    public GetNearbyStopsTask(StopListDatabaseHelper dbHelper) {
        super(dbHelper);
    }

    @Override
    public Cursor runTask(SQLiteDatabase db, Bundle... params) {
        Bundle data = params[0];
        Location location = data.getParcelable(BUNDLE_LOCATION);
        String limit = data.getString(BUNDLE_LIMIT);

        Logger.i(TAG, "Fetching %s stops near %s", limit, location);

        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());

        String[] args = {lat, lat, lon, lon, lat, lat, lon, lon, limit};

        return db.rawQuery(SQL_QUERY, args);
    }

    /**
     * Get the stops are near the given location.
     *
     * @param location the current user location
     * @param limit the number of results to show
     */
    public AsyncTask<Bundle, Void, AsyncTaskResult<Cursor>> execute(Location location, String limit) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_LOCATION, location);
        bundle.putString(BUNDLE_LIMIT, limit);
        return this.execute(bundle);
    }
}
