package io.github.sjakthol.stoptimes.activity.stoplist;

import io.github.sjakthol.stoptimes.utils.Helpers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class StopListAdapterTest {
    @Test
    public void test_formatDistance() throws Exception {
        assertThat(Helpers.formatDistance(12.323), is("12m"));
        assertThat(Helpers.formatDistance(54.752), is("55m"));
        assertThat(Helpers.formatDistance(6), is("6m"));

        assertThat(Helpers.formatDistance(1000), is("1,0km"));
        assertThat(Helpers.formatDistance(1542), is("1,5km"));
        assertThat(Helpers.formatDistance(1550), is("1,6km"));

        assertThat(Helpers.formatDistance(1551234), is("1551km"));
        assertThat(Helpers.formatDistance(1551554), is("1552km"));
    }

}