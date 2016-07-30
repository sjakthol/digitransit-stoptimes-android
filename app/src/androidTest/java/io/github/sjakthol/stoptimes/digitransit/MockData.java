package io.github.sjakthol.stoptimes.digitransit;

public class MockData {
    public final static int NUM_MOCK_DEPARTURES = 6;
    public final static String MOCK_DEPARTURES =
        "{" +
        "  \"data\": {" +
        "    \"stop\": {" +
        "      \"stoptimesWithoutPatterns\": [{" +
        "        \"realtime\": false," +
        "        \"serviceDay\": 1469826000," +
        "        \"scheduledDeparture\": 34560," +
        "        \"realtimeDeparture\":34560," +
        "        \"trip\": {" +
        "          \"route\": {" +
        "            \"shortName\": \"550\"," +
        "            \"type\": \"BUS\"" +
        "          }," +
        "          \"tripHeadsign\": \"Westendinasema\"" +
        "        }" +
        "      }, {" +
        "        \"realtime\": false," +
        "        \"serviceDay\": 1469826000," +
        "        \"scheduledDeparture\": 34560," +
        "        \"realtimeDeparture\":34560," +
        "        \"trip\": {" +
        "          \"route\": {" +
        "            \"shortName\": null," +
        "            \"type\": \"SUBWAY\"" +
        "          }," +
        "          \"tripHeadsign\": \"Kamppi\"" +
        "        }" +
        "      }, {" +
        "        \"realtime\": false," +
        "        \"serviceDay\": 1469826000," +
        "        \"scheduledDeparture\": 34560," +
        "        \"realtimeDeparture\":34560," +
        "        \"trip\": {" +
        "          \"route\": {" +
        "            \"shortName\": \"A\"," +
        "            \"type\": \"RAIL\"" +
        "          }," +
        "          \"tripHeadsign\": \"Leppävaara\"" +
        "        }" +
        "      }, {" +
        "        \"realtime\": true," +
        "        \"serviceDay\": 1469826000," +
        "        \"scheduledDeparture\": 34560," +
        "        \"realtimeDeparture\":34500," +
        "        \"trip\": {" +
        "          \"route\": {" +
        "            \"shortName\": \"550\"," +
        "            \"type\": \"BUS\"" +
        "          }," +
        "          \"tripHeadsign\": \"Westendinasema\"" +
        "        }" +
        "      }, {" +
        "        \"realtime\": true," +
        "        \"serviceDay\": 1469826000," +
        "        \"scheduledDeparture\": 34560," +
        "        \"realtimeDeparture\": 34560," +
        "        \"trip\": {" +
        "          \"route\": {" +
        "            \"shortName\": null," +
        "            \"type\": \"SUBWAY\"" +
        "          }," +
        "          \"tripHeadsign\": \"Kamppi\"" +
        "        }" +
        "      }, {" +
        "        \"realtime\": true," +
        "        \"serviceDay\": 1469826000," +
        "        \"scheduledDeparture\": 34560," +
        "        \"realtimeDeparture\":34660," +
        "        \"trip\": {" +
        "          \"route\": {" +
        "            \"shortName\": \"A\"," +
        "            \"type\": \"RAIL\"" +
        "          }," +
        "          \"tripHeadsign\": \"Leppävaara\"" +
        "        }" +
        "      }]" +
        "    }" +
        "  }" +
        "}";

    public static final int NUM_MOCK_STOPS = 10;
    public static final String MOCK_STOPS = "{" +
        "  \"data\": {" +
        "    \"stops\": [" +
        "      {" +
        "        \"platformCode\": null," +
        "        \"vehicleType\": 3," +
        "        \"code\": \"Ki1519\"," +
        "        \"lon\": 24.358088," +
        "        \"lat\": 60.202282," +
        "        \"name\": \"Louhosmäki\"," +
        "        \"gtfsId\": \"HSL:6150219\"" +
        "      }," +
        "      {" +
        "        \"platformCode\": \"10\"," +
        "        \"vehicleType\": 3," +
        "        \"code\": \"E2226\"," +
        "        \"lon\": 24.829056699999974," +
        "        \"lat\": 60.183854099999984," +
        "        \"name\": \"Alvar Aallon puisto\"," +
        "        \"gtfsId\": \"HSL:2222235\"" +
        "      }," +
        "      {" +
        "        \"platformCode\": null," +
        "        \"vehicleType\": 1," +
        "        \"code\": \"0011\"," +
        "        \"lon\": 24.880192," +
        "        \"lat\": 60.160069," +
        "        \"name\": \"Lauttasaari\"," +
        "        \"gtfsId\": \"HSL:1310602\"" +
        "      }," +
        "      {" +
        "        \"platformCode\": null," +
        "        \"vehicleType\": 3," +
        "        \"code\": \"1522\"," +
        "        \"lon\": 24.861452100000005," +
        "        \"lat\": 60.22109100000007," +
        "        \"name\": \"Takkatie\"," +
        "        \"gtfsId\": \"HSL:1465111\"" +
        "      }," +
        "      {" +
        "        \"platformCode\": \"30\"," +
        "        \"vehicleType\": 3," +
        "        \"code\": \"4330\"," +
        "        \"lon\": 25.07743519999993," +
        "        \"lat\": 60.20973490000011," +
        "        \"name\": \"Itäkeskus(M)\"," +
        "        \"gtfsId\": \"HSL:1453130\"" +
        "      }," +
        "      {" +
        "        \"platformCode\": \"32\"," +
        "        \"vehicleType\": 3," +
        "        \"code\": \"4332\"," +
        "        \"lon\": 25.07793479999993," +
        "        \"lat\": 60.209874000000106," +
        "        \"name\": \"Itäkeskus(M)\"," +
        "        \"gtfsId\": \"HSL:1453132\"" +
        "      }," +
        "      {" +
        "        \"platformCode\": null," +
        "        \"vehicleType\": 3," +
        "        \"code\": \"4275\"," +
        "        \"lon\": 25.083304800000057," +
        "        \"lat\": 60.21352620000002," +
        "        \"name\": \"Korsholmantie\"," +
        "        \"gtfsId\": \"HSL:1453122\"" +
        "      }," +
        "      {" +
        "        \"platformCode\": null," +
        "        \"vehicleType\": 3," +
        "        \"code\": \"1518\"," +
        "        \"lon\": 24.870397699999966," +
        "        \"lat\": 60.21688569999964," +
        "        \"name\": \"Takomotie\"," +
        "        \"gtfsId\": \"HSL:1465104\"" +
        "      }," +
        "      {" +
        "        \"platformCode\": null," +
        "        \"vehicleType\": 0," +
        "        \"code\": \"0402\"," +
        "        \"lon\": 24.942100599999986," +
        "        \"lat\": 60.1688444000004," +
        "        \"name\": \"Ylioppilastalo\"," +
        "        \"gtfsId\": \"HSL:1020447\"" +
        "      }," +
        "      {" +
        "        \"platformCode\": \"4\"," +
        "        \"vehicleType\": 109," +
        "        \"code\": \"V0746\"," +
        "        \"lon\": 25.059989," +
        "        \"lat\": 60.323635," +
        "        \"name\": \"Koivukylä\"," +
        "        \"gtfsId\": \"HSL:4740551\"" +
        "      }" +
        "    ]" +
        "  }" +
        "}";
}
