package io.github.sjakthol.stoptimes.db.task;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;

import static io.github.sjakthol.stoptimes.db.StopListContract.Stop.*;

/**
 * A task that returns the list of favorite stops.
 */
public class GetDepartureFiltersTask extends QueryStopsDatabaseTask<Bundle> {
    private static final String BUNDLE_STOPID = "BUNDLE_STOPID";

    /**
     * Create a DatabaseTask for the given StopListDatabaseHelper.
     *
     * @param dbHelper the database helper instance
     */
    public GetDepartureFiltersTask(StopListDatabaseHelper dbHelper) {
        super(dbHelper);
    }

    private static final String GET_DEPARTURE_FILTERS_SQL =
        "SELECT " +
            COLUMN_NAME_GTFS_ID + ", " +
            COLUMN_NAME_ROUTE + ", " +
            COLUMN_NAME_HEADSIGN + " " +
        "FROM " + DEPARTURE_FILTERS_TABLE_NAME + " " +
        "WHERE " +
            COLUMN_NAME_GTFS_ID + " = ?";


    @Override
    public Cursor runTask(SQLiteDatabase db, Bundle... params) {
        Bundle data = params[0];
        String[] args = {data.getString(BUNDLE_STOPID)};
        return db.rawQuery(GET_DEPARTURE_FILTERS_SQL, args);
    }

    /**
     * Get departure filters
     *
     * @param stopId the stop for which filters should be get from
     */
    public AsyncTask<Bundle, Void, AsyncTaskResult<Cursor>> execute(String stopId) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_STOPID, stopId);
        return this.execute(bundle);
    }
}
