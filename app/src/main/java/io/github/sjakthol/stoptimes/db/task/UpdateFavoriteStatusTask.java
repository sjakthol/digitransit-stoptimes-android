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
        boolean isFavorite = params[0].getBoolean(BUNDLE_IS_FAVORITE);
        String stopId = params[0].getString(BUNDLE_STOP_ID);

        if (isFavorite) {
            Logger.i(TAG, "Adding %s to favorites", stopId);

            // Prepare data
            ContentValues values = new ContentValues();
            values.put(StopListContract.Stop.COLUMN_NAME_GTFS_ID, stopId);

            // Insert
            db.insert(StopListContract.Stop.FAVORITES_TABLE_NAME, null, values);

        } else {
            Logger.i(TAG, "Removing %s from favorites", stopId);

            // Prepare deletion
            String where = StopListContract.Stop.COLUMN_NAME_GTFS_ID + " = ?";
            String[] whereArgs = {stopId};

            // Delete
            long changed = db.delete(StopListContract.Stop.FAVORITES_TABLE_NAME, where, whereArgs);

            if (changed != 1) {
                Logger.w(TAG, "Unexpected number of rows deleted: %d rows", changed);
            }
        }

        return null;
    }
}
