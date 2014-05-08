package com.pianist.battlelasers.gcm;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.pianist.battlelasers.activities.BattleLaserActivity;

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
    BattleLaserActivity game;

    public GcmIntentService() {
        super(BattleLaserActivity.SENDER_ID);
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
    		if (messageType.equals("matchFound")) {
    			String otherPlayerName = data.getString("otherPlayerName");
    			int playerNumber = Integer.parseInt(data.getString("playerNumber"));
    			int mapId = Integer.parseInt(data.getString("mapId"));
    			int otherPlayerRating = Integer.parseInt(data.getString("otherPlayerRating"));
    			Intent intent = new Intent(BattleLaserActivity.MATCH_FOUND);
    			intent.putExtra("otherPlayerName", otherPlayerName);
    			intent.putExtra("otherPlayerRating", otherPlayerRating);
    			intent.putExtra("playerNumber", playerNumber);
    			intent.putExtra("mapId", mapId);
    			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    		} else if (messageType.equals("move")) {
    			int startRow = Integer.parseInt(data.getString("startRow"));
    			int startCol = Integer.parseInt(data.getString("startCol"));
    			int endRow = Integer.parseInt(data.getString("endRow"));
    			int endCol = Integer.parseInt(data.getString("endCol"));
    			boolean turnRight = data.getString("turnRight").equals("true");
    			Intent intent = new Intent(BattleLaserActivity.MOVE);
    			intent.putExtra("startRow", startRow);
    			intent.putExtra("startCol", startCol);
    			intent.putExtra("endRow", endRow);
    			intent.putExtra("endCol", endCol);
    			intent.putExtra("turnRight", turnRight);
    			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    		} else if (messageType.equals("matchStart")) {
    			Intent intent = new Intent(BattleLaserActivity.MATCH_START);
    			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    		} else if (messageType.equals("matchEnd")) {
    			Intent intent = new Intent(BattleLaserActivity.MATCH_END);
    			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    		}
    		
    	} catch (Exception e) {
    		Log.e("EXCEPTION", "exception: ", e);
    	}
    }
}