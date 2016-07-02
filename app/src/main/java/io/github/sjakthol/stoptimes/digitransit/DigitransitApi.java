package io.github.sjakthol.stoptimes.digitransit;

import android.content.Context;
import android.support.annotation.Nullable;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import io.github.sjakthol.stoptimes.digitransit.models.Departure;
import io.github.sjakthol.stoptimes.net.VolleyWrapper;
import io.github.sjakthol.stoptimes.utils.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Vector;

/**
 * A class that allows the Digitransit API to be accessed asynchronously.
 */
public class DigitransitApi {
    private static final String TAG = "DigitransitApi";
    private static final String API_HOST = "https://api.digitransit.fi";
    private static final String API_GRAPHQL = API_HOST + "/routing/v1/routers/hsl/index/graphql";

    // TODO: Digitransit returns internal server error when variables are used
    private static final String QUERY_DEPARTURES_TEMPLATE = "query {\n" +
        "  stop(id: \"%s\") {\n" +
        "    stoptimesWithoutPatterns(numberOfDepartures: %d) {\n" +
        "      realtime,\n" +
        "      serviceDay,\n" +
        "      scheduledDeparture,\n" +
        "      realtimeDeparture,\n" +
        "      trip {\n" +
        "        route {\n" +
        "          shortName,\n" +
        "          type\n" +
        "        }\n" +
        "        tripHeadsign\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}";
    public static final short WAIT_TIMEOUT = 30;

    /**
     * Download a list of all stops.
     *
     * @return a Future that resolves with a JSONObject that contains all known stops as data.stops.
     */
    public static RequestFuture<JSONObject> getAllStops(Context ctx) {
        String query = "query {stops { gtfsId, name, lat, lon, code, vehicleType, platformCode } }";
        JSONObject body = buildGraphQLQuery(query, null);

        Logger.i(TAG, "Fetching a list of all stops from Digitransit");
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        JsonObjectRequest req = new JsonObjectRequest(API_GRAPHQL, body, future, future);
        req.setTag(ctx);

        VolleyWrapper
            .getInstance(ctx)
            .getRequestQueue()
            .add(req);

        return future;
    }

    /**
     * Fetch the next departures from the Digitransit API for the given stop.
     *
     * @param ctx application context
     * @param stopId GTFS ID of the stop
     * @param numDepartures the number of departures to fetch
     * @param listener a DepartureResponseListener for the request
     */
    public static void getDepartures(
        final Context ctx,
        final String stopId,
        final int numDepartures,
        final DepartureResponseListener listener)
    {
        String query = String.format(QUERY_DEPARTURES_TEMPLATE, stopId, numDepartures);
        JSONObject body = buildGraphQLQuery(query, null);

        Logger.i(TAG, "Fetching %d departures for %s", numDepartures, stopId);
        Logger.d(TAG, query);

        JsonObjectRequest req = new JsonObjectRequest(API_GRAPHQL, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            Logger.d(TAG, "Got successful response");
            try {
                Vector<Departure> res = parseDepartureList(response);
                listener.onDeparturesAvailable(res);
            } catch (JSONException e) {
                listener.onDepartureLoadError(new VolleyError(e));
            }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Logger.e(TAG, "Got error response", error);
                Logger.d(TAG, error.toString());
                listener.onDepartureLoadError(error);
            }
        });

        req.setTag(ctx);

        VolleyWrapper
            .getInstance(ctx)
            .getRequestQueue()
            .add(req);
    }

    /**
     * Parses a Departure list response into a Vector of Departure objects.
     *
     * @param response the response to parse
     * @return a list of Departures
     * @throws JSONException if the response JSON has unexpected format or is missing fields
     */
    private static Vector<Departure> parseDepartureList(JSONObject response) throws JSONException {
        JSONObject data = response.getJSONObject("data");
        JSONObject stop = data.getJSONObject("stop");
        JSONArray raw = stop.getJSONArray("stoptimesWithoutPatterns");

        // Add a sane default capacity.
        Vector<Departure> res = new Vector<>(20);
        for (int i = 0; i < raw.length(); i++) {
            res.add(Departure.fromJsonObject(raw.getJSONObject(i)));
        }

        return res;
    }

    /**
     * Builds a JSONObject request body for the given GraphQL query.
     *
     * @param query the query
     * @param variables the variables
     * @return the request body
     */
    private static JSONObject buildGraphQLQuery(String query, @Nullable String variables) {
        HashMap<String, String> payload = new HashMap<>();
        payload.put("query", query);

        // TODO: Variables support once Digitransit works
        return new JSONObject(payload);
    }

    public interface DepartureResponseListener {
        /**
         * Called when a list of departures has been succesfully received from the server
         *
         * @param departures a List of departures received from the server
         */
        void onDeparturesAvailable(Vector<Departure> departures);

        /**
         * Called when the request for departures fail with an error. Could be an exception or
         * error code from the server.
         *
         * @param error an object containing the reason for failure
         */
        void onDepartureLoadError(VolleyError error);
    }
}
