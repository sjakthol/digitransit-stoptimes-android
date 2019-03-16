package io.github.sjakthol.stoptimes.db.task;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import io.github.sjakthol.stoptimes.db.StopListContract;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import io.github.sjakthol.stoptimes.utils.Logger;

/**
 * A task that returns the list of stops matching the given query. The task expects
 * two parameters:
 * - query: the query string to match
 * - limit: the number of stops to return
 *
 * These MUST be given to the .execute(query, limit) method.
 *
 */
public class QueryStopsTask extends QueryStopsDatabaseTask<Bundle> {
    private static final String TAG = QueryStopsTask.class.getSimpleName();
    private static final String BUNDLE_QUERY = "BUNDLE_QUERY";
    private static final String BUNDLE_LIMIT = "BUNDLE_LIMIT";

    /**
     * Create a DatabaseTask for the given StopListDatabaseHelper.
     *
     * @param dbHelper the database helper instance
     */
    public QueryStopsTask(StopListDatabaseHelper dbHelper) {
        super(dbHelper);
    }

    private static final String SQL_QUERY_STOPS =
        "SELECT * FROM (" +
            "SELECT " + TextUtils.join(", ", STOP_QUERY_COLUMNS) +
            " FROM " + StopListContract.Stop.STOPS_TABLE_NAME +
            " NATURAL LEFT JOIN " + StopListContract.Stop.FAVORITES_TABLE_NAME +
            " WHERE " + StopListContract.Stop.COLUMN_NAME_NAME + " LIKE ?" +
            "   AND " + StopListContract.Stop.COLUMN_NAME_PARENT_STATION + " IS NULL" +
            " UNION " +
            "SELECT " + TextUtils.join(", ", STATION_QUERY_COLUMNS) +
            " FROM " + StopListContract.Stop.STATIONS_TABLE_NAME +
            " NATURAL LEFT JOIN " + StopListContract.Stop.FAVORITES_TABLE_NAME +
            " WHERE " + StopListContract.Stop.COLUMN_NAME_NAME + " LIKE ?" +
            ") " +
            "WHERE " + StopListContract.Stop.COLUMN_NAME_LOCATION_TYPE + " != ? " +
            "ORDER BY " +
                StopListContract.Stop.COLUMN_NAME_NAME +
            " LIMIT ?";

    @Override
    public Cursor runTask(SQLiteDatabase db, Bundle... params) {
        Bundle data = params[0];

        String rawQuery = data.getString(BUNDLE_QUERY);
        String query = prepareQueryString(rawQuery);
        String limit = data.getString(BUNDLE_LIMIT);
        boolean citybikes = data.getBoolean(BUNDLE_CITYBIKES);

        Logger.d(TAG, "Running query: query='%s', limit='%s'",
                query, limit);

        String[] selection = {query, query, citybikes ? "" : "CITYBIKE_STATION", limit};
        return db.rawQuery(SQL_QUERY_STOPS, selection);
    }

    /**
     * Plugs the given query into LIKE pattern.
     *
     * @param query the query string
     * @return a wildcard pattern for LIKE query
     */
    private String prepareQueryString(String query) {
        if (TextUtils.isEmpty(query)) {
            return query;
        }

        return String.format("%%%s%%", query);
    }

    /**
     * Get the stops matching the query
     *
     * @param query the query string
     * @param limit the number of results to show
     * @param includeCitybikes whether to include citybikes or not
     */
    public AsyncTask<Bundle, Void, AsyncTaskResult<Cursor>> execute(String query, String limit, boolean includeCitybikes) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_QUERY, query);
        bundle.putString(BUNDLE_LIMIT, limit);
        bundle.putBoolean(BUNDLE_CITYBIKES, includeCitybikes);
        return this.execute(bundle);
    }
}
