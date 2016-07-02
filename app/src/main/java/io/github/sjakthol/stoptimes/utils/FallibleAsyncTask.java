package io.github.sjakthol.stoptimes.utils;

import android.os.AsyncTask;
import io.github.sjakthol.stoptimes.BuildConfig;

/**
 * A wrapper around AsyncTask that handles failures.
 */
abstract public class FallibleAsyncTask<T, V> extends AsyncTask<T, Void, AsyncTaskResult<V>> {
    private static final String TAG = FallibleAsyncTask.class.getSimpleName();

    /**
     * Timestamp in nanoseconds for when the task was started.
     */
    private long mStart;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (BuildConfig.DEBUG) {
            mStart = System.nanoTime();
        }
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<V> vAsyncTaskResult) {
        super.onPostExecute(vAsyncTaskResult);

        // Measure how long this task took.
        if (BuildConfig.DEBUG) {
            long delta = System.nanoTime() - mStart;
            Logger.d(TAG, String.format("Task took %d ms", delta / 1000000));
        }
    }

    @Override
    protected AsyncTaskResult<V> doInBackground(T... params) {
        try {
            return new AsyncTaskResult<>(runTask(params));
        } catch (Throwable e) {
            Logger.d("FallibleAsyncTask", "runTask() failed", e);
            return new AsyncTaskResult<>(e);
        }
    }

    /**
     * A method that performs the required computation. If exception is thrown, a failure result
     * is delivered to the caller.
     *
     * @param params arguments passed to .execute() of AsyncTask
     *
     * @return any value V
     */
    abstract public V runTask(T... params);
}
