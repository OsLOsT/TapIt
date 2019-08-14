package com.oslost.tapit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap googleMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private SharedPreferences showLocation;
    private Context context;
    private double myLat;
    private double myLng;
    private Bundle getMyLatLng;
    private String bLng;
    private String bLat;

    /* On Search Variables */
    private String location;

    /* Geofence Variables */
    private GeofencingClient geofencingClient;
    private Marker geoFenceMarker;
    private Marker searchMarker;
    private static final long GEO_DURATION = NEVER_EXPIRE;
    private static final String GEOFENCE_REQ_ID = "Your Destination";
    private static final float GEOFENCE_RADIUS = 20.0f; // in meters
    private static final int ME_WANT_NO_INITIAL_TRIGGER = 0;
    private boolean checkPermission = false;
    //private String geofenceallowed = "n";

    /* GeoFence Pending Intent Variables */
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    /* Notification Variables */
    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

    /* Get Current Location variable */
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5445;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentLocationMarker;
    private Location currentLocation;
    private boolean firstTimeFlag = true;

    /* Rotate map wrt to phone movement variable */
    private float[] mRotationMatrix = new float[16];
    float mDeclination;
    private SensorManager mSensorManager;
    private Sensor mRotVectSensor;
    private String rotatoE = "n";

    /* JSON variables */
    private String json;
    private Marker mClosestMarker;
    private float mindist;
    private double gpsLat;
    private double gpsLon;

        /* #################################################################
        SharedPrefs showLocation contains the following key-value pair:

        - MyLocationName : <Location Name>
        - MyLatitude : <Location Lat>
        - MyLongitude : <Location Lng>
        - FirstTime : <Boolean FirsTime>
        - CurrentLatitude : < Location Lat>
        - CurrentLongitude : < Location Lng>

       ################################################################# */

        /* #############################################################
            set Geofence inital trigger : Geofence.GEOFENCE_TRANSITION_ENTER
            when you want to show the teacher a notification will appear

            or else just use default which is
            default: ME_WANT_NO_INITIAL_TRIGGER
          ############################################################# */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        /* Initialise geofencing client with this activity */
        geofencingClient = LocationServices.getGeofencingClient(this);

        /* Obtain the SupportMapFragment and get notified when the map is ready to be used. */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /* Compass Enable click Listener*/
        findViewById(R.id.compassEnableButton).setOnClickListener(compass);

        /* Set Listener for the current location button */
        findViewById(R.id.currentLocationImageButton).setOnClickListener(myCurrentLocation);

        /* Set Listner for the closest water cooler */
        findViewById(R.id.closest_waterCooler).setOnClickListener(closestWaterCooler);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mRotVectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        /* Bottom Navigation View with Activity */
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        Menu menu = bottomNav.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;


        /* Check if it is first time user */
        setUpAddress();

        /* Setting up Updated Geofence */
        setMyUpdatedGeoFence();

        /* Calling Geofence */
        callGeofence();

        /* Ground Overlay of SP */
        groundOverLaySP();

        /* Style your map */
        styleMyMap();


        /* Parse JSON file as String */
        parsingJson();

        /* On marker Click */
        spawnAlertDialogforWaterCooler();


    }

    /* #################################
             PARSING JSON FUNCTIONS
       #################################*/
    private void parsingJson() {
        try {
            InputStream inputStream = getAssets().open("water_coolers.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            createMarkersFromJson(jsonArray);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "Createmarkers with json has error");
        }

    }

    /* #################################################
           CREATING MARKER FOR WATER COOLER FUNCTIONS
       #################################################*/

    void createMarkersFromJson(JSONArray jsonArray) throws JSONException {

        // De-serialize the JSON string into an array of city objects
        for (int i = 0; i < jsonArray.length(); i++) {

            // Create a marker for each city in the JSON data.
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            /* Get the latLong of each water cooler */
            double lat = jsonObject.getJSONArray("latlng").getDouble(0);
            double lon = jsonObject.getJSONArray("latlng").getDouble(1);

            LatLng position = new LatLng(lat, lon);
            Log.i(TAG, "json lat:" + lat);
            Log.i(TAG, "json lon:" + lon);
            Marker pin = this.googleMap.addMarker(new MarkerOptions().title(jsonObject.getString("name")).snippet(jsonObject.getString("Location")).position(position));
            /* TODO: Potentially Dangerous */
            pin.setTag(jsonObject.getInt("Tag"));

        }


    }



    /* ###################################
        FIRST TIME APP USER FUNCTION
       ###################################*/

    private void setUpAddress() {
        /* Run this function only and IF ONLY the user really launch the app for the first time */
        if (loadPrefs("FirstTime", true)) {
            savePrefs("FirstTime", false);

            /* If is first time using app */
            //POP OUT THE DIALOG WINDOW
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            final View view = getLayoutInflater().inflate(R.layout.first_time_add_address, null);
            final EditText addressText = view.findViewById(R.id.new_address_text);
            Button addressButton = view.findViewById(R.id.new_address_button);

            builder.setView(view);
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);

            addressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    location = addressText.getText().toString();
                    if (location != null) {
                        Log.i(TAG, "location not null");
                        if (!location.matches("")) {
                            Log.i(TAG, "location not empty space");
                            onSearchLocation(location);

                            Bundle putLatLng = new Bundle();
                            putLatLng.putString("myLat", loadPrefs("MyLatitude", null));
                            putLatLng.putString("myLng", loadPrefs("MyLongitude", null));

                            Log.i(TAG, "myLat: " + loadPrefs("MyLatitude", null));
                            Log.i(TAG, "myLng:" + loadPrefs("MyLongitude", null));

                            Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);
                            mapIntent.putExtras(putLatLng);

                            mapIntent.putExtra("myLat", loadPrefs("MyLatitude", null));
                            mapIntent.putExtra("myLng", loadPrefs("MyLongitude", null));
                            /* TODO: DELETE THIS AFTER BETA TESTING */
                            /* THIS PART OF THE CODE WORKS */
                            Log.i(TAG, "Put Extra for intent done");
                            startActivity(mapIntent);
                            alertDialog.dismiss();
                        }
                    } else
                        Log.i(TAG, "location in setting up new address is null");

                }
            });
        } else {

            Log.i(TAG, "Welcome");
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    /* Check if the user has google play services installed into their mobile phone */
    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            mSensorManager.registerListener(this, mRotVectSensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
            startCurrentLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        /* Stop the listener to save battery */
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient = null;
        googleMap = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {

            SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);
            double Bearing = Math.toDegrees(orientation[0]) + mDeclination;
            float bearing = (float) Bearing;
            if (rotatoE == "y") {
                if (googleMap != null) {
                    updateCamera(bearing);
                }
            }
        }
    }



    /* ###########################
            GEOCODE FUNCTION
       ###########################*/

    public void onSearchLocation(String location) {

        if (location != null) {
            savePrefs("MyLocationName", location.toUpperCase());

            List<Address> addressList = null;
            if (location != null || !location.equals("")) {
                Geocoder geocoder = new Geocoder(this);
                try {

                    addressList = geocoder.getFromLocationName(location, 1);
                    if (addressList != null) {
                        addressList = geocoder.getFromLocationName(location, 1);
                        Log.i(TAG, "onSearchLocation() is null 1");

                    } else {
                        addressList = null;
                        Toast.makeText(this, "Location does not exist,please enter something else.", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onSearchLocation() is null 2");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Location does not exist,please enter something else.", Toast.LENGTH_SHORT).show();


                }
                if (addressList.size() > 0) {

                    Address address = addressList.get(0);
                    String show = "Latitude: " + address.getLatitude() + "\nLongitude: " + address.getLongitude();
                    Toast.makeText(getApplicationContext(), address.getLatitude() + " " + address.getLongitude(), Toast.LENGTH_LONG).show();

                    /* Store the updated address into the static variable */
                    showLocation.edit().putString("MyLatitude", String.valueOf(address.getLatitude())).apply();
                    showLocation.edit().putString("MyLongitude", String.valueOf(address.getLongitude())).apply();
                    Log.i(TAG, "onSearchLocation() is null 3");

                } else {
                    Toast.makeText(this, "Location does not exist, please enter something else.", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onSearchLocation() is null 4");
                }
            }
        }
    }



    /* ###################################
        MY PERSONAL TESTING FUNCTION
       ###################################*/

    private void testingMyStuff() {

    }

    /* ###################################
       GET UPDATED ADDRESS FUNCTION
   ###################################*/
    private void callGeofence() {
        if (!loadPrefs("MyLatitude", null).equals(null)) {
            if (!loadPrefs("MyLongitude", null).equals(null)) {

                double loadLat = Double.parseDouble(loadPrefs("MyLatitude", null));
                double loadLng = Double.parseDouble(loadPrefs("MyLongitude", null));

                /* Implementation of the whole GeoFence Lies here */
                LatLng home = new LatLng(loadLat, loadLng);

                if (geoFenceMarker != null) {
                    Log.i(TAG, "Geofence marker not null");
                    clearGeofence();
                }

                if (geoFenceMarker == null) {
                    Log.i(TAG, "Geofence marker is null");
                    markerForGeofence(home);
                    startGeofence();
                    Log.i(TAG, "bL Geofence created");
                }

            }
        }
    }

    private void setMyUpdatedGeoFence() {

        /* TODO: THIS is in working condition */
        Intent intent = getIntent();
        if (intent.hasExtra("myLat")) {
            /* If there is no latitude to get in the first place then there is no point
                in getting the longitude which in turns means is useless to even create a geofence */

            getMyLatLng = getIntent().getExtras();
            if (!getMyLatLng.getString("myLat").equals(null)) {
                bLat = getMyLatLng.getString("myLat");
                myLat = Double.parseDouble(bLat);
                Log.i(TAG, "bLat:" + myLat);
            }


            if (intent.hasExtra("myLng")) {
                getMyLatLng = getIntent().getExtras();
                if (!getMyLatLng.getString("myLng").equals(null)) {
                    bLng = getMyLatLng.getString("myLng");
                    myLng = Double.parseDouble(bLng);
                    Log.i(TAG, "bLng:" + myLng);
                }
            }

            /* Implementation of the whole GeoFence Lies here */
            LatLng home = new LatLng(myLat, myLng);

            if (geoFenceMarker != null) {
                Log.i(TAG, "Geofence marker not null");
                clearGeofence();
            }

            if (geoFenceMarker == null) {
                Log.i(TAG, "Geofence marker is null");
                markerForGeofence(home);
                startGeofence();
                Log.i(TAG, "bL Geofence created");
            }


        }


    }



    /* #############################
         GEOFENCE MARKER FUNCTIONS
       #############################*/

    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        String title = "HOME";
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if (this.googleMap != null) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();

            geoFenceMarker = this.googleMap.addMarker(markerOptions);

            Log.i(TAG, "markerForGeofence(" + latLng + ") HAS FINISH DRAWING");

        }
    }

    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;

    private void drawGeofence() {
        Log.i(TAG, "drawGeofence()");

        if (geoFenceLimits != null)
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(GEOFENCE_RADIUS);
        geoFenceLimits = this.googleMap.addCircle(circleOptions);
    }

    private void removeGeofenceDraw() {
        Log.i(TAG, "removeGeofenceDraw()");
        if (geoFenceMarker != null)
            geoFenceMarker.remove();
        if (geoFenceLimits != null)
            geoFenceLimits.remove();
        Log.i(TAG, "removeGeofenceDraw() FINISHED");
    }

     /* #############################
               GEOFENCE FUNCTIONS
        #############################*/

    /* Start Geofence creation process */
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if (geoFenceMarker != null) {
            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
            //Toast.makeText(this, "Your Home is set", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "startGeoFence Your Home is set");
        } else {
            Toast.makeText(this, "Your Home is null", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Geofence marker is null");
        }
    }


    /* #############################
         GEOFENCE SUPPORT FUNCTIONS
       #############################*/

    /* Create a Geofence */
    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.i(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    /* Create a Geofence Request
       Specify the geofences to monitor and to set how related Geofence events are triggered
       INITIAL_TRIGGER_ENTER tells Location services that GEOFENCE_TRANSITION_ENTER should be triggered if the device is already inside the geofence.
       Conclusion: I dont want any INITAIL_TRIGGER

       -Referece:
            https://stackoverflow.com/questions/44179175/stop-initial-triggering-if-device-is-already-inside-geofence-android
     */
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.i(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(ME_WANT_NO_INITIAL_TRIGGER)
                .addGeofence(geofence)
                .build();
    }

    /* #########################################
         ADDING AND REMOVING GEOFENCE FUNCTIONS
       #########################################*/

    /* Add the created GeofenceRequest to the device's monitoring list */
    @SuppressLint("MissingPermission")

    private void addGeofence(GeofencingRequest request) {
        Log.i(TAG, "addGeofence");

        if (checkPermission()) {
            /* Permission checking is done by its own function at the bottom */
            geofencingClient.addGeofences(request, createGeofencePendingIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            /* TODO: DELETE THIS LATER */
                            Toast.makeText(context, "GeoFence successfully created", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "GeoFence successfully created");
                            drawGeofence();
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            /* TODO: DELETE THIS LATER */
                            Toast.makeText(context, "GeoFence failed to be created", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "GeoFence failed to be created");
                        }
                    });
        } else
            Log.i(TAG, "Permission for addGeofence is false");
    }

    /* Remove Geofence and stop monitoring */
    private void clearGeofence() {
        Log.i(TAG, "clearGeofence()");
        geofencingClient.removeGeofences(createGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        /* TODO: DELETE THIS LATER AFTER BETA TESTING */
                        Log.i(TAG, "Geofence Removed");
                        //Toast.makeText(context, "Geofence Removed", Toast.LENGTH_SHORT).show();
                        removeGeofenceDraw();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        /* TODO: DELETE THIS LATER AFTER BETA TESTING */
                        Log.i(TAG, "Geofence NOT Removed");
                        Toast.makeText(context, "Geofence NOT Removed", Toast.LENGTH_SHORT).show();
                    }
                });
    }




    /* ####################################
         GEOFENCE PENDING INTENT FUNCTIONS
       #####################################*/

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

        /* Basically the Transition Service is acutally a broadcast receiver */
        Intent intent = new Intent(this, GeofenceTransitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    /* ################################
         NOTIFICATION INTENT FUNCTIONS
       ################################*/
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }


    /* #################################################
        SHARED PREFERENCES SAVING AND LOADING FUNCTIONS
       ################################################# */

    /* FOR BOOLEAN */

    public void savePrefs(String key, boolean value) {
        context = getApplicationContext();
        showLocation = context.getSharedPreferences("db", MODE_PRIVATE);
        //showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = showLocation.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private boolean loadPrefs(String key, boolean value) {
        context = getApplicationContext();
        showLocation = context.getSharedPreferences("db", MODE_PRIVATE);
        //showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean data = showLocation.getBoolean(key, value);

        return data;
    }

    /* FOR FLOAT */
    public void savePrefs(String key, float value) {
        context = getApplicationContext();
        showLocation = context.getSharedPreferences("db", MODE_PRIVATE);
        //showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = showLocation.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    private float loadPrefs(String key, float value) {
        context = getApplicationContext();
        showLocation = context.getSharedPreferences("db", MODE_PRIVATE);
        //showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        float data = showLocation.getFloat(key, value);
        return data;
    }

    /* FOR STRING */

    public void savePrefs(String key, String value) {
        context = getApplicationContext();
        showLocation = context.getSharedPreferences("db", MODE_PRIVATE);
        //showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = showLocation.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String loadPrefs(String key, String value) {
        context = getApplicationContext();
        showLocation = context.getSharedPreferences("db", MODE_PRIVATE);
        //showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String data = showLocation.getString(key, value);
        return data;
    }

    /* ###############################
           STYLE MAP FUNCTIONS
       ###############################*/
    private void styleMyMap() {
        try {
            // Time Check for night
            if ((getCurrentTime() < 24 && getCurrentTime() > 17) || (getCurrentTime() >= 0 && getCurrentTime() < 7)) {
                boolean success = this.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_night));

                if (!success) {
                    Log.e(TAG, "Style parsing failed.");
                }
            } else {
                // Time for morning
                boolean success = this.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_morning));

                if (!success) {
                    Log.e(TAG, "Style parsing failed.");
                }

            }

        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    /* ###############################
           GET 24HR TIME FUNCTIONS
       ###############################*/

    public int getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int timeHour = calendar.get(Calendar.HOUR_OF_DAY);
        return timeHour;

    }


     /* #################################################
                CALLBACK FUNCTIONS
       #################################################*/

    /* LocationCallback class object which we’re passing in when requesting location updates.
     * FirstTime flag is user. If it is true and googleMap instance is not null. It is because when the app opends, we need to animate googleMaps to user
     * current locaiton.*/

    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() == null)
                return;
            currentLocation = locationResult.getLastLocation();
            savePrefs("CurrentLatitude", Double.toString(currentLocation.getLatitude()));
            savePrefs("CurrentLongitude", Double.toString(currentLocation.getLongitude()));
            /* TODO: TO BE DELETED AFTER BETA TESTING */
            Log.i(TAG, "Current Lat:" + Double.toString(currentLocation.getLatitude()));
            Log.i(TAG, "Current Lng:" + Double.toString(currentLocation.getLongitude()));
            if (firstTimeFlag && googleMap != null) {
                rotatoE = "n";

                animateCamera(currentLocation);
                firstTimeFlag = false;
            }
            showMarker(currentLocation);
        }
    };

    /* #################################################
           FINDING CLOSEST WATER COOLER FUNCTIONS
       #################################################*/

    private void findClosestWaterCooler() {
        try {
            InputStream inputStream = getAssets().open("water_coolers.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {

                // Create a marker for each city in the JSON data.
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                /* Get the latLong of each water cooler */
                double lat = jsonObject.getJSONArray("latlng").getDouble(0);
                double lon = jsonObject.getJSONArray("latlng").getDouble(1);

                LatLng position = new LatLng(lat, lon);
                Log.i(TAG, "json lat:" + lat);
                Log.i(TAG, "json lon:" + lon);
                Marker currentMarker = this.googleMap.addMarker(new MarkerOptions().title(jsonObject.getString("name")).snippet(jsonObject.getString("Location")).position(position));
                /* TODO: Potentially Dangerous */
                currentMarker.setTag(jsonObject.getInt("Tag"));
                currentMarker.setVisible(false);

                /* TODO: Delete LOG after beta testing */
                if (!loadPrefs("CurrentLatitude", null).equals(null)) {
                    Log.i(TAG, "CurrentLatitude for json is not null");

                    if (!loadPrefs("CurrentLongitude", null).equals(null)) {
                        Log.i(TAG, "CurrentLongitude for json is not null");

                        gpsLat = Double.parseDouble(loadPrefs("CurrentLatitude", null));
                        gpsLon = Double.parseDouble(loadPrefs("CurrentLongitude", null));

                        Log.i(TAG, "CurrentLatitude for json is :" + gpsLat);
                        Log.i(TAG, "CurrentLongitude for json is :" + gpsLon);

                    }
                }

                float[] distance = new float[1];
                Log.i(TAG, "Going to run cal on location distance between");
                Location.distanceBetween(gpsLat, gpsLat, lat, lon, distance);
                if (i == 0) {
                    mindist = distance[0];
                    mClosestMarker = currentMarker;
                    Log.i(TAG, "Going running cal on existencing");

                } else if (mindist > distance[0]) {
                    mindist = distance[0];
                    mClosestMarker = currentMarker;
                    Log.i(TAG, "Going running cal on location distance between");

                }
            }
            /* SET THE DIALOG HERE */
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            final View view = getLayoutInflater().inflate(R.layout.display_watercooler, null);
            final TextView waterName = view.findViewById(R.id.watercooler_name);
            final TextView waterLoc = view.findViewById(R.id.watercooler_location);
            final ImageView waterPic = view.findViewById(R.id.watercooler_image);

            int position = (int) mClosestMarker.getTag();

            switch(position){
                case 1: waterName.setText(mClosestMarker.getTitle());
                    waterLoc.setText(mClosestMarker.getSnippet());
                    waterPic.setImageResource(R.drawable.t12a_boys_toilet);
                    break;

                case 2: waterName.setText(mClosestMarker.getTitle());
                    waterLoc.setText(mClosestMarker.getSnippet());
                    waterPic.setImageResource(R.drawable.t12a_girls_toilet);
                    break;

                case 3: waterName.setText(mClosestMarker.getTitle());
                    waterLoc.setText(mClosestMarker.getSnippet());
                    waterPic.setImageResource(R.drawable.below_spectrum);
                    break;
            }

            builder.setView(view);
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
            Toast.makeText(this, "Closest Water Cooler Distance: " + mClosestMarker.getTitle(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "Createmarkers with json has error");
        }
    }




     /* #################################################
                ON CLICK LISTENER FUNCTIONS
       #################################################*/

    private void spawnAlertDialogforWaterCooler(){
        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i(TAG, "I am being cliked");
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                final View view = getLayoutInflater().inflate(R.layout.display_watercooler, null);
                final TextView waterName = view.findViewById(R.id.watercooler_name);
                final TextView waterLoc = view.findViewById(R.id.watercooler_location);
                final ImageView waterPic = view.findViewById(R.id.watercooler_image);

                if(marker.getTag() == null)
                    return true;

                int position = (int) marker.getTag();


                switch(position){
                    case 1: waterName.setText(marker.getTitle());
                            waterLoc.setText(marker.getSnippet());
                            waterPic.setImageResource(R.drawable.t12a_boys_toilet);
                            break;

                    case 2: waterName.setText(marker.getTitle());
                            waterLoc.setText(marker.getSnippet());
                            waterPic.setImageResource(R.drawable.t12a_girls_toilet);
                            break;

                    case 3: waterName.setText(marker.getTitle());
                            waterLoc.setText(marker.getSnippet());
                            waterPic.setImageResource(R.drawable.below_spectrum);
                            break;

                }




                builder.setView(view);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return false;
            }
        });



    }

    private View.OnClickListener closestWaterCooler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            findClosestWaterCooler();
        }
    };

    private final View.OnClickListener myCurrentLocation = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            rotatoE = "n";
            if (view.getId() == R.id.currentLocationImageButton && googleMap != null && currentLocation != null) {
                MapsActivity.this.animateCamera(currentLocation);
            }
        }
    };

    private View.OnClickListener compass = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (rotatoE == "y") {
                rotatoE = "n";
                no();
            } else if (rotatoE == "n") {
                rotatoE = "y";
                yes();
            }
        }
    };

      /* ############################################
            BOTTOM NAVIGATION VIEW FUNCTION
       ############################################*/

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //Activity selectedActivity = null;

                    switch (item.getItemId()) {
                        case R.id.nav_map:
                            break;
                        case R.id.nav_ai:
                            Intent iai = new Intent(getApplicationContext(), AiActivity.class);
                            startActivity(iai);
                            break;
                        case R.id.nav_settings:
                            Intent isettings = new Intent(getApplicationContext(), SettingsActivity.class);
                            startActivity(isettings);
                            break;
                    }

                    return true;
                }
            };

    /* ################################################
         GROUND OVER LAY SP MAP FUNCTION [NOT WORKING]
     ##################################################*/
    private void groundOverLaySP() {

        BitmapDescriptor spBitmap = BitmapDescriptorFactory.fromResource(R.raw.resizedmap);

        LatLng southWest = new LatLng(01.3072833, 103.7722194/*1.1826, 103.4619*/);
        LatLng northEast = new LatLng(01.3119111, 103.7827639/*1.1842, 103.4657*/);


        LatLngBounds latLngBounds = new LatLngBounds(southWest, northEast);

        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
        groundOverlayOptions.positionFromBounds(latLngBounds);
        groundOverlayOptions.bearing(-21.0694f);
        groundOverlayOptions.image(spBitmap);
        groundOverlayOptions.transparency(0.2f);
        groundOverlayOptions.visible(true);

        this.googleMap.addGroundOverlay(groundOverlayOptions);


    }

    /* ######################################
                CHECK PERMISSION FUNCTION
       ######################################*/
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    /* Checking function is used on onResume */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status)
            return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(this, "Please Install google play services to use this application", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission denied by uses", Toast.LENGTH_SHORT).show();

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCurrentLocationUpdates();
            }

        }
    }

    /* ######################################
                SUPPORT FUNCTION
       ######################################*/

    /* Convert vector based xml to bitmap so that it can be shown on google map */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /* ######################################
           ORIENT MAP WITH PHONE FUNCTION
       ######################################*/

    /* Toast function for rotato enable/disable */
    void yes() {
        Toast.makeText(this, "Enabled automatic map reorientation.", Toast.LENGTH_SHORT).show();
    }

    void no() {
        Toast.makeText(this, "Disabled auto map reorientation.", Toast.LENGTH_SHORT).show();
    }

    /* For rotatoE */
    private void updateCamera(float bearing) {
        CameraPosition oldPos = googleMap.getCameraPosition();

        CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos), 200, null);


    }

    /* ######################################
               CURRENT LOCATION FUNCTION
       ######################################*/

    @NonNull
    private CameraPosition getCameraPositionWithBearing(LatLng latLng) {
        return new CameraPosition.Builder().target(latLng).zoom(16).build();
    }


    private void showMarker(@NonNull Location currentLocation) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (currentLocationMarker == null)
            currentLocationMarker = googleMap.addMarker(new MarkerOptions().icon(bitmapDescriptorFromVector(this, R.drawable.currentlocation_marker)).position(latLng));
        else {
            MarkerAnimation.animateMarkerToGB(currentLocationMarker, latLng, new LatLngInterpolator.Spherical());
        }
    }

    /* Location Request is to tell how much time interval we need after every location
     *  We also can set the location Accuracy with the location Request which we set to high accuracy
     *  One more thing that we are checking is also if the user has enabled the location permission or now*/

    private void startCurrentLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                return;
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }


    /* Saving the user location in the current location object because we need the location when user tap on the current location button */
    private void animateCamera(@NonNull Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        GoogleMap.CancelableCallback cancelableCallback = new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                rotatoE = "n";
                no();
            }

            @Override
            public void onCancel() {
                /* Do nothing. I just want the onFinish thingy */
            }
        };
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)), cancelableCallback);
    }


    /* ############################################
            USELESS FUNCTION BUT NEED TO IMPLEMENT
       ############################################*/

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /* To not make my class and abstract one I will implement and empty method signature from the interface SensorEventListener */
    }


}
