package edu.ucla.cens.budburstmobile.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;

import edu.ucla.cens.budburstmobile.PBBLogin;
import edu.ucla.cens.budburstmobile.PBBMainPage;
import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.database.OneTimeDBHelper;
import edu.ucla.cens.budburstmobile.database.SyncDBHelper;
import edu.ucla.cens.budburstmobile.floracaching.FloraCacheEasyLevel;
import edu.ucla.cens.budburstmobile.floracaching.FloraCacheOverlay;
import edu.ucla.cens.budburstmobile.floracaching.FloracacheGetLists;
import edu.ucla.cens.budburstmobile.lists.ListGroupItem;
import edu.ucla.cens.budburstmobile.lists.ListItems;
import edu.ucla.cens.budburstmobile.lists.ListMain;
import edu.ucla.cens.budburstmobile.lists.ListUserDefinedCategory;
import edu.ucla.cens.budburstmobile.lists.ListUserDefinedSpeciesDownload;
import edu.ucla.cens.budburstmobile.onetime.OneTimeMainPage;
import edu.ucla.cens.budburstmobile.utils.ImageViewPreference;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Setting class
 * All downloading processes are done in this activity
 * Since the amount of time to download the lists varies depending on the connectivity level.
 * and especially, the user defined lists are pretty huge to download, therefore, the application
 * expects the user to be in the good connectivity area.
 * 1. Downloading Local Lists
 * 2. Downloading User Defined Lists
 * 3. Downloading Floracache Lists
 * 4. Check the version
 * 5. Logout
 * @author kyunghan
 *
 */
public class HelperSettings extends PreferenceActivity {

	private HelperSharedPreference mPref;
	private String mUsername;
	private int mPreviousActivity;
	private int mOneTimeMainPreviousActivity;
	private ArrayList<ListGroupItem> mArr;
	private boolean[] mSelect;
	private String[] mGroupName;
	static final int UNINSTALL_REQUEST = 0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    mArr = new ArrayList<ListGroupItem>();
	    
	    mPref = new HelperSharedPreference(this);
	    Intent pIntent = getIntent();
	 
	    // get the intent named as 'from'
	    // since only the previous activity from main_page.java enables the logout button,
	    // otherwise, gets disabled.
	    mPreviousActivity = pIntent.getExtras().getInt("from");
	    
	    addPreferencesFromResource(R.xml.preferences);
	    // TODO Auto-generated method stub
	    
	    Preference downloadListPref = (Preference) findPreference("downloadLists");
	    downloadListPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				
				new AlertDialog.Builder(HelperSettings.this)
				.setTitle(getString(R.string.List_Download_Title))
				.setMessage(getString(R.string.List_ask_connectivity))
				.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
						HelperFunctionCalls helper = new HelperFunctionCalls();
						helper.changeSharedPreference(HelperSettings.this);

