package io.github.sjakthol.stoptimes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.test.InstrumentationTestCase;
import org.junit.Before;

import static io.github.sjakthol.stoptimes.db.StopListContract.Stop.COLUMN_NAME_GTFS_ID;
import static io.github.sjakthol.stoptimes.db.StopListContract.Stop.STOPS_TABLE_NAME;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


public class DatabaseTestCase extends InstrumentationTestCase {
    protected StopListDatabaseHelper mDbHelper;

    @Before
    public void setup_mockedDbHelper() {
        Context context = InstrumentationRegistry.getTargetContext();
        assertThat("got context", context, notNullValue());

        mDbHelper = new StopListDatabaseHelper(context);

        assertThat("got helper", mDbHelper, notNullValue());

        // Populate the db
        mDbHelper.getWritableDatabase();
    }

    protected void setup_insertData() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues data = new ContentValues();
        data.put(COLUMN_NAME_GTFS_ID, "HSL:1040602");
        data.put(StopListContract.Stop.COLUMN_NAME_NAME, "Kamppi");
        data.put(StopListContract.Stop.COLUMN_NAME_CODE, "0013");
        data.put(StopListContract.Stop.COLUMN_NAME_LAT, 60.168901);
        data.put(StopListContract.Stop.COLUMN_NAME_LON, 24.931515);
        data.put(StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE, 1);
        data.put(StopListContract.Stop.COLUMN_NAME_PLATFORM_CODE, (String) null);
        db.insert(STOPS_TABLE_NAME, null, data);


        data.put(COLUMN_NAME_GTFS_ID, "HSL:6150218");
        data.put(StopListContract.Stop.COLUMN_NAME_NAME, "Kamppi 2");
        data.put(StopListContract.Stop.COLUMN_NAME_CODE, "Ki1518");
        data.put(StopListContract.Stop.COLUMN_NAME_LAT, 60.201517);
        data.put(StopListContract.Stop.COLUMN_NAME_LON, 24.375928);
        data.put(StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE, 3);
        data.put(StopListContract.Stop.COLUMN_NAME_PLATFORM_CODE, (String) null);
        db.insert(STOPS_TABLE_NAME, null, data);

        data.put(COLUMN_NAME_GTFS_ID, "HSL:2111552");
        data.put(StopListContract.Stop.COLUMN_NAME_NAME, "Leppävaara");
        data.put(StopListContract.Stop.COLUMN_NAME_CODE, "E1058");
        data.put(StopListContract.Stop.COLUMN_NAME_LAT, 219477);
        data.put(StopListContract.Stop.COLUMN_NAME_LON, 813224);
        data.put(StopListContract.Stop.COLUMN_NAME_VEHICLE_TYPE, 109);
        data.put(StopListContract.Stop.COLUMN_NAME_PLATFORM_CODE, "2");
        db.insert(STOPS_TABLE_NAME, null, data);
    }
}
