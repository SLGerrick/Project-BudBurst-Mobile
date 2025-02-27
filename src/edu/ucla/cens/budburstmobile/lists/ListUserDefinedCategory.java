package edu.ucla.cens.budburstmobile.lists;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.database.OneTimeDBHelper;
import edu.ucla.cens.budburstmobile.helper.HelperSettings;
import edu.ucla.cens.budburstmobile.helper.HelperValues;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
//import edu.ucla.cens.budburstmobile.helper.HelperSharedPreference;

public class ListUserDefinedCategory extends AsyncTask<ListItems, Void, Void>{

	private Context mContext;
	private ProgressDialog mDialog;
	private boolean[] mSelect;
	private String[] mGroupName;
//	private HelperSharedPreference mPref;
	private ArrayList<ListGroupItem> mArr;
	
	public ListUserDefinedCategory(Context context) {
		mContext = context;
		mDialog = ProgressDialog.show(context, mContext.getString(R.string.Alert_pleaseWait), 
				mContext.getString(R.string.Alt_User_Defined_Group), true);
		mDialog.setCancelable(true);
		mDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				// TODO Auto-generated method stub
				cancel(true);
			}
		});		
	}
	
	@Override
	protected void onCancelled() {
	
	}
	
	
	// Getting user defined group lists from the server.
	@Override
	protected Void doInBackground(ListItems... item) {
		// TODO Auto-generated method stub
		/*
		 * Make the folder 
		 */
		File f = new File(HelperValues.TREE_PATH);
		if(!f.exists()) {
			f.mkdir();
		}
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(mContext.getString(R.string.get_user_defined_group) 
				+ "?lat=" + item[0].latitude + "&lon=" + item[0].longitude);
		String result = "";
		
		try {
			HttpResponse response = httpClient.execute(httpPost);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				
				StringBuilder result_str = new StringBuilder();
				String line = "";
				
				while((line = br.readLine()) != null){
					result_str.append(line);
				}
				
				result = result_str.toString();
				
				JSONObject jsonObj = new JSONObject(result);
				JSONArray jsonAry = jsonObj.getJSONArray("results");
				
				/*
				 * Open OneTime Database and delete the list first - by category
				 * After that, reINSERT the values into the database
				 */
				OneTimeDBHelper otDBH = new OneTimeDBHelper(mContext);
				otDBH.clearUserDefinedGroup(mContext);

				// Delete all images in the folder
				Log.i("K", "Json Array Length : " + jsonAry.length());	
				SQLiteDatabase otDB = null;
				
				// ReINSERT the values
				for(int i = 0 ; i < jsonAry.length() ; i++) {
					
					try {
						otDB = otDBH.getWritableDatabase();
						
						otDB.execSQL("INSERT INTO userDefinedGroup VALUES(" +
								jsonAry.getJSONObject(i).getString("Category_ID")  + ",\"" +
								jsonAry.getJSONObject(i).getString("Category_Name")  + "\"," +
								jsonAry.getJSONObject(i).getString("User_Admin_ID") + "," +
								jsonAry.getJSONObject(i).getString("Latitude")  + "," +
								jsonAry.getJSONObject(i).getString("Longitude")  + ",\"" +
								jsonAry.getJSONObject(i).getString("Description") + "\",\"" +
								jsonAry.getJSONObject(i).getString("Long_Description") + "\",\"" +
								jsonAry.getJSONObject(i).getString("icon_url") + "\"," +
								jsonAry.getJSONObject(i).getString("Distance") + ");"
							);
						
						URL urls = new URL(jsonAry.getJSONObject(i).getString("icon_url"));
						HttpURLConnection conn = (HttpURLConnection)urls.openConnection();
						conn.connect();
						
						File hasImage = new File(HelperValues.TREE_PATH
								+ "group_"
								+ jsonAry.getJSONObject(i).getString("Category_ID") 
								+ ".jpg");
						if(hasImage.exists()) {
							Log.i("K", "already has the image");
						}
						else {
							// save the user defined groups' image into the sdcard.
							int read;
							try {
								
								Object getContent = urls.getContent();
								
								Log.i("K", "getContent : " + getContent);
								conn = (HttpURLConnection)urls.openConnection();
								conn.connect();
								
								int len = conn.getContentLength();
								
								byte[] buffer = new byte[len];
								InputStream is = conn.getInputStream();
								FileOutputStream fos = new FileOutputStream(HelperValues.TREE_PATH 
										+ "group_"
										+ jsonAry.getJSONObject(i).getString("Category_ID") 
										+ ".jpg");
								
								while ((read = is.read(buffer)) > 0) {
									fos.write(buffer, 0, read);
								}
								fos.close();
								is.close();
							}
							catch(Exception e) {
								
							}
						}
					}
					catch(SQLiteException ex) {
						ex.printStackTrace();
						otDB.close();
					}
				}
				// DB close
				otDB.close();
				otDBH.close();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void Void) {
		Log.i("K", "Download complete - userDefinedGroup");
		
		mDialog.dismiss();
		
		showUserDefinedLists();
	}
	
	// based on the user's selection,
	// download the user defined lists.
	// by passing the parameters. 
	private void showUserDefinedLists() {
		// call user defined lists
		OneTimeDBHelper oDBH = new OneTimeDBHelper(mContext);
		mArr = new ArrayList<ListGroupItem>();
		mArr = oDBH.getListGroupItem(mContext);
		
		int groupCnt = mArr.size();
		mGroupName = new String[groupCnt];
		mSelect = new boolean[groupCnt];
		
		for(int i = 0 ; i < groupCnt ; i++) {
			mGroupName[i] = mArr.get(i).getCategoryName();
			mSelect[i] = false;
		}
		
		new AlertDialog.Builder(mContext)
		.setTitle(mContext.getString(R.string.User_Defined_Lists_Group))
		.setMultiChoiceItems(mGroupName, mSelect, new DialogInterface.OnMultiChoiceClickListener() {
//		.setSingleChoiceItems(mGroupName, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				// TODO Auto-generated method stub
				
	/*			AlertDialog d = (AlertDialog) dialog;
                ListView v = d.getListView();
                v.getItemAtPosition(which);
                v.setChoiceMode(v.CHOICE_MODE_MULTIPLE);
			*/	
				if(isChecked){
					mSelect[which]=true;
				}
				else{
					mSelect[which] = false;
				}
			}
		})
		.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				for(int i = 0 ; i < mSelect.length ; i++) {
					if(mSelect[i]) {
						ListUserDefinedSpeciesDownload userPlant = new ListUserDefinedSpeciesDownload(mContext
								, mArr.get(i).getCategoryID());
						userPlant.execute();
					}
				}
			}
		})
		.setNeutralButton(mContext.getString(R.string.Button_back), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		})
		.show();
	}
}
