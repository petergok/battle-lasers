package com.pianist.battlelasers.tasks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;

import com.pianist.battlelasers.activities.BattleLaserGame;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class UnregisterPlayerTask extends AsyncTask<Void, Void, String>
{
	private int mPlayerId;
	
	public UnregisterPlayerTask(int playerId) {
		mPlayerId = playerId;
	}
	
	@Override
	protected String doInBackground(Void... args)
	{
		HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = "none";
        String uri = BattleLaserGame.BASE_URL + "/player/" + mPlayerId;
        try {
        	HttpDelete method = new HttpDelete(uri);
        	
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
        SharedPreferences.Editor editor = BattleLaserGame.settings.edit();
	    editor.putInt(BattleLaserGame.PREF_USER_ID, 0);
	    editor.commit();
        Log.d("RESPONSE", result);
    }
}

