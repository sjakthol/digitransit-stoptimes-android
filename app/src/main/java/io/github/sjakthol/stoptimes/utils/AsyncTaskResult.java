package io.github.sjakthol.stoptimes.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A wrapper for DatabaseTask errors.
 */
public class AsyncTaskResult<V> {
    private Throwable mError;
    private V mResult;

    /**
     * Create a failure result caused by given Throwable.
     *
     * @param cause the cause of the error
     */
    AsyncTaskResult(@NonNull Throwable cause) {
        mError = cause;
    }

    /**
     * Create a success result with the given value.
     *
     * @param result the result of the task
     */
    AsyncTaskResult(V result) {
        mResult = result;
    }

    /**
     * Get the error that caused the task to fail.
     *
     * @return a Throwable that caused the task to fail or null if no error occurred
     */
    @Nullable
    public Throwable getError() {
        return mError;
    }

    /**
     * Get the result of a successful task.
     *
     * @return the result of a successful task (if any)
     */
    @Nullable
    public V getResult() {
        return mResult;
    }

    /**
     * Check if the task was successful.
     *
     * @return true if task was successful, false if it failed
     */
    public boolean isSuccess() {
        return mError == null;
    }
}
