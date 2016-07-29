package io.github.sjakthol.stoptimes.db.task;

import android.database.Cursor;
import android.location.Location;
import android.support.test.runner.AndroidJUnit4;
import io.github.sjakthol.stoptimes.db.DatabaseTestCase;
import io.github.sjakthol.stoptimes.digitransit.models.Stop;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(AndroidJUnit4.class)
public class GetNearbyStopsTaskTest extends DatabaseTestCase {

    @Test
    public void test_getNearby() throws InterruptedException {
        setup_insertData();

        Location loc = new Location("dummy");
        loc.setLongitude(24.93);
        loc.setLatitude(60.16);

        final CountDownLatch latch = new CountDownLatch(1);
        new GetNearbyStopsTask(mDbHelper) {
            @Override
            protected void onPostExecute(AsyncTaskResult<Cursor> result) {
                super.onPostExecute(result);

                assertThat("No errors occurred", result.getError(), is(not(notNullValue())));
                assertThat("Task succeeded", result.isSuccess(), is(true));

                Cursor nearby = result.getResult();
                assertThat("Two results returned", nearby.getCount(), is(2));

                assertThat(nearby.moveToPosition(0), is(true));

                Stop first = Stop.fromCursor(nearby);
                assertThat("Nearest stop first", first.getId(), is("HSL:1040602"));

                assertThat(nearby.moveToPosition(1), is(true));

                Stop second = Stop.fromCursor(nearby);
                assertThat("Second nearest second", second.getId(), is("HSL:6150218"));

                latch.countDown();
            }
        }.execute(loc, "2");

        latch.await();
    }
}
