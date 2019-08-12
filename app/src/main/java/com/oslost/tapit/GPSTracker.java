package com.oslost.tapit;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

public class GPSTracker extends Service implements LocationListener {
    private Context mContext = null;
    boolean isGPSEnabled = false; //flag for GPA status
    boolean isNetworkEnabled = false; // flag for network status
    boolean canGetLocation = false; //flag for GPS status


    Location location; //location
    double latitude; //latitude
    double longitude; //longitude

    //The min distance to change Update in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //10 meters
    //The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000*60*1; // 1 minute
    //Declaring a  Location Manager
    protected LocationManager locationManager;

    public GPSTracker() {
        checkGPSPermissions();
    }

    public GPSTracker(Context context) {
        this.mContext = context;
        checkGPSPermissions();
    }

    public Location getLocation() {
        this.canGetLocation = false;
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            //getting GPS Status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            //getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                //no network provider is enabled
                //prompt user to enable location service
                showEnableLocationAlert();
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    //Permission granted
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }

                    }

                }
                // if GPS Enabled get lat/long using GPS services
                if(isGPSEnabled) {
                    if(location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if(locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if(location !=null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return location;
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if(location !=null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if(location !=null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */

    public void stopUsingGPS() {
        if(locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return Boolean
     */

    public boolean canGetLocation() {
        checkGPSPermissions();
        return canGetLocation;
    }

    //check for location permission
    public void checkGPSPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionState2 = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED) {
            //Permission granted, get GPS location
            getLocation();
        }
        else {
            //Prompt user to enable location permission
            showEnablePermissionAlert();
        }
    }
    /**
     * Function to show settings alert dialog On pressing Settings button will launch Settings Options
     */
    public void showEnablePermissionAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        //Setting Dialog Title
        alertDialog.setTitle("Location and Storage Permission Settings");
        //Setting Dialog Message
        alertDialog.setMessage("Gravitask Location and Storage Permission is not enabled. Do you want to go to setting menu?");
        //On press Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                //Goto Application Setting
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        //Showing Alert Message
        alertDialog.show();
    }

    public void showEnableLocationAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        //Setting Dialog Title
        alertDialogBuilder.setTitle("Location Service Settings");
        alertDialogBuilder.setMessage("Location service is diabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page to Enable Location Service", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Goto location service setting
                        Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(callGPSSettingIntent);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        getLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider){
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
