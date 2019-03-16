package io.github.sjakthol.stoptimes.db.task;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import io.github.sjakthol.stoptimes.db.StopListContract;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
import io.github.sjakthol.stoptimes.digitransit.models.DepartureFilter;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;

import java.util.HashSet;

/**
 * A task for updating departure filters for a stop.
 */
public class UpdateDepartureFilterTask extends DatabaseTask<Bundle, Void> {
    private static final String TAG = UpdateDepartureFilterTask.class.getSimpleName();
    private static final String BUNDLE_STOP_ID = "BUNDLE_STOP_ID";
    private static final String BUNDLE_FILTERS = "BUNDLE_FILTERS";

    /**
     * Create a DatabaseTask for the given StopListDatabaseHelper.
     *
     * @param dbHelper the database helper instance
     */
    public UpdateDepartureFilterTask(StopListDatabaseHelper dbHelper) {
        super(dbHelper);
    }

    /**
     * Replace current filters of this stop. Use this method to execute the task.
     *
     * @param stopId the GTFS ID of the stop to modify
     * @param filters filters to add
     */
    public AsyncTask<Bundle, Void, AsyncTaskResult<Void>> execute(String stopId, HashSet<DepartureFilter> filters) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_STOP_ID, stopId);
        bundle.putSerializable(BUNDLE_FILTERS, filters);
        return this.execute(bundle);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Void runTask(SQLiteDatabase db, Bundle... params) {
        String stopId = params[0].getString(BUNDLE_STOP_ID);
        HashSet<DepartureFilter> filters = (HashSet<DepartureFilter>) params[0].getSerializable(BUNDLE_FILTERS);

        String where = StopListContract.Stop.COLUMN_NAME_GTFS_ID + " = ?";
        String[] whereArgs = {stopId};

        db.beginTransaction();
        try {
            db.delete(StopListContract.Stop.DEPARTURE_FILTERS_TABLE_NAME, where, whereArgs);
            ContentValues values = new ContentValues();
            for (DepartureFilter f : filters) {
                values.put(StopListContract.Stop.COLUMN_NAME_GTFS_ID, stopId);
                values.put(StopListContract.Stop.COLUMN_NAME_ROUTE, f.getRoute());
                values.put(StopListContract.Stop.COLUMN_NAME_HEADSIGN, f.getHeadsign());
                db.insert(StopListContract.Stop.DEPARTURE_FILTERS_TABLE_NAME, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return null;
    }
}
