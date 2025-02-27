package edu.ucla.cens.budburstmobile;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.util.ByteArrayBuffer;

import edu.ucla.cens.budburstmobile.database.OneTimeDBHelper;
import edu.ucla.cens.budburstmobile.database.StaticDBHelper;
import edu.ucla.cens.budburstmobile.database.SyncDBHelper;
import edu.ucla.cens.budburstmobile.helper.HelperBackgroundService;
import edu.ucla.cens.budburstmobile.helper.HelperFunctionCalls;
import edu.ucla.cens.budburstmobile.helper.HelperRefreshPlantLists;
import edu.ucla.cens.budburstmobile.helper.HelperSettings;
import edu.ucla.cens.budburstmobile.helper.HelperSharedPreference;
import edu.ucla.cens.budburstmobile.helper.HelperValues;
import edu.ucla.cens.budburstmobile.lists.ListUserDefinedSpeciesDownload;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class firstActivity extends Activity{
	
	private String mGetCurrentVersion;
	private String mGetOldVersion;
	private HelperSharedPreference mPref;
	private LocationManager mLocManager;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.splash);
	    Log.i("K", "firstActivity - onCreate()");
	    // TODO Auto-generated method stub
	    
	    checkFileDirectory();
	}
	
	private void checkFileDirectory() {
		File file = new File(HelperValues.BASE_PATH);
		if(!file.exists()) {
			file.mkdir();
		}
	}
	
	public void onResume() {
		super.onResume();
		
		getCurrentVersion();
		moveToMainPage();
		
		Intent service = new Intent(firstActivity.this, HelperBackgroundService.class);
	    startService(service);
	}
	
	public void getCurrentVersion() {
		mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		ComponentName comp = new ComponentName(firstActivity.this, "");
		
		try {
			PackageInfo pInfo = this.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			mGetCurrentVersion = pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void moveToMainPage() {
		mPref = new HelperSharedPreference(firstActivity.this);
		
		// get the old version and the current version
		// compare them and if the version is different, update it.
		mGetOldVersion = mPref.getPreferenceString("version", mGetCurrentVersion);
		mPref.setPreferencesString("version", mGetCurrentVersion);
		
		if(!mGetCurrentVersion.equals(mGetOldVersion)) {
		 	downloadPlantLists();
		}
		else {
			
			
			if(!mLocManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
					&&(mPref.getPreferenceString("Username", "").equals("")) 
					&& mPref.getPreferenceString("Password", "").equals("")) {
				alert_no_gps();
		    }
		    else {
	
				new Handler().postDelayed(new Runnable(){
			    	public void run() {
			    		//checkUpdate();
			    		Intent intent = new Intent(firstActivity.this, PBBSplash.class);
						finish();
						startActivity(intent);
			    	}
			    }, 2000);

		    }		
		}
	}
	private void alert_no_gps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Yout GPS seems to be disabled.  You need GPS to run some parts of this application. Do you want to enable it now?")
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        firstActivity.this.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 3);
        				new Handler().postDelayed(new Runnable(){
        			    	public void run() {
        			    		//checkUpdate();
        			    		Intent intent = new Intent(firstActivity.this, PBBSplash.class);
        						finish();
        						startActivity(intent);
        			    	}
        			    }, 2000);
                    }
                })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
        				new Handler().postDelayed(new Runnable(){
        			    	public void run() {
        			    		//checkUpdate();
        			    		Intent intent = new Intent(firstActivity.this, PBBSplash.class);
        						finish();
        						startActivity(intent);
        			    	}
        			    }, 2000);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
	
	private void downloadPlantLists() {
		
		HelperFunctionCalls helper = new HelperFunctionCalls();
		helper.changedSharedPreferenceTree(firstActivity.this);
		
		HelperSharedPreference hPref = new HelperSharedPreference(firstActivity.this);
		hPref.setPreferencesBoolean("firstDownloadTreeList", false);

		HelperRefreshPlantLists refreshList = new HelperRefreshPlantLists(firstActivity.this);
		refreshList.execute();
		
	 	Intent intent = new Intent(firstActivity.this, firstActivity.class);
		startActivity(intent);
		finish();
	}
		
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	     if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	 //preventing default implementation previous to android.os.Build.VERSION_CODES.ECLAIR
	    	 return true;
	     }
	     return super.onKeyDown(keyCode, event);    
	}
	
	public void checkUpdate() {
		
		try {
			URL urls = new URL(getString(R.string.check_version_number) + "?version=" + mGetCurrentVersion);
			Object getContent = urls.getContent();
			
			Log.i("K", "getContent : " + getContent);
			HttpURLConnection conn = (HttpURLConnection)urls.openConnection();
			conn.connect();
			
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
            
            /* Read bytes to the Buffer until
             * there is nothing more to read(-1). */
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current = 0;
            while((current = bis.read()) != -1){
                    baf.append((byte)current);
            }

            /* Convert the Bytes read to a String. */
            String myString = new String(baf.toByteArray());
            
            if(myString.equals("NEEDUPDATES")) {
            	new AlertDialog.Builder(this)
    	    	.setTitle(getString(R.string.Upgrade_the_application_Title))
    	    	.setMessage(getString(R.string.Upgrade_the_application_Text))
    	    	.setPositiveButton(getString(R.string.Updates_Text), new DialogInterface.OnClickListener() {
    				
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					// TODO Auto-generated method stub
    					Intent intent = new Intent(Intent.ACTION_VIEW);
    					intent.setData(Uri.parse("market://details?id=edu.ucla.cens.budburstmobile"));
    					startActivity(intent);
    				}
    			})
    			.setNegativeButton(getString(R.string.Skip), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						moveToMainPage();
					}
				})
    			.show();
            }
            else {
            	moveToMainPage();
            }
		}
		catch(Exception e) {}
	}
}
