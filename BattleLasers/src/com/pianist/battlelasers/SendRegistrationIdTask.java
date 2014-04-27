package com.pianist.battlelasers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class SendRegistrationIdTask extends AsyncTask<Void, Void, String>
{
	String mUri;
	BattleLaserGame mActivity;
	
	public SendRegistrationIdTask(String uri, BattleLaserGame activity) {
		mUri = uri;
		this.mActivity = activity;
	}
	
	@Override
	protected String doInBackground(Void... args)
	{
		HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = "none";
        try {
            response = httpclient.execute(new HttpPut(mUri));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        } catch (IOException e) {
            //TODO Handle problems..
        }
        return responseString;
	}
	
	@Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        int id = getId(result);
        if (id >= 0) {
        	mActivity.registeredUser(id);
        }
        Log.d("RESPONSE", result);
    }
	
	private static int getId(String s) {
		int id = -1;
	    try { 
	        id = Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return -1; 
	    }
	    // only got here if we didn't return false
	    return id;
	}
}

