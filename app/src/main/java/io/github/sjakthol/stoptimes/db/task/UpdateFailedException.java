package io.github.sjakthol.stoptimes.db.task;

/**
 * An exception that is thrown if the database creation / population fails.
 */
public class UpdateFailedException extends RuntimeException {
    UpdateFailedException(Throwable throwable) {
        super(throwable);
    }
}
