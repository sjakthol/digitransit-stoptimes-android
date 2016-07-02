package io.github.sjakthol.stoptimes.db.task;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    protected GetFavoriteStopsTask(StopListDatabaseHelper dbHelper) {
        super(dbHelper);
    }

    @Override
    public Cursor runTask(SQLiteDatabase db, Void... params) {
        return db.query(
            StopListContract.Stop.TABLE_NAME, /* table */
            STOP_QUERY_COLUMNS, /* Columns */
            StopListContract.Stop.COLUMN_NAME_IS_FAVORITE + " = 1", /* Selection */
            null, /* selectionArgs */
            null, /* groupBy */
            null, /* having */
            StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE + ", " + StopListContract.Stop.COLUMN_NAME_NAME, /* order */
            null /* limit */
        );
    }
}
