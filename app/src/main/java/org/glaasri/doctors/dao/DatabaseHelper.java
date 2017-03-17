package org.glaasri.doctors.dao;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.glaasri.doctors.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

/**
 * Created by sid on 1/1/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "doctors.db";

    Context mContext = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL("CREATE TABLE doctor (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name NVARCHAR(255)," +
            "address NVARCHAR(255)," +
            "telephone NVARCHAR(20)," +
            "city NVARCHAR(50)," +
            "specialty NVARCHAR(100)," +
            "latitude REAL," +
            "longitude REAL" +
        ");");

        // Read the json file into a string
        InputStream is = mContext.getResources().openRawResource(R.raw.db);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];

        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String jsonString = writer.toString();

        // Pre-declared out-of-loop variables to reduce garbage-collection stress
        String city, specialty, doctorName, doctorAddress, doctorTelephone;
        Iterator<String> cityKeys, specialtyKeys;
        JSONObject cities, specialties, doctor;
        double doctorLatitude, doctorLongitude;
        // Get cities
        try {
            cities = new JSONObject(jsonString);
            cityKeys = cities.keys();
            while (cityKeys.hasNext()) {
                city = cityKeys.next();

                // Get specialties
                try {
                    specialties = cities.getJSONObject(city);
                    specialtyKeys = specialties.keys();
                    while (specialtyKeys.hasNext()) {
                        specialty = specialtyKeys.next();

                        // Get doctors
                        try {
                            JSONArray doctorList = specialties.getJSONArray(specialty);
                            for (int i = 0; i < doctorList.length(); ++i) {
                                doctor = doctorList.getJSONObject(i);

                                try {
                                    doctorName = doctor.getString("name");
                                    doctorAddress = doctor.getString("address");
                                    doctorTelephone = doctor.getString("tel");
                                    doctorLatitude = doctor.getDouble("x");
                                    doctorLongitude = doctor.getDouble("y");

                                    // INSERT INTO DATABASE
                                    ContentValues values = new ContentValues();
                                    values.put("name", doctorName);
                                    values.put("address", doctorAddress);
                                    values.put("telephone", doctorTelephone);
                                    values.put("latitude", doctorLatitude);
                                    values.put("longitude", doctorLongitude);
                                    values.put("specialty", specialty);
                                    values.put("city", city);

                                    db.insert("doctor", null, values);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE doctor;");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
