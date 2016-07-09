package io.github.sjakthol.stoptimes.db.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.runner.AndroidJUnit4;
import io.github.sjakthol.stoptimes.db.DatabaseTestCase;
import io.github.sjakthol.stoptimes.db.StopListContract;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(AndroidJUnit4.class)
public class GetFavoriteStopsTaskTest extends DatabaseTestCase {

    @Test
    public void test_getFavoritesNoFavorites() throws InterruptedException {
        setup_insertData();

        final CountDownLatch latch = new CountDownLatch(1);
        new GetFavoriteStopsTask(mDbHelper) {
            @Override
            protected void onPostExecute(AsyncTaskResult<Cursor> result) {
                super.onPostExecute(result);

                assertThat("No errors occurred", result.getError(), is(not(notNullValue())));
                assertThat("Task succeeded", result.isSuccess(), is(true));
                assertThat("No favorites at the beginning", result.getResult().getCount(), is(0));

                latch.countDown();
            }
        }.execute();

        latch.await();
    }

    @Test
    public void test_getFavoritesWithFavorites() throws InterruptedException {
        setup_insertData();

        // Add some favorites
        ContentValues data = new ContentValues();
        final String stopId = "HSL:1040602";

        data.put(StopListContract.Stop.COLUMN_NAME_GTFS_ID, stopId);
        data.put(StopListContract.Stop.COLUMN_NAME_IS_FAVORITE, 1);

        mDbHelper.getWritableDatabase().insert(StopListContract.Stop.FAVORITES_TABLE_NAME, null, data);

        final CountDownLatch latch = new CountDownLatch(1);
        new GetFavoriteStopsTask(mDbHelper) {
            @Override
            protected void onPostExecute(AsyncTaskResult<Cursor> result) {
                super.onPostExecute(result);

                assertThat("No errors occurred", result.getError(), is(not(notNullValue())));
                assertThat("Task succeeded", result.isSuccess(), is(true));

                Cursor cur = result.getResult();
                assertThat("Found the favorite", cur.getCount(), is(1));

                assertThat(cur.moveToFirst(), is(true));

                String id = cur.getString(cur.getColumnIndex(StopListContract.Stop.COLUMN_NAME_GTFS_ID));
                assertThat("the favorite stop is correct", id, is(stopId));

                cur.close();
                latch.countDown();
            }
        }.execute();

        latch.await();
    }
}