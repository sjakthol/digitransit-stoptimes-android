package io.github.sjakthol.stoptimes.digitransit.models;

import org.json.JSONException;
import org.json.JSONObject;

public class CitybikeStatus {
    static final private String STATUS_BIKESTATION_ON = "Station on";

    private final int mBikesAvailable;
    private final int mSpaces;
    private final String mState;

    private CitybikeStatus(int spaces, int available, String state) {
        mSpaces = spaces;
        mBikesAvailable = available;
        mState = state;
    }

    /**
     * Construct a new status from JSON object.
     *
     * @param obj json object
     * @return status
     * @throws JSONException if json is not valid
     */
    public static CitybikeStatus fromJsonObject(JSONObject obj) throws JSONException {
        int spaces = obj.getInt("spacesAvailable");
        int available = obj.getInt("bikesAvailable");
        String state = obj.getString("state");

        return new CitybikeStatus(spaces, available, state);
    }

    /**
     * Get the number of spaces this station has.
     *
     * @return number of spaces
     */
    public int getSpaces() {
        return Math.max(mSpaces, mBikesAvailable);
    }

    /**
     * Get the number of bikes this station has.
     *
     * @return number of bikes
     */
    public int getBikesAvailable() {
        return mBikesAvailable;
    }

    /**
     * Check if this station is available.
     *
     * @return true if station is in use, false if not
     */
    public boolean getStationAvailable() {
        return mState.equals(STATUS_BIKESTATION_ON);
    }
}

