package com.oslost.tapit;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;


public class GeofenceTransitionService extends IntentService {

    private static final String TAG = GeofenceTransitionService.class.getSimpleName();

    public static final int GEOFENCE_NOTIFICATION_ID = 0;

    /* Notification Variables */
    private NotificationChannel mChannel;
    private String status;
    private static final String CHANNEL_ID = "my_channel_01";// The id of the channel.
    private int notifyID = 1;
    private static final int importance = NotificationManager.IMPORTANCE_HIGH;
    private  NotificationManager notificationManager;

    public GeofenceTransitionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        /* Error Handling */
        if (geofencingEvent.hasError()) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode());
            Log.i(TAG, errorMsg);
            return;
        }

        /* Get the transition type */
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();

        /* Check if the transition type is of interest */
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            /* Get the geofence that were triggered */
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            /* Get the transition details as a String. */
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geoFenceTransition, triggeringGeofences);

            /* Send notification details as a String */
            sendNotification(geofenceTransitionDetails);
            Log.i(TAG, "Geofence transition:" + geofenceTransitionDetails);
        }else {
            // Log the error.
            Log.e(TAG, "Geofence Transition invalid type");
        }
    }

    /* ##################################
        GEOFENCE TRANSITIONING FUNCTIONS
       ################################## */

    private String getGeofenceTransitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {

        /* Get the ID of each geofence triggered */
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesList.add(geofence.getRequestId());
        }

        status = null;
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            /*TODO: DELETE THIS LATER STRICTLY FOR BETA TESTING*/
            status = "You are already home! ";
            Log.i(TAG, "GEOFENCE IS ENTERING");


        } else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            status = "Did you remember to bring along your water bottle ? ";

            /*TODO: DELETE THIS LATER */
            Log.i(TAG, "GEOFENCE IS EXITING");

        }
        return status;


    }

    /* ########################
        NOTIFICATION FUNCTIONS
       ######################## */

    private void sendNotification(String msg) {
        Log.i(TAG, "sendNotification: " + msg);

        // Intent to start the main Activity
        Intent notificationIntent = MapsActivity.makeNotificationIntent(getApplicationContext(), msg);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MapsActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        CharSequence name = "name";// The user-visible name of the channel.

        /* Check for the minimum API level to assign notification Channel */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            /* Creating and sending Notification */
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
        }

        notificationManager.notify(GEOFENCE_NOTIFICATION_ID, createNotification(msg, notificationPendingIntent));


    }


    private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {

        Log.i(TAG, "NOTIFICATION IS WORKING");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        notificationBuilder
                .setSmallIcon(R.drawable.icon_notification)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("Good Job!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setChannelId(CHANNEL_ID);
        return notificationBuilder.build();
    }


    /* ########################
         ERROR STRING FUNCTION
       ######################## */


    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }

}