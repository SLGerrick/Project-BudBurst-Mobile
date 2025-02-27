package edu.ucla.cens.budburstmobile.lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.database.OneTimeDBHelper;
import edu.ucla.cens.budburstmobile.helper.HelperFunctionCalls;
import edu.ucla.cens.budburstmobile.helper.HelperSharedPreference;
import edu.ucla.cens.budburstmobile.helper.HelperValues;

/**
 * Class for downloading the user defined lists
 * @author kyunghan
 *
 */
public class ListUserDefinedSpeciesDownload extends AsyncTask<Void, Void, Void>{

	private Context mContext;
	private HelperSharedPreference mPref;
	private int mCategory;
	private NotificationManager notificationMgr = null;
	private Notification noti = null;
	private int SIMPLE_NOTFICATION_ID = HelperValues.NOTIFI_USER_DEFINED_LISTS;
	
	public ListUserDefinedSpeciesDownload(Context context, int category) {
		mContext = context;
		mCategory = category;
		mPref = new HelperSharedPreference(mContext);
	}
	
	@Override
	protected void onPreExecute() {
		notificationMgr = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
		noti = new Notification(android.R.drawable.stat_sys_download, mContext.getString(R.string.Start_Downloading_UCLA_Tree_Lists), System.currentTimeMillis());
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, null, PendingIntent.FLAG_CANCEL_CURRENT);
		
		noti.setLatestEventInfo(mContext, mContext.getString(R.string.List_Project_Budburst_title), mContext.getString(R.string.Downloading_User_Defined_Lists), contentIntent);
		
		notificationMgr.notify(SIMPLE_NOTFICATION_ID, noti);
	}
	
	@Override
	protected Void doInBackground(Void... unused) {

		downloadUserDefinedList();
		return null;
	}
	
	@Override
	protected void onPostExecute(Void unused) {
		
		setPreference();
		
		noti = new Notification(R.drawable.s1000, 
				mContext.getString(R.string.User_Defined_Lists_Download_Complete), System.currentTimeMillis());
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, null, PendingIntent.FLAG_CANCEL_CURRENT);
		
		noti.setLatestEventInfo(mContext, mContext.getString(R.string.List_Project_Budburst_title), 
				mContext.getString(R.string.User_Defined_Lists_Download_Complete2), contentIntent);
		
		notificationMgr.notify(SIMPLE_NOTFICATION_ID, noti);
	}
	
	public void downloadUserDefinedList() {
		
		Log.i("K", "Start Downloading User-Defined-Lists.");
		
		HttpClient httpClient = new DefaultHttpClient();
		String url = new String(mContext.getString(R.string.get_user_defined_species_lists));
		HttpPost httpPost = new HttpPost(url);
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				
				/*
				 * Delete values in UCLAtreeLists table
				 */
				OneTimeDBHelper onehelper = new OneTimeDBHelper(mContext);
				onehelper.clearUserDefineListByCategory(mContext, mCategory);
				onehelper.close();
				mPref.setPreferencesBoolean(""+mCategory, true);
				
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String serverResponse = ""; 
				serverResponse += br.readLine();
			
				
				JSONObject jsonObj = new JSONObject(serverResponse);
				if(jsonObj.getBoolean("success")) {
					JSONArray jsonAry = jsonObj.getJSONArray("results");
					
					OneTimeDBHelper otDBH = new OneTimeDBHelper(mContext);
					SQLiteDatabase otDB = otDBH.getWritableDatabase();

					for(int i = 0 ; i < jsonAry.length() ; i++) {
						
						
						HelperFunctionCalls helper = new HelperFunctionCalls();
						
						try {
							String cat = jsonAry.getJSONObject(i).getString("Category");
							if(Integer.parseInt(cat) != mCategory) {
								continue;
							}

							otDB.execSQL("INSERT INTO userDefineLists VALUES(" +
									jsonAry.getJSONObject(i).getString("Tree_ID") + "," +
									"\"" + jsonAry.getJSONObject(i).getString("Common_Name") + "\"," +
									"\"" + jsonAry.getJSONObject(i).getString("Science_Name") + "\"," + 
									"\"" + jsonAry.getJSONObject(i).getString("Credit") + "\"," +
									"" + mCategory + "," +
									jsonAry.getJSONObject(i).getString("Protocol_ID") + "," +
									"\"" + jsonAry.getJSONObject(i).getString("Description") + "\"" + 
									");"
									);
							
							URL urls = new URL(mContext.getString(R.string.user_plant_lists_image) 
									+ jsonAry.getJSONObject(i).getString("Tree_ID") + "_thumb.jpg");
							HttpURLConnection conn = (HttpURLConnection)urls.openConnection();
							conn.connect();
							
							/*
							 * ResponseCode 404, means there is no photo related to the corresponding id
							 * So in this case, we alternatively link to basic tree photo. (100_thumb.jpg)
							 */
							if(conn.getResponseCode() == 404) {
							
								urls = new URL(mContext.getString(R.string.user_plant_lists_image) + "0_thumb.jpg");
								conn = (HttpURLConnection)urls.openConnection();
								conn.connect();
							}
							
							
							File hasImage = new File(HelperValues.TREE_PATH + jsonAry.getJSONObject(i).getString("Tree_ID") + ".jpg");

							// if there's an image in the SDcard, delete it and redownload the species again.
							if(hasImage.exists()) {
								Log.i("K", "already has the image in the SDcard, delete it.");
								hasImage.delete();
							}
							
							// download user defined images
							int read;
							try {
								
								Object getContent = urls.getContent();
								
								Log.i("K", "Downloading user defined image from the server.");
								conn = (HttpURLConnection)urls.openConnection();
								conn.connect();
								
								int len = conn.getContentLength();
								
								byte[] buffer = new byte[len];
								InputStream is = conn.getInputStream();
								FileOutputStream fos = new FileOutputStream(HelperValues.TREE_PATH + jsonAry.getJSONObject(i).getString("Tree_ID") + ".jpg");
								
								while ((read = is.read(buffer)) > 0) {
									fos.write(buffer, 0, read);
								}
								fos.close();
								is.close();
							}
							catch(Exception e) {
								
							}
						}
						catch(Exception e) {
							otDBH.close();
							otDB.close();
						}
					}
					
					otDBH.close();
					otDB.close();
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setPreference() {
		/*
		 * Set the boolean variable to TRUE
		 */
		HelperSharedPreference hPref = new HelperSharedPreference(mContext);
		hPref.setPreferencesBoolean("getTreeLists", true);
		hPref.setPreferencesBoolean("firstDownloadTreeList", true);
	}
}