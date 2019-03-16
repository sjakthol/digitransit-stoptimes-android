package io.github.sjakthol.stoptimes.db.task;

/**
 * An exception that is thrown if the database creation / population fails.
 */
class UpdateFailedException extends RuntimeException {
    UpdateFailedException(Throwable throwable) {
        super(throwable);
    }
}
