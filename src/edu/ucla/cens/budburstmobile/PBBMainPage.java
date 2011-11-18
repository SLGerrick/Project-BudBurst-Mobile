package edu.ucla.cens.budburstmobile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import edu.ucla.cens.budburstmobile.database.OneTimeDBHelper;
import edu.ucla.cens.budburstmobile.database.StaticDBHelper;
import edu.ucla.cens.budburstmobile.database.SyncDBHelper;
import edu.ucla.cens.budburstmobile.utils.PBBItems;
import edu.ucla.cens.budburstmobile.floracaching.FloraCacheEasyLevel;
import edu.ucla.cens.budburstmobile.floracaching.FloraCacheMain;
import edu.ucla.cens.budburstmobile.helper.HelperBackgroundService;
import edu.ucla.cens.budburstmobile.helper.HelperShowAll;
import edu.ucla.cens.budburstmobile.helper.HelperFunctionCalls;
import edu.ucla.cens.budburstmobile.helper.HelperSettings;
import edu.ucla.cens.budburstmobile.helper.HelperSharedPreference;
import edu.ucla.cens.budburstmobile.helper.HelperValues;
import edu.ucla.cens.budburstmobile.lists.ListMain;
import edu.ucla.cens.budburstmobile.lists.ListUserDefinedSpecies;
import edu.ucla.cens.budburstmobile.mapview.MapViewMain;
import edu.ucla.cens.budburstmobile.myplants.PBBPlantList;
import edu.ucla.cens.budburstmobile.onetime.OneTimeMainPage;
import edu.ucla.cens.budburstmobile.utils.PBBItems;
import edu.ucla.cens.budburstmobile.utils.QuickCapture;
import edu.ucla.cens.budburstmobile.weeklyplant.WeeklyPlant;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PBBMainPage extends Activity {
	
	private ImageButton myPlantBtn = null;
	private Button oneTimeBtn = null;
	private Button myResultBtn = null;
	private Button singleReportBtn = null;
	private Button syncBtn = null;
	private Button infoBtn = null;
	private Button floraBtn = null;
	private TextView mUserInfo = null;
	private HelperSharedPreference mPref;
	private PBBItems pbbItem;
	private String mUsername;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.mainpage);
	    
	    mPref = new HelperSharedPreference(this);
	    
	    mUsername = mPref.getPreferenceString("Username", "");
	    if(mUsername.equals("test10")){
	    	mUsername = "Preview";
	    }
	    
	    mPref.setPreferencesBoolean("new", false);
	    mPref.setPreferencesBoolean("visited", false);
	    
		LinearLayout ll = (LinearLayout) findViewById(R.id.header);
		
		ll.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PBBMainPage.this, firstActivity.class);
				finish();
				startActivity(intent);
			}
		});
		

		// check sync
		int synced = checkSync();
	    
	    // if the app has not been synced.
	    if(synced == SyncDBHelper.SYNCED_NO) {
	    	LinearLayout sync_layout = (LinearLayout)findViewById(R.id.my_plant_sync);
	    	LinearLayout unsync_layout = (LinearLayout)findViewById(R.id.my_plant_unsync);
	    	
	    	sync_layout.setVisibility(View.GONE);
	    	unsync_layout.setVisibility(View.VISIBLE);
	    	
	    	ImageButton unsync_Btn = (ImageButton) findViewById(R.id.unsync_my_plant);
	    	unsync_Btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(PBBMainPage.this, PBBPlantList.class);
					startActivity(intent);
				}
			});
	    }
	    
	    // myBudburst button
	    myPlantBtn = (ImageButton)findViewById(R.id.my_plant);
	    myPlantBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PBBMainPage.this, PBBPlantList.class);
				startActivity(intent);
			}
	    });
	    
	    // local list button
	    myResultBtn = (Button) findViewById(R.id.myresults);
	    myResultBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Toast.makeText(MainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
			//	startActivity(new Intent(PBBMainPage.this, ListMain.class));
				// TODO Auto-generated method stub
				Intent intent = new Intent(PBBMainPage.this, OneTimeMainPage.class);
				pbbItem = new PBBItems();
				pbbItem.setLocalImageName("");
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
				startActivity(intent);
			}
	    });
	    
	    // plant maps button
	    singleReportBtn = (Button)findViewById(R.id.map);
	    singleReportBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Toast.makeText(MainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				new AlertDialog.Builder(PBBMainPage.this)
				.setTitle(getString(R.string.Menu_addQCPlant))
				.setMessage(getString(R.string.Start_Shared_Plant))
				.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						/*
						 * Move to QuickCapture
						 */
						Intent intent = new Intent(PBBMainPage.this, QuickCapture.class);
						pbbItem = new PBBItems();
						intent.putExtra("pbbItem", pbbItem);
						intent.putExtra("from", HelperValues.FROM_QUICK_CAPTURE);
						startActivity(intent);
					}
				})
				.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(PBBMainPage.this, OneTimeMainPage.class);
						pbbItem = new PBBItems();
						pbbItem.setLocalImageName("");
						intent.putExtra("pbbItem", pbbItem);
						intent.putExtra("from", HelperValues.FROM_QUICK_CAPTURE);
						startActivity(intent);
					}
				})
				.setNegativeButton(getString(R.string.Button_Cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				})
				.show();

			}
	    	
	    });
	    
	    // Sync button
	    syncBtn = (Button)findViewById(R.id.news);
	    syncBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(PBBMainPage.this, PBBSync.class);
				intent.putExtra("sync_instantly", true);
				intent.putExtra("from", HelperValues.FROM_MAIN_PAGE);
				startActivity(intent);
				finish();
			}
	    	
	    });
	    
	    // info plant button
	    infoBtn = (Button)findViewById(R.id.info);
	    infoBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Toast.makeText(MainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(PBBMainPage.this, HelperShowAll.class);
				intent.putExtra("from", HelperValues.FROM_MAIN_PAGE); 
				startActivity(intent);

			}
	    	
	    });
	    
	    // floracaching button
	    floraBtn = (Button)findViewById(R.id.flora);
	    floraBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Toast.makeText(PBBMainPage.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				
				
				HelperSharedPreference hPref = new HelperSharedPreference(PBBMainPage.this);
				if(hPref.getPreferenceBoolean("floracache")) {
					Intent intent = new Intent(PBBMainPage.this, FloraCacheMain.class);
					startActivity(intent);
				}
				else {
					Toast.makeText(PBBMainPage.this, getString(R.string.go_to_settings_page), Toast.LENGTH_SHORT).show();
				}
				
			}
		});
	    
	    mUserInfo = (TextView) findViewById(R.id.user_info);
	    mUserInfo.setText("Hi, " + mUsername);
	    
	    // TODO Auto-generated method stub
	}
	
    // or when user press back button
	// when you hold the button for 3 sec, the app will be exited
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == event.KEYCODE_BACK) {

			new AlertDialog.Builder(PBBMainPage.this)
			.setTitle(getString(R.string.Exit_Application_title))
			.setIcon(R.drawable.pbb_icon_small)
			.setMessage(getString(R.string.Exit_Application))
			.setPositiveButton(getString(R.string.Button_yes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
					Intent service = new Intent(PBBMainPage.this, HelperBackgroundService.class);
				    stopService(service);
				    
					finish();
				}
			})
			.setNegativeButton(getString(R.string.Button_no), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					// nothing to do
				}
			})
			.show();
		}
		
		return false;
	}
	
	public int checkSync() {
		SyncDBHelper syncDB = new SyncDBHelper(PBBMainPage.this);
		OneTimeDBHelper onetime = new OneTimeDBHelper(PBBMainPage.this);
		
		SQLiteDatabase ot = onetime.getReadableDatabase();
		SQLiteDatabase sync = syncDB.getReadableDatabase();
		
		int synced = SyncDBHelper.SYNCED_YES;
		
		Cursor syncCheck = ot.rawQuery("SELECT synced FROM oneTimeObservation", null);
		while(syncCheck.moveToNext()) {
			if(syncCheck.getInt(0) == SyncDBHelper.SYNCED_NO) {
				synced = SyncDBHelper.SYNCED_NO;
			}
		}
		syncCheck.close();
		
		syncCheck = ot.rawQuery("SELECT synced FROM oneTimePlant", null);
		while(syncCheck.moveToNext()) {
			if(syncCheck.getInt(0) == SyncDBHelper.SYNCED_NO) {
				synced = SyncDBHelper.SYNCED_NO;
			}
		}
		syncCheck.close();
	
		// check if there is any unsynced data from my_observation and onetimeob tables.
		syncCheck = sync.rawQuery("SELECT synced FROM my_plants", null);
		while(syncCheck.moveToNext()) {
			if(syncCheck.getInt(0) == SyncDBHelper.SYNCED_NO) {
				synced = SyncDBHelper.SYNCED_NO;
			}
		}
		syncCheck.close();
		
		sync.close();
		ot.close();
		
		return synced;
	}
	
	/*
	 * Menu option(non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
//		menu.add(0, 1, 0, getString(R.string.Menu_help)).setIcon(android.R.drawable.ic_menu_help);
//		menu.add(0, 2, 0, getString(R.string.Menu_sync)).setIcon(R.drawable.ic_menu_refresh);
		menu.add(0, 3, 0, getString(R.string.Menu_about)).setIcon(android.R.drawable.ic_menu_info_details);
		//menu.add(0, 4, 0, getString(R.string.Menu_logout)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, 4, 0, getString(R.string.Menu_settings)).setIcon(android.R.drawable.ic_menu_preferences);
			
		return true;
	}
	
	/*
	 * Menu option selection handling(non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item){

		Intent intent;
		switch(item.getItemId()){
			case 1:
				intent = new Intent(PBBMainPage.this, PBBHelpPage.class);
				intent.putExtra("from", HelperValues.FROM_MAIN_PAGE);
				startActivity(intent);
				return true;
			case 2:
				intent = new Intent(PBBMainPage.this, PBBSync.class);
				intent.putExtra("sync_instantly", true);
				intent.putExtra("from", HelperValues.FROM_MAIN_PAGE);
				startActivity(intent);
				finish();
				return true;				
			case 3:
				intent = new Intent(PBBMainPage.this, PBBHelpPage.class);
				intent.putExtra("from", HelperValues.FROM_ABOUT);
				startActivity(intent);
				return true;
			case 4:
				intent = new Intent(PBBMainPage.this, HelperSettings.class);
				intent.putExtra("from", HelperValues.FROM_MAIN_PAGE);
				startActivity(intent);
				finish();
				return true;
		}
		return false;
	}
}

