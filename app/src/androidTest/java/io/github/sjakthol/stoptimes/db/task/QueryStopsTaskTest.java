package io.github.sjakthol.stoptimes.db.task;

import android.database.Cursor;
import android.support.test.runner.AndroidJUnit4;
import io.github.sjakthol.stoptimes.db.DatabaseTestCase;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class QueryStopsTaskTest extends DatabaseTestCase {

    @Test
    public void test_queryStopsTask() throws InterruptedException {
        setup_insertData();

        final CountDownLatch latch = new CountDownLatch(1);
        new QueryStopsTask(mDbHelper) {
            @Override
            protected void onPostExecute(AsyncTaskResult<Cursor> result) {
                super.onPostExecute(result);

                assertThat("No errors occurred", result.getError(), is(not(notNullValue())));
                assertThat("Task succeeded", result.isSuccess(), is(true));

                Cursor res = result.getResult();
                assertThat("Got results", res, is(notNullValue()));
                assertThat("Found two stops", res.getCount(), is(2));

                res.close();
                latch.countDown();
            }
        }.execute("kamppi", "20", false);

        latch.await();
    }

    @Test
    public void test_queryStopsTaskLimiting() throws InterruptedException {
        setup_insertData();

        final CountDownLatch latch = new CountDownLatch(1);
        new QueryStopsTask(mDbHelper) {
            @Override
            protected void onPostExecute(AsyncTaskResult<Cursor> result) {
                super.onPostExecute(result);

                assertThat("No errors occurred", result.getError(), is(not(notNullValue())));
                assertThat("Task succeeded", result.isSuccess(), is(true));

                Cursor res = result.getResult();
                assertThat("Got results", res, is(notNullValue()));
                assertThat("Only two stops returned", res.getCount(), is(2));

                res.close();
                latch.countDown();
            }
        }.execute("a", "2", false);

        latch.await();
    }
}