package io.github.sjakthol.stoptimes.digitransit;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import io.github.sjakthol.stoptimes.digitransit.models.Departure;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import static io.github.sjakthol.stoptimes.digitransit.MockData.NUM_MOCK_DEPARTURES;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

@RunWith(AndroidJUnit4.class)
public class DigitransitApiTest {
    @Test
    public void test_buildGraphQLQueryNoVariables() throws Exception {
        JSONObject q = DigitransitApi.buildGraphQLQuery("query", null);
        assertThat("Query is correct", q.getString("query"), is("query"));
        assertThat("No variables", q.has("variables"), is(false));
    }

    @Test
    public void test_buildGraphQLQueryWithVariables() throws Exception {
        HashMap<String, String> variables = new HashMap<>();
        variables.put("a", "b");

        JSONObject q = DigitransitApi.buildGraphQLQuery("query", variables);
        assertThat("Query is correct", q.getString("query"), is("query"));
        assertThat("Has variables", q.has("variables"), is(true));

        JSONObject v = q.getJSONObject("variables");
        assertThat("variables contains |a|", v.has("a"), is(true));
        assertThat("value of |a| correct", v.getString("a"), is("b"));
    }

    @Test
    public void test_parseDepartureList() throws Exception {
        JSONObject resp = new JSONObject(MockData.MOCK_DEPARTURES);

        Vector<Departure> departures = DigitransitApi.parseDepartureList(resp);
        assertThat("correct number of departures", departures.size(), is(NUM_MOCK_DEPARTURES));
    }

    @Test
    public void test_getDepartures() throws Exception {
        final MockWebServer server = setup_mockServer(
            new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(MockData.MOCK_DEPARTURES)
        );

        DigitransitApi.API_GRAPHQL = server.url("GRAPHQL").toString();

        final CountDownLatch latch = new CountDownLatch(1);
        DigitransitApi.getDepartures(
            InstrumentationRegistry.getTargetContext(),
            "HSL:1234", 10,
            new DigitransitApi.DepartureResponseListener() {
                @Override
                public void onDeparturesAvailable(Vector<Departure> departures) {
                    assertThat("got departures", departures.size(), is(NUM_MOCK_DEPARTURES));

                    try {
                        RecordedRequest req = server.takeRequest();
                        assertThat("request path correct", req.getPath(), is("/GRAPHQL"));

                        JSONObject body = new JSONObject(req.getBody().readUtf8());
                        JSONObject var = body.getJSONObject("variables");

                        assertThat("body has correct stop", var.getString("stop"), is("HSL:1234"));
                        assertThat("body has correct limit", var.getString("departures"), is("10"));
                    } catch (Exception e) {
                        assertThat(e.toString(), false, is(true));
                    }

                    latch.countDown();
                }

                @Override
                public void onDepartureLoadError(VolleyError error) {
                    assertThat("unexpected error " + error.toString(), true, is(false));
                    latch.countDown();
                }
            }
        );

        latch.await();

    }

    @Test
    public void test_getDeparturesJsonError() throws Exception {
        final MockWebServer server = setup_mockServer(
            new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody("NOT JSON")
        );

        DigitransitApi.API_GRAPHQL = server.url("GRAPHQL").toString();

        final CountDownLatch latch = new CountDownLatch(1);
        DigitransitApi.getDepartures(
            InstrumentationRegistry.getTargetContext(),
            "HSL:1234", 10, new DigitransitApi.DepartureResponseListener() {
                @Override
                public void onDeparturesAvailable(Vector<Departure> departures) {
                    assertThat("unexpected success ", true, is(false));
                    latch.countDown();
                }

                @Override
                public void onDepartureLoadError(VolleyError error) {
                    assertThat("got JSON error", error.getCause() instanceof JSONException, is(true));
                    latch.countDown();
                }
            }
        );

        latch.await();
    }

    @Test
    public void test_getDeparturesServerError() throws Exception {
        final MockWebServer server = setup_mockServer(
            new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setStatus("500 Internal Server Error")
                .setBody("{}")
        );

        DigitransitApi.API_GRAPHQL = server.url("GRAPHQL").toString();

        final CountDownLatch latch = new CountDownLatch(1);
        DigitransitApi.getDepartures(
            InstrumentationRegistry.getTargetContext(),
            "HSL:1234", 10, new DigitransitApi.DepartureResponseListener() {
                @Override
                public void onDeparturesAvailable(Vector<Departure> departures) {
                    assertThat("unexpected success ", true, is(false));
                    latch.countDown();
                }

                @Override
                public void onDepartureLoadError(VolleyError error) {
                    assertThat("got error", error, is(notNullValue()));
                    latch.countDown();
                }
            }
        );

        latch.await();
    }

    @Test
    public void test_getStops() throws Exception {
        final MockWebServer server = setup_mockServer(
            new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(MockData.MOCK_STOPS)
        );

        DigitransitApi.API_GRAPHQL = server.url("GRAPHQL").toString();

        RequestFuture<JSONObject> res = DigitransitApi.getAllStops(InstrumentationRegistry.getTargetContext());
        JSONObject result = res.get();

        int nstops = result.getJSONObject("data").getJSONArray("stops").length();

        assertThat("got result", result, is(notNullValue()));
        assertThat("got 10 stops", nstops, is(MockData.NUM_MOCK_STOPS));
    }

    public MockWebServer setup_mockServer(MockResponse... responses) {
        MockWebServer server = new MockWebServer();

        for (MockResponse res : responses) {
            server.enqueue(res);
        }

        return server;
    }

}