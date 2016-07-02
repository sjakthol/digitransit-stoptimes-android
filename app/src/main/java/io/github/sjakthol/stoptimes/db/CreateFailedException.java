package io.github.sjakthol.stoptimes.db;

/**
 * An exception that is thrown if the database creation / population fails.
 */
class CreateFailedException extends RuntimeException {
    CreateFailedException(Throwable throwable) {
        super(throwable);
    }
}
