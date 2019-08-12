package com.oslost.tapit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap googleMap;
    private GoogleMap mMap;
    private GPSTracker gpsTracker;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private SharedPreferences showLocation;

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

        /* #################################################################
        SharedPrefs showLocation contains the following key-value pair:

        - MyLocationName : <Location Name>
        - MyLatitude : <Location Lat>
        - MyLongitude : <Location Lng>

       ################################################################# */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        GPSTracker gpsTracker = new GPSTracker(this);

        /* Obtain the SupportMapFragment and get notified when the map is ready to be used. */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /* Compass Enable click Listender */
        findViewById(R.id.compassEnableButton).setOnClickListener(compass);

        /* Set Listener for the current location button */
        findViewById(R.id.currentLocationImageButton).setOnClickListener(clickListener);

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
        String myLatS = loadPrefs("MyLatitude", null).trim();
        String myLngS = loadPrefs("MyLongitude", null).trim();
        Log.i(TAG, "LatS :" + myLatS);
        Log.i(TAG, "LatS :" + myLngS);


       /* LatLng sp = new LatLng(1.3099, 103.7775);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sp,16)); */

       if(myLatS !=null && myLngS !=null){
           double myLat = Double.parseDouble(myLatS);
           double myLng = Double.parseDouble(myLngS);
           LatLng current = new LatLng(myLat,myLng);
           Log.i(TAG, "Lat :" + myLat);
           Log.i(TAG, "Lat :" + myLng);
           //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,16));
       }


       /* Style the map */
        styleMyMap();

        /* Ground Overlay of SP */
        groundOverLaySP(this.googleMap);


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

    /* #############################
            GEOFENCE FUNCTIONS
       #############################*/


    /* #################################################
        SHARED PREFERENCES SAVING AND LOADING FUNCTIONS
       ################################################# */

    public void savePrefs(String key, float value){
        showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = showLocation.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public void savePrefs(String key, String value){
        showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = showLocation.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private float loadPrefs(String key, float value){
        showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        float data = showLocation.getFloat(key, value);
        return data;
    }

    private String loadPrefs(String key, String value){
        showLocation = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String data = showLocation.getString(key, value).trim();
        return data;
    }

    /* ###############################
           STYLE MAP FUNCTIONS
       ###############################*/
    private void styleMyMap(){
        try {
            // Time Check for night
            if ((getCurrentTime() < 24 && getCurrentTime() > 17) || (getCurrentTime() >= 0 && getCurrentTime() < 7)) {
                boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_night));

                if (!success) {
                    Log.e(TAG, "Style parsing failed.");
                }
            } else {
                // Time for morning
                boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_morning));

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
            if (firstTimeFlag && googleMap != null) {
                rotatoE = "n";
               // animateCamera(currentLocation);
                firstTimeFlag = false;
            }
            showMarker(currentLocation);
        }
    };

     /* #################################################
                ON CLICK LISTENER FUNCTIONS
       #################################################*/

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            rotatoE = "n";
            if (view.getId() == R.id.currentLocationImageButton && googleMap != null && currentLocation != null) {
               // MapsActivity.this.animateCamera(currentLocation);
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
    private void groundOverLaySP(GoogleMap googleMap) {

        mMap = googleMap;
        BitmapDescriptor spBitmap = BitmapDescriptorFactory.fromResource(R.raw.resizedmap);

        LatLng southWest = new LatLng(1.1826, 103.4619);
        LatLng NorthEast = new LatLng(1.1842, 103.4657);

        LatLngBounds latLngBounds = new LatLngBounds(southWest, NorthEast);

        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
        groundOverlayOptions.positionFromBounds(latLngBounds);
        groundOverlayOptions.bearing(-21.0694f);
        groundOverlayOptions.image(spBitmap);
        groundOverlayOptions.transparency(0.2f);
        groundOverlayOptions.visible(true);

        // mMap.addGroundOverlay(groundOverlayOptions);


    }

    /* ######################################
                CHECK PERMISSION FUNCTION
       ######################################*/


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
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, "Permission denied by uses", Toast.LENGTH_SHORT).show();
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startCurrentLocationUpdates();
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
                rotatoE = "y";
                yes();
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
