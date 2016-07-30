package io.github.sjakthol.stoptimes.activity.stoplist;

import android.widget.EditText;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.activity.MockStopsIntentsTestRule;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;


public class StopSearchActivityTest {
    @Rule
    public MockStopsIntentsTestRule<StopSearchActivity> mActivityRule =
        new MockStopsIntentsTestRule<>(StopSearchActivity.class);

    @Test
    public void test_initialMessage() {
        onView(withText(R.string.stoplist_search_title))
            .check(matches(isDisplayed()));
    }

    @Test
    public void test_searchStops() {
        onView(isAssignableFrom(EditText.class)).perform(typeText("lautta"));
        onView(withId(R.id.stop_list_recycler))
                .check(matches(isDisplayed()))
                .check(matches(hasDescendant(withText("Lauttasaari"))));    }

    @Test
    public void test_noResults() {
        onView(isAssignableFrom(EditText.class)).perform(typeText("gibberish"));
        onView(withText(R.string.stoplist_search_empty_title))
                .check(matches(isDisplayed()));
    }
}