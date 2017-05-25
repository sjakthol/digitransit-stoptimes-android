package io.github.sjakthol.stoptimes.digitransit;

import android.content.Context;
import android.support.annotation.Nullable;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import io.github.sjakthol.stoptimes.digitransit.models.Departure;
import io.github.sjakthol.stoptimes.utils.VolleyWrapper;
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
    public static String API_GRAPHQL = API_HOST + "/routing/v1/routers/hsl/index/graphql";
    private static final String QUERY_DEPARTURES_OF_STOP =
        "query ($stop: String!, $departures: Int) {\n" +
        "  stop(id: $stop) {\n" +
        "    stoptimesWithoutPatterns(numberOfDepartures: $departures) {\n" +
        "      realtime,\n" +
        "      pickupType,\n" +
        "      stop { platformCode },\n" +
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

    private static final String QUERY_DEPARTURES_OF_STATION =
        "query ($stop: String!, $departures: Int) {\n" +
        "  station(id: $stop) {\n" +
        "    stoptimesWithoutPatterns(numberOfDepartures: $departures) {\n" +
        "      realtime,\n" +
        "      pickupType,\n" +
        "      stop { platformCode },\n" +
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

    private static final String QUERY_STOPS =
        "query {" +
        "  stops {" +
        "    gtfsId, name, lat, lon, code, vehicleType, locationType, platformCode, parentStation { gtfsId }" +
        "  }" +
        "  stations {" +
        "    gtfsId, name, lat, lon, code, vehicleType, locationType, platformCode, parentStation { gtfsId }" +
        "  }" +
        "}";

    public static final short WAIT_TIMEOUT = 60;

    /**
     * Download a list of all stops.
     *
     * @return a Future that resolves with a JSONObject that contains all known stops as data.stops.
     */
    public static RequestFuture<JSONObject> getAllStops(Context ctx) throws JSONException {
        JSONObject body = buildGraphQLQuery(QUERY_STOPS, null);

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
     * @param ctx application context
     * @param stopId GTFS ID of the stop
     * @param locationType the type of the stop
     * @param numDepartures the number of departures to fetch
     * @param includeTerminus whether to include arrivals to terminus in the results
     * @param listener a DepartureResponseListener for the request
     */
    public static void getDepartures(
        final Context ctx,
        final String stopId,
        final String locationType,
        final int numDepartures,
        final boolean includeTerminus,
        final DepartureResponseListener listener) throws JSONException
    {
        HashMap<String, String> vars = new HashMap<>();
        vars.put("stop", stopId);
        vars.put("departures", String.valueOf(numDepartures));

        String query = locationType.equals("STATION") ?
            QUERY_DEPARTURES_OF_STATION : QUERY_DEPARTURES_OF_STOP;

        JSONObject body = buildGraphQLQuery(query, vars);

        Logger.i(TAG, "Fetching %d departures for %s", numDepartures, stopId);
        Logger.d(TAG, "%s", body);

        JsonObjectRequest req = new JsonObjectRequest(API_GRAPHQL, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            Logger.d(TAG, "Got successful response");
            try {
                Vector<Departure> res = parseDepartureList(response, includeTerminus);
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
     * @param includeTerminus whether to include arrivals to terminus in the results
     * @return a list of Departures
     * @throws JSONException if the response JSON has unexpected format or is missing fields
     */
    static Vector<Departure> parseDepartureList(JSONObject response, boolean includeTerminus) throws JSONException {
        JSONObject data = response.getJSONObject("data");
        JSONObject stop = data.optJSONObject("stop");
        JSONObject station = data.optJSONObject("station");
        if (stop == null && station == null) {
            throw new JSONException("No stop or station in data");
        }

        JSONObject stopOrStation = stop == null ? station : stop;
        JSONArray raw = stopOrStation.getJSONArray("stoptimesWithoutPatterns");

        Vector<Departure> res = new Vector<>(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            JSONObject obj = raw.getJSONObject(i);
            boolean isTerminus = obj.getString("pickupType").equals("NONE");

            if (includeTerminus || !isTerminus) {
                res.add(Departure.fromJsonObject(obj));
            }
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
    static JSONObject buildGraphQLQuery(String query, @Nullable HashMap<String, String> variables)
            throws JSONException
    {
        JSONObject req = new JSONObject();
        req.put("query", query);

        // Add variables only if given
        if (variables != null) {
            req.put("variables", new JSONObject(variables));
        }

        return req;
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
