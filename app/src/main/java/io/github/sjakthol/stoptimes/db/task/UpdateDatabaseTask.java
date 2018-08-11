package io.github.sjakthol.stoptimes.db.task;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import com.android.volley.NoConnectionError;
import com.android.volley.toolbox.RequestFuture;
import io.github.sjakthol.stoptimes.db.StopListContract;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
import io.github.sjakthol.stoptimes.digitransit.DigitransitApi;
import io.github.sjakthol.stoptimes.digitransit.models.Stop;
import io.github.sjakthol.stoptimes.utils.Logger;
import io.github.sjakthol.stoptimes.utils.NetworkRequiredException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.github.sjakthol.stoptimes.db.StopListContract.Stop.COLUMN_NAME_GTFS_ID;
import static io.github.sjakthol.stoptimes.db.StopListContract.Stop.STOPS_TABLE_NAME;

/**
 * A task that updates the contents of the stop list database.
 */
public class UpdateDatabaseTask extends DatabaseTask<Void, Void> {
    private static final String TAG = UpdateDatabaseTask.class.getSimpleName();
    private static final String TEMP_TABLE_NAME = "NEW_STOPS";
    private static final String CREATE_TEMP_TABLE =
        "CREATE TEMPORARY TABLE IF NOT EXISTS " + TEMP_TABLE_NAME + "(" +
            COLUMN_NAME_GTFS_ID + " TEXT PRIMARY KEY" +
        ");";
    private static final String DELETE_REMOVED_COND =
        COLUMN_NAME_GTFS_ID + " NOT IN (" +
            "SELECT " + COLUMN_NAME_GTFS_ID + " FROM " + TEMP_TABLE_NAME + "" +
        ")";

    private final Context mContext;

    /**
     * Create a DatabaseTask for the given StopListDatabaseHelper.
     *
     * @param context the application context
     * @param dbHelper the database helper instance
     */
    public UpdateDatabaseTask(Context context, StopListDatabaseHelper dbHelper) {
        super(dbHelper);

        mContext = context;
    }

    @Override
    public Void runTask(SQLiteDatabase db, Void... unused) {
        // Fetch stops
        Map<String, Vector<Stop>> data = this._fetchStops();
        Vector<Stop> stops = data.get("stops");
        Vector<Stop> stations = data.get("stations");

        // First, add stations to the database
        this.updateDatabase(db, stations, StopListContract.Stop.STATIONS_TABLE_NAME);

        // Then, add the stops
        this.updateDatabase(db, stops, StopListContract.Stop.STOPS_TABLE_NAME);

        return null;
    }

    /**
     * Retrieves the stop list from the Digitransit API
     *
     * @return an array with stop object
     */
    public Map<String, Vector<Stop>> _fetchStops() {
        Logger.i(TAG, "Fetching stops");
        try {
            RequestFuture<JSONObject> req = DigitransitApi.getAllStops(mContext);
            JSONObject response = req.get(DigitransitApi.WAIT_TIMEOUT, TimeUnit.SECONDS);
            JSONObject data = response.getJSONObject("data");

            // Parse stops and stations from the response
            Vector<Stop> stops = parseStopList(data.getJSONArray("stops"));
            Vector<Stop> stations = parseStopList(data.getJSONArray("stations"));
            Logger.i(TAG, "Found %d stops, %d stations", stops.size(), stations.size());

            Map<String, Vector<Stop>> result = new HashMap<>(2);
            result.put("stops", stops);
            result.put("stations", stations);
            return result;

        } catch (InterruptedException e) {
            Logger.e(TAG, "Unexpected InterruptedException", e);
            throw new UpdateFailedException(e);
        } catch (ExecutionException e) {
            Logger.e(TAG, String.format("Request failed: %s", e.getMessage()), e);

            // Network connection is missing. Notify the caller that network connection is required.
            if (e.getCause() instanceof NoConnectionError) {
                Logger.i(TAG, "Request failed due to networking issues");
                throw new UpdateFailedException(new NetworkRequiredException());
            }

            // Something else went wrong.
            throw new UpdateFailedException(e);

        } catch (TimeoutException e) {
            Logger.e(TAG, "Request timed out", e);
            throw new UpdateFailedException(e);
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to parse response", e);
            throw new UpdateFailedException(e);
        }
    }

