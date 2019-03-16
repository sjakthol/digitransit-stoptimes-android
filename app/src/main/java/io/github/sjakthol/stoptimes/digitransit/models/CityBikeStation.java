package io.github.sjakthol.stoptimes.digitransit.models;

import android.support.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

public class CityBikeStation extends Stop {

    /**
     * Create a new city bike station.
     *
     * @param id the station code
     * @param name the station name
     * @param lat the latitude of the stop position
     * @param lon the longitude of the stop position
     * @param is_favorite if this stop is favorite or not
     */
    private CityBikeStation(String id, String name, double lat, double lon, boolean is_favorite) {
        super(id, name, id, lat, lon, null, Stop.CITYBIKE_CODE, "CITYBIKE_STATION", null, is_favorite);
    }

    public static CityBikeStation fromJson(@NonNull JSONObject station) throws JSONException {
        return new CityBikeStation(
            station.getString("stationId"),
            station.getString("name"),
            station.getDouble("lat"),
            station.getDouble("lon"),
            false
        );
    }
}
