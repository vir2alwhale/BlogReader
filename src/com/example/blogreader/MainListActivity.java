package com.example.blogreader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;


public class MainListActivity extends ListActivity {
	
	protected String[] mBlogPostTitles;
	public static final int NUMBER_OF_POSTS = 20;
	public static final String TAG = MainListActivity.class.getSimpleName();
	protected JSONObject mBlogData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        
        if(isNetworkAvailable()){
        	GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
        	getBlogPostsTask.execute();
        } else {
    		Toast.makeText(this,"Network in unavailable", Toast.LENGTH_LONG).show();
    	}
        //Toast.makeText(this, getString(R.string.no_items), Toast.LENGTH_LONG).show();
    }


    private boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		
		boolean isAvailable = false;
		if(networkInfo!=null && networkInfo.isConnected()) {
			isAvailable = true;
		}
		return isAvailable;
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_list, menu);
        return true;
    }
	
	public void updateList() {
		if(mBlogData==null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.error_title);
			builder.setMessage(R.string.error_message);
			builder.setPositiveButton(android.R.string.ok, null);
			
			AlertDialog dialog = builder.create();
			dialog.show();
			
		} else {
			try {
				JSONArray jsonPosts = mBlogData.getJSONArray("posts");
				mBlogPostTitles = new String[jsonPosts.length()]; 
				for(int i=0; i<jsonPosts.length(); i++) {
					JSONObject post = jsonPosts.getJSONObject(i);
					String title = post.getString("title");
					title = Html.fromHtml(title).toString();
					Log.v(TAG, title);
					mBlogPostTitles[i] = title;
				}
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mBlogPostTitles);
				setListAdapter(adapter);
				
			} catch (JSONException e) {
				Log.e(TAG, "Exception caught!", e);
			}
		}
	}
    
    private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(Object... params) {
			int responseCode = -1;
			JSONObject jsonResponse = null;
			
			try{
	        	URL blogFeedURL = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);
	        	HttpURLConnection connection = (HttpURLConnection) blogFeedURL.openConnection();
	        	connection.connect();
	        	
	        	responseCode = connection.getResponseCode();
	        	if(responseCode == HttpURLConnection.HTTP_OK) {
	        		InputStream inputStream = connection.getInputStream();
	        		Reader reader = new InputStreamReader(inputStream);
	        		int contentLength = connection.getContentLength();
	        		char[] charArray = new char [contentLength];
	        		reader.read(charArray);
	        		String responseData = new String(charArray);
	        		
	        		jsonResponse = new JSONObject(responseData);
	        		
	        	} else {
	        		Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
	        	}
	        }
	        catch(MalformedURLException e){
	        	Log.e(TAG, "MalformedURLException caught: ");
	        }
	        catch(IOException e){
	        	Log.e(TAG, "IOException caught: ");
	        }
			// This exception happens if phone doesn't have access to Wifi.
	        catch(Exception e){
	        	Log.e(TAG, "GenericException caught: ");
	        }
			
			return jsonResponse;
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			mBlogData = result;
			updateList();
		}
    	
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
