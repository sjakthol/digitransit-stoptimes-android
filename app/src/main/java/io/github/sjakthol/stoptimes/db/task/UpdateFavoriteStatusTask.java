package io.github.sjakthol.stoptimes.db.task;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import io.github.sjakthol.stoptimes.db.StopListContract;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import io.github.sjakthol.stoptimes.utils.Logger;

/**
 * A task for updating favorite status of a stop. Use the non-varargs variant of
 * .execute() to execute the update.
 */
public class UpdateFavoriteStatusTask extends DatabaseTask<Bundle, Void> {
    private static final String TAG = UpdateFavoriteStatusTask.class.getSimpleName();
    private static final String BUNDLE_STOP_ID = "BUNDLE_STOP_ID";
    private static final String BUNDLE_IS_FAVORITE = "BUNDLE_IS_FAVORITE";

    /**
     * Create a DatabaseTask for the given StopListDatabaseHelper.
     *
     * @param dbHelper the database helper instance
     */
    protected UpdateFavoriteStatusTask(StopListDatabaseHelper dbHelper) {
        super(dbHelper);
    }

    /**
     * Set the favorite status of the given stop. Use this method to execute the task.
     *
     * @param stopId the GTFS ID of the stop to modify
     * @param isFavorite the value to set for is_favorite column
     */
    public AsyncTask<Bundle, Void, AsyncTaskResult<Void>> execute(String stopId, boolean isFavorite) {
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_STOP_ID, stopId);
        bundle.putBoolean(BUNDLE_IS_FAVORITE, isFavorite);
        return this.execute(bundle);
    }

    @Override
    public Void runTask(SQLiteDatabase db, Bundle... params) {

        // The values to update
        ContentValues values = new ContentValues();
        values.put(StopListContract.Stop.COLUMN_NAME_IS_FAVORITE, params[0].getBoolean(BUNDLE_IS_FAVORITE));

        // The rows to update
        String where = StopListContract.Stop.COLUMN_NAME_GTFS_ID + " = ?";
        String[] whereArgs = {params[0].getString(BUNDLE_STOP_ID)};

        // Execute
        int updated = db.update(StopListContract.Stop.TABLE_NAME, values, where, whereArgs);
        if (updated != 1) {
            Logger.w(TAG, "Unexpected number of columns changed %d columns", updated);
        }

        return null;
    }
}
