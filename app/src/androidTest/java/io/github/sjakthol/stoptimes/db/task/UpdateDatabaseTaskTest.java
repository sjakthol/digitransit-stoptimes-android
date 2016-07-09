package io.github.sjakthol.stoptimes.db.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import io.github.sjakthol.stoptimes.db.DatabaseTestCase;
import io.github.sjakthol.stoptimes.db.StopListContract;
import io.github.sjakthol.stoptimes.digitransit.models.Stop;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import static io.github.sjakthol.stoptimes.db.StopListContract.Stop.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class UpdateDatabaseTaskTest extends DatabaseTestCase {

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

    public interface AssertHandler {
        /**
         * Perform any assertions to check the update was successful.
         *
         * @param db a read-only db to use.
         */
        void assertResults(SQLiteDatabase db);
    }

    /**
     * A method that triggers an update and triggers the given handler
     * that can be used to assert the update was successful.
     *
     * Initially, the DB will have three stops in the db:
     * - HSL:1040602 - Kamppi
     * - HSL:6150218 - Kamppi 2
     * - HSL:2111552 - Leppävaara (also favorite)
     *
     * This method will update the DB with the following stops:
     * - HSL:1234567 - New Stop
     * - HSL:2111552 - Updated Leppävaara
     * - HSL:6150218 - Kamppi 2
     *
     * When the handler is called, query the db and assert that
     * required updates were performed.
     * @param handler the handler that performs any assertions
     */
    public void runUpdateTest(final AssertHandler handler) throws JSONException, InterruptedException {
        setup_insertData();

        /**
         * Initially we have three stops in the db:
         * - HSL:1040602 - Kamppi
         * - HSL:6150218 - Kamppi 2
         * - HSL:2111552 - Leppävaara (also favorite)
         *
         * In the test we will update the DB with the following stops:
         * - HSL:1234567 - New Stop
         * - HSL:2111552 - Updated Leppävaara
         * - HSL:6150218 - Kamppi 2
         *
         * Here we assert that:
         * - Kamppi is removed from the db
         * - Leppävaara is updated with the new name
         * - Kamppi 2 is unchanged
         * - New Stop is added to the db
         * - Updated Leppävaara is still a favorite
         */

        // Add one new stop
        String newStopData = "{" +
                "\"platformCode\": null,\n" +
                "\"vehicleType\": 1,\n" +
                "\"code\": \"0013\",\n" +
                "\"lon\": 24.931515,\n" +
                "\"lat\": 60.168901,\n" +
                "\"name\": \"New Stop\",\n" +
                "\"gtfsId\": \"HSL:1234567\"\n" +
                "}";

        final Stop newStop = Stop.fromJson(new JSONObject(newStopData));

        // Update Leppävaara
        String updatedStopData = "{" +
                "\"platformCode\": null,\n" +
                "\"vehicleType\": 1,\n" +
                "\"code\": \"0013\",\n" +
                "\"lon\": 24.931515,\n" +
                "\"lat\": 60.168901,\n" +
                "\"name\": \"Updated Leppävaara\",\n" +
                "\"gtfsId\": \"HSL:2111552\"\n" +
                "}";

        final Stop updatedStop = Stop.fromJson(new JSONObject(updatedStopData));

        // Keep Kamppi 2 unchanged
        String oldStopData = "{\n" +
                "\"platformCode\": null,\n" +
                "\"vehicleType\": 3,\n" +
                "\"code\": \"Ki1518\",\n" +
                "\"lon\": 24.375928,\n" +
                "\"lat\": 60.201517,\n" +
                "\"name\": \"Kamppi 2\",\n" +
                "\"gtfsId\": \"HSL:6150218\"\n" +
                "}";

        final Stop oldStop = Stop.fromJson(new JSONObject(oldStopData));

        // Add Leppävaara to favorites
        ContentValues data = new ContentValues();
        final String stopId = "HSL:2111552";

        data.put(StopListContract.Stop.COLUMN_NAME_GTFS_ID, stopId);
        data.put(StopListContract.Stop.COLUMN_NAME_IS_FAVORITE, 1);

        mDbHelper.getWritableDatabase().insert(StopListContract.Stop.FAVORITES_TABLE_NAME, null, data);

        final CountDownLatch latch = new CountDownLatch(1);
        new UpdateDatabaseTask(InstrumentationRegistry.getTargetContext(), mDbHelper) {
            @Override
            public Vector<Stop> _fetchStops() {
                Vector<Stop> stops = new Vector<>(3);
                stops.addElement(newStop);
                stops.addElement(oldStop);
                stops.addElement(updatedStop);
                return stops;
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<Void> result) {
                super.onPostExecute(result);

                // First do some basic checks that everything went OK
                assertThat("No errors occurred", result.getError(), CoreMatchers.is(not(notNullValue())));
                assertThat("Task succeeded", result.isSuccess(), CoreMatchers.is(true));

                // Then let the test perform assertions
                handler.assertResults(mDbHelper.getReadableDatabase());

                // And unblock the method
                latch.countDown();
            }
        }.execute();

        latch.await();
    }

    @Test
    public void test_parseResponse() throws JSONException {
        Vector<Stop> stops = UpdateDatabaseTask.parseResponse(new JSONObject(SAMPLE_RESPONSE));
        assertThat("Three stops parsed", stops.size(), is(3));
    }

    @Test
    public void test_updateDeletesMissingStops() throws JSONException, InterruptedException {
        runUpdateTest(new AssertHandler() {
            @Override
            public void assertResults(SQLiteDatabase db) {
                // Check that Kamppi was removed since it was not in the new data
                Cursor cur = db.rawQuery(
                    "SELECT " +
                        COLUMN_NAME_NAME +
                    " FROM " +
                        STOPS_TABLE_NAME +
                    " WHERE " + COLUMN_NAME_GTFS_ID + " = 'HSL:1040602';",
                    null
                );
                assertThat("Stop not in the updated list was removed ", cur.getCount(), is(0));
                cur.close();
            }
        });
    }

    @Test
    public void test_updateUpdatesExistingStops() throws JSONException, InterruptedException {
        runUpdateTest(new AssertHandler() {
            @Override
            public void assertResults(SQLiteDatabase db) {
                // #2: Check that Leppävaara was updated
                Cursor cur = db.rawQuery(
                    "SELECT " +
                        COLUMN_NAME_NAME +
                    " FROM " +
                        STOPS_TABLE_NAME +
                    " WHERE " + COLUMN_NAME_GTFS_ID + " = 'HSL:2111552';",
                    null
                );

                assertThat("Existing stop still in db", cur.getCount(), is(1));
                assertThat(cur.moveToFirst(), is(true));
                assertThat("Existing stop was updated", cur.getString(0), is("Updated Leppävaara"));
                cur.close();
            }
        });
    }

    @Test
    public void test_updateAddsNewStops() throws JSONException, InterruptedException {
        runUpdateTest(new AssertHandler() {
            @Override
            public void assertResults(SQLiteDatabase db) {
                // #3: Check that the new stop was added
                Cursor cur = db.rawQuery(
                    "SELECT " +
                        COLUMN_NAME_NAME +
                    " FROM " +
                        STOPS_TABLE_NAME +
                    " WHERE " + COLUMN_NAME_GTFS_ID + " = 'HSL:1234567';",
                    null
                );
                assertThat("New stop added", cur.getCount(), is(1));
                assertThat(cur.moveToFirst(), is(true));
                assertThat("New stop has correct name", cur.getString(0), is("New Stop"));
                cur.close();
            }
        });
    }

    @Test
    public void test_updateKeepsUnchangedStops() throws JSONException, InterruptedException {
        runUpdateTest(new AssertHandler() {
            @Override
            public void assertResults(SQLiteDatabase db) {
                // #4: Check that non-changed stops (Kamppi 2) are still there
                Cursor cur = db.rawQuery(
                    "SELECT " +
                        COLUMN_NAME_NAME +
                    " FROM " +
                        STOPS_TABLE_NAME +
                    " WHERE " + COLUMN_NAME_GTFS_ID + " = 'HSL:6150218';",
                    null
                );
                assertThat("New stop added", cur.getCount(), is(1));
                assertThat(cur.moveToFirst(), is(true));
                assertThat("New stop has correct name", cur.getString(0), is("Kamppi 2"));
                cur.close();
            }
        });
    }

    @Test
    public void test_updateRetainsFavoriteStatus() throws JSONException, InterruptedException {
        runUpdateTest(new AssertHandler() {
            @Override
            public void assertResults(SQLiteDatabase db) {
                Cursor cur = db.rawQuery(
                        "SELECT * FROM " + FAVORITES_TABLE_NAME +
                                " WHERE " + COLUMN_NAME_GTFS_ID + " = 'HSL:2111552';",
                        null
                );

                assertThat("Favorite status kept", cur.getCount(), is(1));
                cur.close();
            }
        });
    }
}
