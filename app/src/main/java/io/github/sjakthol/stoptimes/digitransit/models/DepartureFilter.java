package io.github.sjakthol.stoptimes.digitransit.models;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.util.ObjectsCompat;
import io.github.sjakthol.stoptimes.db.StopListContract;

public class DepartureFilter {
    private String mRoute;
    private String mHeadsign;

    private DepartureFilter(String route, String headsign) {
        mRoute = route;
        mHeadsign = headsign;
    }

    @Override
    public String toString() {
        return super.toString() + "{" +
                "mRoute='" + mRoute + '\'' +
                ", mHeadsign='" + mHeadsign + '\'' +
                '}';
    }

    static public DepartureFilter fromDeparture(Departure dep) {
        return new DepartureFilter(dep.getRoute(), dep.getHeadsign());
    }

    /**
     * Construct a DepartureFilter from cursor that contains the following columns: route, headsign.
     *
     * @param cursor the cursor to use
     *
     * @return a new DepartureFilter object
     */
    @NonNull
    public static DepartureFilter fromCursor(@NonNull Cursor cursor) {
        return new DepartureFilter(
                cursor.getString(cursor.getColumnIndex(StopListContract.Stop.COLUMN_NAME_ROUTE)),
                cursor.getString(cursor.getColumnIndex(StopListContract.Stop.COLUMN_NAME_HEADSIGN))
        );
    }
    public String getRoute() {
        return mRoute;
    }

    public String getHeadsign() {
        return mHeadsign;
    }

    public boolean matchesDeparture(Departure d) {
        return d.getRoute().equals(this.getRoute()) && d.getHeadsign().equals(this.getHeadsign());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DepartureFilter)) return false;
        DepartureFilter that = (DepartureFilter) o;
        return mRoute.equals(that.mRoute) &&
                mHeadsign.equals(that.mHeadsign);
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(mRoute, mHeadsign);
    }
}
