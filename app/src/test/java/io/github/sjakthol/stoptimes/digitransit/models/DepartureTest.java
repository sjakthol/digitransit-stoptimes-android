package io.github.sjakthol.stoptimes.digitransit.models;

import org.junit.Test;

import java.sql.Timestamp;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class DepartureTest {
    @Test
    public void test_routeToVehicleType() throws Exception {
        assertThat(Departure.routeToVehicleType("0"), is(VehicleType.TRAM));
        assertThat(Departure.routeToVehicleType("1"), is(VehicleType.SUBWAY));
        assertThat(Departure.routeToVehicleType("109"), is(VehicleType.COMMUTER_TRAIN));
        assertThat(Departure.routeToVehicleType("701"), is(VehicleType.BUS));
        assertThat(Departure.routeToVehicleType("702"), is(VehicleType.BUS));
        assertThat(Departure.routeToVehicleType("RANDOM"), is(VehicleType.BUS));
        assertThat(Departure.routeToVehicleType("1234"), is(VehicleType.BUS));
    }
}