package io.github.sjakthol.stoptimes.activity.update;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.activity.BaseActivity;
import io.github.sjakthol.stoptimes.activity.generic.LoadingFragment;
import io.github.sjakthol.stoptimes.activity.generic.NoConnectionFragment;
import io.github.sjakthol.stoptimes.activity.generic.UnexpectedErrorFragment;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
import io.github.sjakthol.stoptimes.db.task.UpdateDatabaseTask;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import io.github.sjakthol.stoptimes.utils.Logger;
import io.github.sjakthol.stoptimes.utils.NetworkRequiredException;

/**
 * An activity that WILL update the stop database from the Digitransit API.
 */
public class StopDatabaseUpdateActivity extends BaseActivity implements NoConnectionFragment.OnConnectionAvailable {
    private static final String TAG = StopDatabaseUpdateActivity.class.getSimpleName();
    private static final String FRAG_LOADING = "FRAG_LOADING";
    private StopListDatabaseHelper mDbHelper;

    public StopDatabaseUpdateActivity() {
        super(R.id.main_content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stop_database_update);
        setToolbar();

        mDbHelper = new StopListDatabaseHelper(this);

        startUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    private void showLoadingIndicator() {
        LoadingFragment frag =
            LoadingFragment.createWithMessage(getString(R.string.db_update_loading_message));
        setFragment(frag, FRAG_LOADING);
    }

    @SuppressLint("StaticFieldLeak")
    private void startUpdate() {
        Logger.i(TAG, "Starting update");
        showLoadingIndicator();

        new UpdateDatabaseTask(this, mDbHelper) {
            @Override
            protected void onPostExecute(AsyncTaskResult<Void> res) {
                super.onPostExecute(res);

                if (res.isSuccess()) {
                    Logger.i(TAG, "Update finished succesfully");
                    setDbUpdateTimestamp();
                    finish();
                } else {
                    Throwable err = res.getError();
                    if (err.getCause() instanceof NetworkRequiredException) {
                        Logger.i(TAG, "No network connectivity; showing dialog");
                        setFragment(new NoConnectionFragment());
                    } else {
                        Logger.e(TAG, "Update failed", err);
                        setFragment(new UnexpectedErrorFragment());
                    }
                }
            }
        }.execute();
    }

    private void setDbUpdateTimestamp() {
        String key = getString(R.string.stopdb_last_update);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, System.currentTimeMillis());
        editor.apply();
    }

    @Override
    public void onConnectionAvailable() {
        Logger.i(TAG, "Got network; updating");
        startUpdate();
    }
}
