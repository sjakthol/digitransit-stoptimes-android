package io.github.sjakthol.stoptimes.db;


import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.github.sjakthol.stoptimes.db.StopListContract.Stop.STOPS_TABLE_NAME;

@RunWith(AndroidJUnit4.class)
public class StopListDatabaseHelperTest extends DatabaseTestCase {

    @Test
    public void test_createDb() {
        mDbHelper.getWritableDatabase().rawQuery("SELECT * FROM " + STOPS_TABLE_NAME, null);
        // If we got this far, the database seems to be correctly initialized
    }
}
