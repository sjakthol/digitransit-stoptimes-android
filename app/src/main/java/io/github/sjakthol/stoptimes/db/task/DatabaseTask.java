package io.github.sjakthol.stoptimes.db.task;

import android.database.sqlite.SQLiteDatabase;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
import io.github.sjakthol.stoptimes.utils.FallibleAsyncTask;

/**
 * An abstract interface for running database queries in a background thread.
 *
 * Extending classes must implement the method runTask() that receives two arguments:
 *  (1) a read-write SQLiteDatabase
 *  (2) arguments of type T passed to the execute() method AsyncTask
 * The method should return an object of type V with the results of the computation (e.g. cursor).
 */
abstract class DatabaseTask<T, V> extends FallibleAsyncTask<T, V> {
    /**
     * A handle to the database.
     */
    private final StopListDatabaseHelper mDbHelper;

    /**
     * Create a DatabaseTask for the given StopListDatabaseHelper.
     *
     * @param dbHelper the database helper instance
     */
    DatabaseTask(StopListDatabaseHelper dbHelper) {
        mDbHelper = dbHelper;
    }

    @Override
    public V runTask(T... params) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return runTask(db, params);
    }

    /**
     * A method that performs the required queries against the database.
     *
     * @param db a read-write database to work with
     * @param params arguments passed to .execute() of AsyncTask
     *
     * @return cursor containing any query results
     */
    protected abstract V runTask(SQLiteDatabase db, T... params);
}
