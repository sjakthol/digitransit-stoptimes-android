package io.github.sjakthol.stoptimes.digitransit.models;

import android.location.Location;
import io.github.sjakthol.stoptimes.R;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class StopTest {
    @Test
    public void test_codeToType() throws Exception {
        assertThat(Stop.codeToType(0), is(VehicleType.TRAM));
        assertThat(Stop.codeToType(1), is(VehicleType.SUBWAY));
        assertThat(Stop.codeToType(3), is(VehicleType.BUS));
        assertThat(Stop.codeToType(109), is(VehicleType.COMMUTER_TRAIN));

        assertThat("Unknown stop code turns to bus", Stop.codeToType(2), is(VehicleType.BUS));
    }

    @Test
    public void test_getters() throws Exception {
        Stop stop = new Stop("HSL:1234", "Railway Station", "0001", 78, 88, "21", 3, true);
        assertThat(stop.getId(), is("HSL:1234"));
        assertThat(stop.getName(), is("Railway Station"));
        assertThat(stop.getCode(), is("0001"));
        assertThat(stop.isFavorite(), is(true));
        assertThat(stop.getVehicleType(), is(VehicleType.BUS));
        assertThat(stop.getPlatform(), is("21"));
        assertThat(stop.getCity(), is(R.string.stop_location_helsinki));
    }

}