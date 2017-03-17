package org.glaasri.doctors;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.glaasri.doctors.dao.DatabaseHelper;
import org.glaasri.doctors.dao.Doctor;
import org.glaasri.doctors.util.MapFiller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnConnectionFailedListener, LocationListener
{
    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap = null;
    private GoogleApiClient mApiClient;

    private String mSpecialty;

    private double mLatitude = 33.589914;
    private double mLongitude = -7.5916366;
    private boolean mMovedToInitialPosition = false;
    private Map<Marker, Doctor> mMarkers;
    private Marker mPositionMarker;

    private MapFiller mMapFiller;

    private Marker mCurrentMarker;

    private float dpToPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize the google places API client
        mApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSpecialty = getIntent().getStringExtra("specialty");
        mMarkers = new HashMap<Marker, Doctor>();

        Log.d(TAG, "Initializing location settings");
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mMapFiller != null) mMapFiller.release();
    }

    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();

        if (mPositionMarker != null) {
            mPositionMarker.setPosition(new LatLng(mLatitude, mLongitude));
        }

        if (!mMovedToInitialPosition && mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLatitude, mLongitude)));
            mMovedToInitialPosition = true;
        }
    }

    public void onProviderEnabled(String provider) {
        // Nothing to do?
    }

    public void onProviderDisabled(String provider) {
        // Nothing to do?
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Nothing to do?
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMinZoomPreference(10.f);
        mMap.setMaxZoomPreference(20.f);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude, mLongitude), 13.f));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Doctor doctor = mMarkers.get(marker);
                if (doctor != null) {
                    View viewDoctorInfo = findViewById(R.id.viewDoctorInfo);
                    View viewSubInfo = findViewById(R.id.viewSubInfo);
                    TextView doctorNameView = (TextView) findViewById(R.id.textDoctorName);
                    TextView doctorAddressView = (TextView) findViewById(R.id.textDoctorAddress);
                    TextView doctorTelephoneView = (TextView) findViewById(R.id.textDoctorTelephone);

                    doctorNameView.setText(doctor.getName());
                    doctorAddressView.setText(doctor.getAddress());
                    doctorTelephoneView.setText(doctor.getTelephone());

                    viewDoctorInfo.animate().translationY(0);
                    viewSubInfo.animate().translationY(0);
                }
                else {
                    hideDoctorInfo();
                }

                mCurrentMarker = marker;

                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng) {
                hideDoctorInfo();
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener()
        {
            @Override
            public void onCameraIdle() {
                mMapFiller.update(mMap.getProjection().getVisibleRegion(), mCurrentMarker);
            }
        });

        mMapFiller = new MapFiller(this, mMap, mMarkers, mSpecialty);
        mMapFiller.update(mMap.getProjection().getVisibleRegion(), mCurrentMarker);

        mPositionMarker = mMap.addMarker(new MarkerOptions()
            .position(new LatLng(mLatitude, mLongitude))
            .title("Votre position")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    public void hideDoctorInfo() {
        View viewDoctorInfo = findViewById(R.id.viewDoctorInfo);
        View viewSubInfo = findViewById(R.id.viewSubInfo);

        viewSubInfo.animate()
            .translationY(dpToPx(54));

        viewDoctorInfo.animate()
            .translationY(dpToPx(104))
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    TextView doctorNameView = (TextView) findViewById(R.id.textDoctorName);
                    TextView doctorAddressView = (TextView) findViewById(R.id.textDoctorAddress);
                    TextView doctorTelephoneView = (TextView) findViewById(R.id.textDoctorTelephone);

                    doctorNameView.setText("");
                    doctorAddressView.setText("");
                    doctorTelephoneView.setText("");
                }
            });

        mCurrentMarker = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        new AlertDialog.Builder(this)
            .setTitle("Impossible de se connecter")
            .setMessage("Impossible d'établir la connexion avec Internet.")
            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    public void onRelocate(View source) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(mLatitude, mLongitude)), 400, null);
       }
    }
}