						// getting contents again...
						Toast.makeText(HelperSettings.this, getString(R.string.Start_Downloading), Toast.LENGTH_SHORT).show();
						HelperRefreshPlantLists getLocalList = new HelperRefreshPlantLists(HelperSettings.this);
						getLocalList.execute();
					}
				})
				.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				})
				.show();
				return true;
			}
	    	
	    });
	    
	    Preference downloadUserDefinedListPref = (Preference) findPreference("downloadDefinedLists");
	    downloadUserDefinedListPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				checkConnectivity();
				return true;
			}
	    });
	    
	    Preference downloadFloracachePref = (Preference) findPreference("downloadFloracache");
	    downloadFloracachePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				
				new AlertDialog.Builder(HelperSettings.this)
				.setTitle(getString(R.string.DownLoad_Tree_Lists))
				.setMessage(getString(R.string.List_ask_connectivity))
				.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						Toast.makeText(HelperSettings.this, getString(R.string.Start_Downloading_Floracache_Lists), Toast.LENGTH_SHORT).show();
						
						FloracacheGetLists fLists = new FloracacheGetLists(HelperSettings.this);
						fLists.execute(getString(R.string.get_floracaching_plant_lists));
					}
				})
				.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				})
				.show();
				return true;
			}
	    	
	    });
	    
	    mUsername = mPref.getPreferenceString("Username", "");
	    if(mUsername.equals("test10")){
	    	mUsername = "Preview";
	    }

	    //Preference updatePref = (Preference) findPreference("update");
	    ImageViewPreference imagePref = (ImageViewPreference) findPreference("key1");
	    Resources res = getResources();
	    if(mPref.getPreferenceBoolean("needUpdate")) {
		    Drawable icon = res.getDrawable(R.drawable.upload_icon);
		    imagePref.setComponent(icon, getString(R.string.Upgrade_Needed_Text));
	    }
	    else {
	    	Drawable icon = res.getDrawable(R.drawable.pbb_icon_main2);
		    imagePref.setComponent(icon, getString(R.string.No_Upgrade_Needed_2));
	    }
	    
	    imagePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				if(mPref.getPreferenceBoolean("needUpdate")) {
					new AlertDialog.Builder(HelperSettings.this)
					.setTitle(getString(R.string.Upgrade_app_text))
					.setMessage(getString(R.string.Move_to_the_market_Text))
					.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse("market://details?id=edu.ucla.cens.budburstmobile"));
							startActivity(intent);
						}
					})
					.setNegativeButton(getString(R.string.Button_back), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					})
					.show();
				}
				else {
					Toast.makeText(HelperSettings.this, getString(R.string.No_Upgrade_Needed), Toast.LENGTH_SHORT).show();
				}
				return false;
			}
	    });
	    
	    Preference logoutPref = (Preference) findPreference("userlogout");
	    if(mPreviousActivity != HelperValues.FROM_MAIN_PAGE) {
	    	logoutPref.setEnabled(false);
	    	logoutPref.setSummary("Only logout from the main page");
	    }
	    
	    logoutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(HelperSettings.this)
				.setTitle(getString(R.string.Menu_logout) + " - " + mUsername)
				.setMessage(getString(R.string.Alert_logout))
				.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
						// initialize the preference values
						mPref.setPreferencesString("Username", "");
						mPref.setPreferencesString("Password", "");
						mPref.setPreferencesBoolean("getSynced", false);
						mPref.setPreferencesBoolean("getTreeLists", false);
						mPref.setPreferencesBoolean("Update", false);
						mPref.setPreferencesBoolean("localbudburst", false);
						mPref.setPreferencesBoolean("localwhatsinvasive", false);
						mPref.setPreferencesBoolean("localpoisonous", false);
						mPref.setPreferencesBoolean("localendangered", false);
						mPref.setPreferencesBoolean("listDownloaded", false);
						mPref.setPreferencesBoolean("floracache", false);
						mPref.setPreferencesBoolean("firstDownloadTreeList", true);
						mPref.setPreferencesBoolean("needUpdate", false);
						
						//Drop user table in database
						SyncDBHelper sDBH = new SyncDBHelper(HelperSettings.this);
						OneTimeDBHelper oDBH = new OneTimeDBHelper(HelperSettings.this);
						SQLiteDatabase oDB = oDBH.getWritableDatabase();
						
						sDBH.clearAllTable(HelperSettings.this);
						oDBH.clearAllTable(HelperSettings.this);
						sDBH.close();
						oDBH.close();

						HelperFunctionCalls helper = new HelperFunctionCalls();
						helper.deleteContents(HelperValues.BASE_PATH);

						Intent intent = new Intent(HelperSettings.this, PBBLogin.class);
				        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra("from", HelperValues.FROM_SETTINGS);
						startActivity(intent);
						
						stopService(new Intent(HelperSettings.this, HelperGpsHandler.class));
						
						finish();
					}
				})
				.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				})
				.show();
				return true;
			}
	    });
	}
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == UNINSTALL_REQUEST) {
            if (resultCode == RESULT_OK) {
            	Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id=edu.ucla.cens.budburstmobile"));
				startActivity(intent);
            }
        }
    }
	
	// always do check network connectivity first
	private void checkConnectivity() {
		new AlertDialog.Builder(HelperSettings.this)
		.setTitle(getString(R.string.DownLoad_Tree_Lists))
		.setMessage(getString(R.string.List_ask_connectivity))
		.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub				
				downloadGroupList();

			}
		})
		.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		})
		.show();

	}
	
	
	private void downloadGroupList() {

		ListUserDefinedCategory userCategory = new ListUserDefinedCategory(HelperSettings.this);
		
		double getLatitude = Double.parseDouble(mPref.getPreferenceString("latitude", "0.0"));
		double getLongitude = Double.parseDouble(mPref.getPreferenceString("longitude", "0.0"));
		
		ListItems lItem = new ListItems(getLatitude, getLongitude);
		userCategory.execute(lItem);	
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {
			switch(mPreviousActivity) {
			case HelperValues.FROM_MAIN_PAGE:
				startActivity(new Intent(this, PBBMainPage.class));
				break;
			case HelperValues.FROM_PLANT_LIST:
				finish();
				break;
			case HelperValues.FROM_ONE_TIME_MAIN:
				finish();
				break;
			}
			return true;
		}
		return false;
	}	
	
}
