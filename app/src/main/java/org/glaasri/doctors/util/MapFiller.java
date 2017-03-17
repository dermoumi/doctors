package org.glaasri.doctors.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.glaasri.doctors.dao.DatabaseHelper;
import org.glaasri.doctors.dao.Doctor;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by sid on 01/04/2017.
 */

public class MapFiller
{
    private SQLiteDatabase mDatabase;

    private GoogleMap mMap;
    private Map<Marker, Doctor> mMarkers;
    private String mSpecialty;

    public MapFiller(
        Context context, GoogleMap map, Map<Marker, Doctor> markers, String currentSpecialty
    ) {
        DatabaseHelper helper = new DatabaseHelper(context);
        mDatabase = helper.getReadableDatabase();

        mMap = map;
        mMarkers = markers;
        mSpecialty = currentSpecialty;
    }

    public void clearMap() {
        Iterator<Marker> it = mMarkers.keySet().iterator();
        while (it.hasNext()) {
            Marker marker = it.next();
            marker.remove();
            it.remove();
        }
    }

    public void release() {
        clearMap();
        mDatabase.close();
    }

    public void update(VisibleRegion region, Marker currentMarker) {
        clearMap();

        Double n = region.latLngBounds.northeast.longitude;
        Double e = region.latLngBounds.northeast.latitude;
        Double s = region.latLngBounds.southwest.longitude;
        Double w = region.latLngBounds.southwest.latitude;

        Cursor cursor = mDatabase.query(
            "doctor", null, "specialty = ? and latitude between ? and ? and longitude between ? and ?",
            new String[] {mSpecialty, w.toString(), e.toString(), s.toString(), n.toString()}, null, null, null
        );

        while (cursor.moveToNext()) {
            Doctor doctor = new Doctor(
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(5),
                cursor.getDouble(6),
                cursor.getDouble(7)
            );

            Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(doctor.getLatitude(), doctor.getLongitude()))
                .title(doctor.getName()));

            if (currentMarker != null && currentMarker.getPosition().equals(marker.getPosition())) {
                marker.showInfoWindow();
            }

            mMarkers.put(marker, doctor);
        }
    }
}
