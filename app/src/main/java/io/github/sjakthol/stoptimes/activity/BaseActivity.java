package io.github.sjakthol.stoptimes.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.activity.settings.SettingsActivity;
import io.github.sjakthol.stoptimes.utils.Helpers;
import io.github.sjakthol.stoptimes.utils.Logger;

/**
 * A base activity that handles generic tasks all activities in this app needs. This
 * includes menu creation, fragmenStopListAdapter.ActionHandlert management etc.
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    /**
     * An ID for the container view that holds the fragment shown in this view.
     */
    private int mFragmentContainer;

    /**
     * Create a new BaseActivity with the given container for fragments.
     *
     * @param fragmentContainer a resource ID for the fragment container in the activity
     */
    public BaseActivity(@IdRes int fragmentContainer) {
        super();

        mFragmentContainer = fragmentContainer;
    }

    /**
     * Adds the settings button to the main menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handles the settings item click. Other events are delegated to superclass.
     *
     * @param item the clicked item
     * @return true if handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Disables the usage of location data if user has removed the permission
     * while the app was not used.
     */
    @Override
    protected void onResume() {
        super.onResume();

        Helpers.maybeDisableUseLocation(this);
    }

    /**
     * Set the resource with given ID as the action bar of the activity.
     *
     * @param id and id for Toolbar resource
     * @throws ClassCastException if resource matching the given ID is not a Toolbar
     */
    protected void setToolbar(@IdRes int id) {
        // Get the shared toolbar
        Toolbar toolbar = (Toolbar) findViewById(id);
        if (toolbar == null) {
            Logger.w(TAG, "No toolbar present in the layout!");
            return;
        }

        // Set the shared toolbar.
        setSupportActionBar(toolbar);
    }

    /**
     * Renders the given fragment to the content view.
     *
     * @param frag the fragment to render
     */
    protected void setFragment(Fragment frag) {
        setFragment(frag, null);
    }

    /**
     * Renders the given fragment to the content view
     *
     * @param frag the fragment to render
     * @param tag  a tag to add to the fragment
     */
    protected void setFragment(Fragment frag, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(mFragmentContainer, frag, tag)
                .commit();
    }

    /**
     * Get the fragment with the given tag (if any)
     *
     * @param tag the tag to search
     * @return a Fragment that matches the tag or null
     */
    protected @Nullable Fragment getFragment(@NonNull String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag);
    }
}
