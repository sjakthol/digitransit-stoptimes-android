package io.github.sjakthol.stoptimes.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import com.android.volley.NoConnectionError;
import com.android.volley.toolbox.RequestFuture;
import io.github.sjakthol.stoptimes.BuildConfig;
import io.github.sjakthol.stoptimes.digitransit.DigitransitApi;
import io.github.sjakthol.stoptimes.net.NetworkRequiredException;
import io.github.sjakthol.stoptimes.utils.Helpers;
import io.github.sjakthol.stoptimes.utils.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A helper for accessing the stop list database.
 */
public class StopListDatabaseHelper extends SQLiteOpenHelper {
    private final String TAG = getClass().getSimpleName();
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "stops.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ", ";
    private static final String SQL_CREATE_TABLE =
        "CREATE TABLE " + StopListContract.Stop.TABLE_NAME + " (" +
            StopListContract.Stop._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            StopListContract.Stop.COLUMN_NAME_GTFS_ID + TEXT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_CODE + TEXT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_LAT + REAL_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_LON + REAL_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE + INT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_PLATFORM_CODE + TEXT_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_IS_FAVORITE + INT_TYPE + " DEFAULT 0" +
    " )";

    private static final String SQL_INSERT_STOP =
        "INSERT INTO " + StopListContract.Stop.TABLE_NAME + " (" +
            StopListContract.Stop.COLUMN_NAME_GTFS_ID + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_CODE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_NAME + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_LAT + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_LON + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE + COMMA_SEP +
            StopListContract.Stop.COLUMN_NAME_PLATFORM_CODE + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + StopListContract.Stop.TABLE_NAME;

    /**
     * An application context used to create the database.
     */
    private Context mContext;

    /**
     * Create a new StopListDatabaseHelper for the given application context.
     *
     * @param context an application context to tie this database
     */
    public StopListDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) throws CreateFailedException {
        Logger.i(TAG, "Creating stop database");
        db.execSQL(SQL_CREATE_TABLE);

        JSONObject response = this.fetchStops();
        this.populateDatabase(db, response);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.i(TAG, "Upgrading db from v%d to v%d", oldVersion, newVersion);

        // TODO: Do something more sensible
        db.execSQL(SQL_DELETE_ENTRIES);
        this.onCreate(db);
    }

    /**
     * Retrieves the stop list from the Digitransit API
     *
     * @return an object that contains the stops in data.stops array.
     */
    private JSONObject fetchStops() {
        if (!Helpers.isConnected(mContext)) {
            Logger.w(TAG, "fetchStops() failed: no network connection available");
            throw new NetworkRequiredException();
        }

        Logger.i(TAG, "Fetching stops");
        RequestFuture<JSONObject> req = DigitransitApi.getAllStops(mContext);
        try {
            return req.get(DigitransitApi.WAIT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.e(TAG, "Didn't expect InterruptedException but got it", e);
            throw new CreateFailedException(e);
        } catch (ExecutionException e) {
            Logger.e(TAG, String.format("Request failed: %s", e.getMessage()), e);

            // Network connection is missing. Notify the caller that network connection is required.
            if (e.getCause() instanceof NoConnectionError) {
                Logger.i(TAG, "Request failed due to networking issues");
                throw new CreateFailedException(new NetworkRequiredException());
            }

            // Something else went wrong.
            throw new CreateFailedException(e);

        } catch (TimeoutException e) {
            Logger.e(TAG, "Request timed out", e);
            throw new CreateFailedException(e);
        }
    }

    /**
     * Populates the database with the given stop list response.
     *
     * @param response the JSON response from the Digitransit API
     */
    private void populateDatabase(SQLiteDatabase db, JSONObject response) {
        Logger.i(TAG, "Populating database");

        db.beginTransaction();
        SQLiteStatement stmt = db.compileStatement(SQL_INSERT_STOP);

        try {
            JSONArray stops = response.getJSONObject("data").getJSONArray("stops");
            Logger.d(TAG, "%d stops found", stops.length());

            for (int i = 0; i < stops.length(); i++) {
                stmt.clearBindings();

                JSONObject stop = stops.getJSONObject(i);
                if (BuildConfig.DEBUG) {
                    Logger.d(TAG, String.format("Inserting: %s", stop.toString()));
                }

                stmt.bindString(1, stop.getString("gtfsId"));
                stmt.bindString(2, stop.optString("code", ""));
                stmt.bindString(3, stop.getString("name"));
                stmt.bindDouble(4, stop.getDouble("lat"));
                stmt.bindDouble(5, stop.getDouble("lon"));
                stmt.bindLong(6, stop.getLong("vehicleType"));
                stmt.bindString(7, stop.getString("platformCode"));
                long id = stmt.executeInsert();
                if (id < 0) {
                    Logger.w(TAG, "Failed to insert %s", stop.getString("name"));
                }
            }

            db.setTransactionSuccessful();
        } catch (JSONException e) {
            Logger.e(TAG, "Failed to parse response", e);
            throw new CreateFailedException(e);
        } finally {
            db.endTransaction();
        }
    }
}