    /**
     * Parses the response received from Digitransit API
     *
     * @param stops a JSON Array of Stop objects
     * @return a Vector of parsed Stop objects
     * @throws JSONException if JSON cannot be parsed
     */
    public static Vector<Stop> parseStopList(JSONArray stops) throws JSONException {
        int numstops = stops.length();
        Vector<Stop> result = new Vector<>(numstops);
        for (int i = 0; i < stops.length(); i++) {
            result.addElement(Stop.fromJson(stops.getJSONObject(i)));
        }

        return result;
    }

    /**
     * Updates the stop list database. This includes:
     * - add any new stops to the database
     * - update details for any existing stops in the database
     * - remove removed stops from the database
     *
     * @param db the database to update
     * @param stops the list of stops
     * @param table the SQL table these should be added to (stations vs stops)
     */
    private void updateDatabase(SQLiteDatabase db, Vector<Stop> stops, String table) {
        db.beginTransaction();

        int deleted = 0;
        int updated = 0;
        int added = 0;
        int errors = 0;

        try {
            // Create temporary table for comparing the IDs of the two datasets.
            db.execSQL(CREATE_TEMP_TABLE);

            for (Stop stop : stops) {
                Location loc = stop.getLocation();

                ContentValues data = new ContentValues();
                data.put(COLUMN_NAME_GTFS_ID, stop.getId());
                data.put(StopListContract.Stop.COLUMN_NAME_NAME, stop.getName());
                data.put(StopListContract.Stop.COLUMN_NAME_CODE, stop.getCode());
                data.put(StopListContract.Stop.COLUMN_NAME_LAT, loc.getLatitude());
                data.put(StopListContract.Stop.COLUMN_NAME_LON, loc.getLongitude());
                data.put(StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE, Stop.typeToCode(stop.getVehicleType()));
                data.put(StopListContract.Stop.COLUMN_NAME_PLATFORM_CODE, stop.getPlatform());
                data.put(StopListContract.Stop.COLUMN_NAME_LOCATION_TYPE, stop.getLocationType());
                data.put(StopListContract.Stop.COLUMN_NAME_PARENT_STATION, stop.getParentStation());

                // Try updating an existing entry first
                int changed = db.update(
                    table,
                    data,
                    COLUMN_NAME_GTFS_ID + " = ?",
                    new String[]{stop.getId()}
                );

                updated += changed;

                if (changed == 0) {
                    // The stop was not in the db; insert it.
                    if (db.insert(table, null, data) == -1) {
                        Logger.w(TAG, "Failed to insert stop %s", stop.getId());
                        errors += 1;
                    } else {
                        added += 1;
                    }
                }

                // Also insert the ID into the temp table for later
                ContentValues id = new ContentValues();
                id.put(COLUMN_NAME_GTFS_ID, stop.getId());

                if (db.insert(TEMP_TABLE_NAME, null, id) == -1) {
                    Logger.w(TAG, "Failed to insert %s into temporary table for removal comparison", stop.getId());
                    errors += 1;
                }
            }

            // Delete removed stops
            deleted = db.delete(table, DELETE_REMOVED_COND, null);

            db.execSQL("DROP TABLE " + TEMP_TABLE_NAME);

            // And commit the transaction
            db.setTransactionSuccessful();
        } finally {
            Logger.i(TAG, "Stats: added=%d, updated=%d, deleted=%d, errors=%d", added, updated, deleted, errors);
            db.endTransaction();
        }
    }
}
