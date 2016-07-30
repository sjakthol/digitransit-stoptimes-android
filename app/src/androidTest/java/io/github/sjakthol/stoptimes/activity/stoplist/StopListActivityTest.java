package io.github.sjakthol.stoptimes.activity.stoplist;

import android.location.Location;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.activity.MockStopsIntentsTestRule;
import io.github.sjakthol.stoptimes.activity.departures.DepartureListActivity;
import io.github.sjakthol.stoptimes.activity.settings.SettingsActivity;
import io.github.sjakthol.stoptimes.utils.Helpers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class StopListActivityTest {

    @Rule
    public MockStopsIntentsTestRule<StopListActivity> mActivityRule =
        new MockStopsIntentsTestRule<>(StopListActivity.class);

    @Test
    public void test_initialLaunchNoFavorites() throws Exception {
        String str = InstrumentationRegistry
            .getTargetContext()
            .getString(R.string.stoplist_empty_favorites_title);
        onView(withText(containsString(str))).check(matches(isDisplayed()));
    }

    @Test
    public void test_launchSearch() throws Exception {
        onView(withId(R.id.menu_search)).perform(click());
        intending(hasComponent(StopSearchActivity.class.getName()));
    }

    @Test
    public void test_nearbyDisabled() throws Exception {
        // Disable location usage
        Helpers.setUseLocation(InstrumentationRegistry.getTargetContext(), false);

        // Click on the Nearby button
        onView(withId(R.id.bottom_nearby)).perform(click());

        // Ensure snackbar notification is displayed
        onView(allOf(withId(android.support.design.R.id.snackbar_text), withText(R.string.location_disabled)))
            .check(matches(isDisplayed()));

        // Check that clicking the settings link in the snackbar launches
        // settings activity
        onView(allOf(withId(android.support.design.R.id.snackbar_action), withText(R.string.settings)))
            .perform(click());

        intending(hasComponent(SettingsActivity.class.getName()));
    }

    @Test
    public void test_nearbyEnabled() throws Exception {
        // Enable location usage
        Helpers.setUseLocation(InstrumentationRegistry.getTargetContext(), true);

        // Click on the nearby tab
        onView(withId(R.id.bottom_nearby)).perform(click());

        // Ensure loading message is shown
        onView(allOf(withId(R.id.loading_text), withText(R.string.acquiring_location)))
            .check(matches(isDisplayed()));

        // Inject location result (Lauttasaari stop)
        Location loc = new Location("mock");
        loc.setLatitude(60.160069);
        loc.setLongitude(24.880192);
        mActivityRule.getActivity().onLocationUpdated(loc);

        // Check that the list is displayed with the Lauttasaari stop as the first
        // one (closest)
        onView(withId(R.id.stop_list_recycler))
            .check(matches(allOf(isDisplayed(), hasDescendant(withText("Lauttasaari")))));

        // Click on the first item (Lauttasaari, HSL:1310602)
        onView(withId(R.id.stop_list_recycler)).perform(actionOnItemAtPosition(0, click()));

        intending(
            allOf(
                hasComponent(DepartureListActivity.class.getName()),
                hasExtra(DepartureListActivity.EXTRA_STOP_ID, "HSL:1310602"),
                hasExtra(DepartureListActivity.EXTRA_STOP_NAME, "Lauttasaari")
            )
        );
    }
}