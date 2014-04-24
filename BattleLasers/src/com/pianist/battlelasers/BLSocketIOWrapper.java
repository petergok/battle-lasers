package com.pianist.battlelasers;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.JSONCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.koushikdutta.async.http.socketio.StringCallback;

public class BLSocketIOWrapper
{
	private static String TAG = "BLSocketIOWrapper";
	
	public static void connectTo(String host) {
		SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), host, new BLConnectCallback());
	}
	
	private static class BLConnectCallback implements ConnectCallback {

		@Override
		public void onConnectCompleted(Exception e, SocketIOClient client)
		{
			if (e != null) {
				Log.e(TAG, "Error in connecting: ", e);
				return;
			}
			
			client.setStringCallback(new BLStringCallback());
			client.on("connected", new ConnectedCallback());
			client.setJSONCallback(new BLJSONCallback());
		}
		
	}
	
	private static class BLStringCallback implements StringCallback {
		@Override
		public void onString(String string, Acknowledge ack)
		{
			Log.d(TAG, "StringCallback: " + string);
		}
	}
	
	private static class ConnectedCallback implements EventCallback {

		@Override
		public void onEvent(JSONArray args, Acknowledge ack)
		{
			Log.d(TAG, "ConnectedCallback: " + args.toString());
		}
	}
	
	private static class BLJSONCallback implements JSONCallback {

		@Override
		public void onJSON(JSONObject args, Acknowledge ack)
		{
			Log.d(TAG, "JSONCallback: " + args.toString());
		}
	}
}
