package io.github.sjakthol.stoptimes.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityTestCase;
import android.test.InstrumentationTestCase;
import io.github.sjakthol.stoptimes.db.task.GetFavoriteStopsTask;
import io.github.sjakthol.stoptimes.db.task.QueryStopsTask;
import io.github.sjakthol.stoptimes.db.task.UpdateFavoriteStatusTask;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import io.github.sjakthol.stoptimes.utils.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static io.github.sjakthol.stoptimes.db.StopListContract.Stop.COLUMN_NAME_GTFS_ID;
import static io.github.sjakthol.stoptimes.db.StopListContract.Stop.FAVORITES_TABLE_NAME;
import static io.github.sjakthol.stoptimes.db.StopListContract.Stop.STOPS_TABLE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class StopListDatabaseTest extends InstrumentationTestCase {

    private static final String SAMPLE_RESPONSE = "{\n" +
            "  \"data\": {\n" +
            "    \"stops\": [\n" +
            "      {\n" +
            "        \"platformCode\": \"2\",\n" +
            "        \"vehicleType\": 109,\n" +
            "        \"code\": \"E1058\",\n" +
            "        \"lon\": 24.813224,\n" +
            "        \"lat\": 60.219477,\n" +
            "        \"name\": \"Leppävaara\",\n" +
            "        \"gtfsId\": \"HSL:2111552\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"platformCode\": null,\n" +
            "        \"vehicleType\": 3,\n" +
            "        \"code\": \"Ki1518\",\n" +
            "        \"lon\": 24.375928,\n" +
            "        \"lat\": 60.201517,\n" +
            "        \"name\": \"Kamppi 2\",\n" +
            "        \"gtfsId\": \"HSL:6150218\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"platformCode\": null,\n" +
            "        \"vehicleType\": 1,\n" +
            "        \"code\": \"0013\",\n" +
            "        \"lon\": 24.931515,\n" +
            "        \"lat\": 60.168901,\n" +
            "        \"name\": \"Kamppi\",\n" +
            "        \"gtfsId\": \"HSL:1040602\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}\n";

    StopListDatabaseHelper mDbHelper;

    @Before
    public void setup_mockedDbHelper() {
        Context context = InstrumentationRegistry.getTargetContext();
        assertThat("got context", context, notNullValue());

        mDbHelper = new StopListDatabaseHelper(context) {
            @Override
            protected JSONObject fetchStops() {
                try {
                    return new JSONObject(SAMPLE_RESPONSE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

        assertThat("got helper", mDbHelper, notNullValue());

        // Populate the db
        mDbHelper.getWritableDatabase();
    }

    @Test
    public void test_createDb() {
        Cursor results = mDbHelper.getWritableDatabase().rawQuery("SELECT * FROM " + STOPS_TABLE_NAME, null);
        assertThat("3 stops inserted", results.getCount(), is(3));
        results.close();
    }

    @Test
    public void test_getFavoritesNoFavorites() throws InterruptedException {
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
    public void test_setFavoriteStatusToTrue() throws InterruptedException {
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

    @Test
    public void test_queryStopsTask() throws InterruptedException {
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
        }.execute("kamppi", "20");

        latch.await();
    }

    @Test
    public void test_queryStopsTaskLimiting() throws InterruptedException {
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
        }.execute("a", "2");

        latch.await();
    }
}
