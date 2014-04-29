package com.pianist.battlelasers;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class GcmIntentService extends IntentService {
	
	private static String TAG = "GcmIntentService";
	
    public static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
    BattleLaserGame game;

    public GcmIntentService() {
        super(BattleLaserGame.SENDER_ID);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            	Log.d(TAG, "Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
            	Log.d(TAG, "Deleted messages on server: " + extras.toString());
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                Log.d(TAG, "Received: " + extras.toString());
                parseAndReportMessage(extras);
            }
        }
    }
    
    private void parseAndReportMessage(Bundle data) {
    	try {
    		
    		String messageType = data.getString("messageType");
    		if (messageType == null) {
    			return;
    		}
    		if (messageType.equals("startMatch")) {
    			String otherPlayerName = data.getString("otherPlayerName");
    			int playerNumber = data.getInt("playerNumber");
    			int mapId = data.getInt("mapId");
    			Intent intent = new Intent(BattleLaserGame.MATCH_STARTED);
    			intent.putExtra("otherPlayerName", otherPlayerName);
    			intent.putExtra("playerNumber", playerNumber);
    			intent.putExtra("mapId", mapId);
    			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    		} else if (messageType.equals("move")) {
    			int startX = data.getInt("startX");
    			int startY = data.getInt("startY");
    			int endX = data.getInt("endX");
    			int endY = data.getInt("endY");
    			Intent intent = new Intent(BattleLaserGame.MOVE);
    			intent.putExtra("startX", startX);
    			intent.putExtra("startY", startY);
    			intent.putExtra("endX", endX);
    			intent.putExtra("endY", endY);
    			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    		}
    		
    	} catch (Exception e) {
    		
    	}
    }
}