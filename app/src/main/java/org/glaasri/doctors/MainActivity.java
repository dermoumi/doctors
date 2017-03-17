package org.glaasri.doctors;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.glaasri.doctors.dao.DatabaseHelper;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private Spinner spnSpecialty;
    private ProgressDialog mProgressDialog;

    private boolean mHasFineLocationPermission = false;
    private boolean mHasCoarseLocationPermission = false;
    private boolean mHasInternetPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup the spinner with the list of available specialty choices
        spnSpecialty = (Spinner) findViewById(R.id.spnSpecialty);
        ArrayAdapter<CharSequence> specialtyAdapter = ArrayAdapter.createFromResource(
            this, R.array.specialty_list, android.R.layout.simple_spinner_item
        );
        specialtyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSpecialty.setAdapter(specialtyAdapter);

        // Checking permissions
        checkFineLocationPermission();
        checkCoarseLocationPermission();
        checkInternetPermission();

        // Setup database?
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setTitle("Chargement...");
        mProgressDialog.setMessage("Préparation de la base de données...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        Thread dbThread = new Thread(new Runnable()
        {
            @Override
            public void run() {
                DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this);
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                db.close();

                try {Thread.sleep(3000);} catch(Exception e) {}
                mProgressDialog.dismiss();
            }
        });

        dbThread.start();
    }

    public void onSearch(View source) {
        if (mHasFineLocationPermission && mHasCoarseLocationPermission && mHasInternetPermission) {
            Intent i = new Intent(this, MapsActivity.class);
            i.putExtra("specialty", spnSpecialty.getSelectedItem().toString());
            startActivity(i);
        }
        else {
            Toast.makeText(this, "Les permissions requises pour le bon fonctionnement de " +
                "l'application ne sont pas accordées.", Toast.LENGTH_SHORT);
        }
    }

    public void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    public void requestCoarseLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
    }

    public void requestInternetPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET}, 3);
    }

    public void checkFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Check if we should show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                    .setTitle("Fine Location permission")
                    .setMessage("Cette application a besoin de connaître votre position actuelle exacte.")
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            requestFineLocationPermission();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
            }
            else {
                requestFineLocationPermission();
            }
        }
        else {
            Log.d(TAG, "FINE_LOCATION_PERMISSION ALREADY TRUE");
            mHasFineLocationPermission = true;
        }
    }

    public void checkCoarseLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Check if we should show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                    .setTitle("Coarse Location permission")
                    .setMessage("Cette application a besoin de connaître votre position actuelle approximative.")
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            requestCoarseLocationPermission();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
            }
            else {
                requestCoarseLocationPermission();
            }
        }
        else {
            Log.d(TAG, "COARSE_LOCATION_PERMISSION ALREADY TRUE");
            mHasCoarseLocationPermission = true;
        }
    }

    public void checkInternetPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Check if we should show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                    .setTitle("Internet permission")
                    .setMessage("Cette application a besoin de se connecter à Internet.")
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            requestInternetPermission();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
            }
            else {
                requestInternetPermission();
            }
        }
        else {
            Log.d(TAG, "INTERNET_PERMISSION ALREADY TRUE");
            mHasInternetPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
        case 1:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "FINE_LOCATION_PERMISSION TRUE");
                mHasFineLocationPermission = true;
            }

            return;
        case 2:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "COARSE_LOCATION_PERMISSION TRUE");
                mHasCoarseLocationPermission = true;
            }

            return;
        case 3:
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "INTERNET_PERMISSION TRUE");
                mHasInternetPermission = true;
            }

            return;
        }
    }
}
