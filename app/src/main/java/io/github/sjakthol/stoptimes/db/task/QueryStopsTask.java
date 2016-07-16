package io.github.sjakthol.stoptimes.db.task;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import io.github.sjakthol.stoptimes.db.StopListContract;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
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
public class QueryStopsTask extends QueryStopsDatabaseTask<String> {
    private static final String TAG = QueryStopsTask.class.getSimpleName();

    /**
     * Create a DatabaseTask for the given StopListDatabaseHelper.
     *
     * @param dbHelper the database helper instance
     */
    public QueryStopsTask(StopListDatabaseHelper dbHelper) {
        super(dbHelper);
    }

    private static final String SQL_QUERY_STOPS =
        "SELECT " + TextUtils.join(", ", STOP_QUERY_COLUMNS) +
        " FROM " + StopListContract.Stop.STOPS_TABLE_NAME +
        " NATURAL LEFT JOIN " + StopListContract.Stop.FAVORITES_TABLE_NAME +
        " WHERE " + StopListContract.Stop.COLUMN_NAME_NAME + " LIKE ?" +
        " ORDER BY " + StopListContract.Stop.COLUMN_NAME_NAME +
        " LIMIT %s";

    @Override
    public Cursor runTask(SQLiteDatabase db, String... params) {
        if (params.length != 2) throw new AssertionError(".execute(query, limit) requires two params");

        String qs = prepareQueryString(params[0]);
        String[] selection = {qs};
        String limit = params[1];

        Logger.d(TAG, "Running stoplist query: query='%s', limit='%s'", qs, limit);

        return db.rawQuery(String.format(SQL_QUERY_STOPS, limit), selection);
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
}
