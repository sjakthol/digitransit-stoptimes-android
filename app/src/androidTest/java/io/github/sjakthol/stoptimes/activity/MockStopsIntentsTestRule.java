package io.github.sjakthol.stoptimes.activity;

import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import io.github.sjakthol.stoptimes.db.StopListDatabaseHelper;
import io.github.sjakthol.stoptimes.db.task.UpdateDatabaseTask;
import io.github.sjakthol.stoptimes.digitransit.DigitransitApi;
import io.github.sjakthol.stoptimes.digitransit.MockData;
import io.github.sjakthol.stoptimes.utils.AsyncTaskResult;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.concurrent.CountDownLatch;


public class MockStopsIntentsTestRule<T extends Activity> extends IntentsTestRule<T> {
    public MockStopsIntentsTestRule(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();

        final CountDownLatch latch = new CountDownLatch(1);
        final Context ctx = InstrumentationRegistry.getTargetContext();

        // Mock the digitransit API
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody(MockData.MOCK_STOPS)
        );

        DigitransitApi.API_GRAPHQL = server.url("GRAPHQL").toString();

        // Trigger an update
        new UpdateDatabaseTask(ctx, new StopListDatabaseHelper(ctx)) {
            @Override
            protected void onPostExecute(AsyncTaskResult<Void> voidAsyncTaskResult) {
                super.onPostExecute(voidAsyncTaskResult);

                latch.countDown();
            }
        }.execute();

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
