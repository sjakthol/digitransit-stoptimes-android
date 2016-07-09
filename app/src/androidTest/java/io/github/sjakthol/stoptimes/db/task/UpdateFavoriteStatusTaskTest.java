package io.github.sjakthol.stoptimes.db.task;

import android.database.Cursor;
import android.support.test.runner.AndroidJUnit4;
import io.github.sjakthol.stoptimes.db.DatabaseTestCase;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static io.github.sjakthol.stoptimes.db.StopListContract.Stop.FAVORITES_TABLE_NAME;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class UpdateFavoriteStatusTaskTest extends DatabaseTestCase {
    @Test
    public void test_setFavoriteStatusToTrue() throws InterruptedException {
        setup_insertData();

        final CountDownLatch latch = new CountDownLatch(1);
        final String stopId = "HSL:1040602";

        new UpdateFavoriteStatusTask(mDbHelper) {
            @Override
            protected void onPostExecute(AsyncTaskResult<Void> result) {
                super.onPostExecute(result);

                assertThat("No errors occurred", result.getError(), is(not(notNullValue())));
                assertThat("Task succeeded", result.isSuccess(), is(true));

                Cursor res = mDbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + FAVORITES_TABLE_NAME, null);
                assertThat("One favorite found", res.getCount(), is(1));

                assertThat(res.moveToFirst(), is(true));
                assertThat("the favorite stop is correct", res.getString(0), is(stopId));

                res.close();
                latch.countDown();
            }
        }.execute(stopId, true);

        latch.await();
    }

    @Test
    public void test_removeFavoriteStatus() throws InterruptedException {
        setup_insertData();

        // Insert the stop into favorites
        final String stopId = "HSL:1040602";
        mDbHelper.getWritableDatabase()
                .execSQL(String.format("INSERT INTO %s VALUES('%s', 1)", FAVORITES_TABLE_NAME, stopId));

        // Remove it using UpdateFavoritesTask
        final CountDownLatch latch = new CountDownLatch(1);
        new UpdateFavoriteStatusTask(mDbHelper) {
            @Override
            protected void onPostExecute(AsyncTaskResult<Void> result) {
                super.onPostExecute(result);

                assertThat("No errors occurred", result.getError(), is(not(notNullValue())));
                assertThat("Task succeeded", result.isSuccess(), is(true));

                Cursor res = mDbHelper.getReadableDatabase().rawQuery("SELECT * FROM " + FAVORITES_TABLE_NAME, null);
                assertThat("No favorites left", res.getCount(), is(0));

                res.close();
                latch.countDown();
            }
        }.execute(stopId, false);

        latch.await();
    }
}