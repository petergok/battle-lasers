package com.pianist.battlelasers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;

public class MakeMoveTask extends AsyncTask<Void, Void, String>
{
	private Point mLastMoveStart;
	private Point mLastMoveEnd;
	private int mPlayerId;
	
	public MakeMoveTask(Point lastMoveStart, Point lastMoveEnd, int playerId) {
		mLastMoveStart = lastMoveStart;
		mLastMoveEnd = lastMoveEnd;
		mPlayerId = playerId;
	}
	
	@Override
	protected String doInBackground(Void... args)
	{
		HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = "none";
        String uri = BattleLaserGame.BASE_URL + "/player/" + mPlayerId + "/move";
        try {
        	HttpPut method = new HttpPut(uri);
        	
        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
            nameValuePairs.add(new BasicNameValuePair("lastMoveStartX", "" + mLastMoveStart.x));
            nameValuePairs.add(new BasicNameValuePair("lastMoveStartY", "" + mLastMoveStart.y));
            nameValuePairs.add(new BasicNameValuePair("endX", "" + mLastMoveEnd.x));
            nameValuePairs.add(new BasicNameValuePair("endY", "" + mLastMoveEnd.y));
            method.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            response = httpclient.execute(method);
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
        Log.d("RESPONSE", result);
    }
}

