package io.github.sjakthol.stoptimes.digitransit.models;

import org.junit.Test;

import java.sql.Timestamp;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class DepartureTest {
    @Test
    public void test_routeToVehicleType() throws Exception {
        assertThat(Departure.routeToVehicleType("BUS"), is(VehicleType.BUS));
        assertThat(Departure.routeToVehicleType("TRAM"), is(VehicleType.TRAM));
        assertThat(Departure.routeToVehicleType("RAIL"), is(VehicleType.COMMUTER_TRAIN));
        assertThat(Departure.routeToVehicleType("SUBWAY"), is(VehicleType.SUBWAY));
    }
}