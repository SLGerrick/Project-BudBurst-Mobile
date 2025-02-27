package edu.ucla.cens.budburstmobile.myplants;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.database.OneTimeDBHelper;
import edu.ucla.cens.budburstmobile.database.SyncDBHelper;
import edu.ucla.cens.budburstmobile.floracaching.FloracacheDetail;
import edu.ucla.cens.budburstmobile.helper.HelperSharedPreference;
import edu.ucla.cens.budburstmobile.helper.HelperValues;
import edu.ucla.cens.budburstmobile.mapview.SitesOverlay;
import edu.ucla.cens.budburstmobile.utils.PBBItems;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PBBChangeMyPosition extends MapActivity {

	private PBBItems pbbItem;
	private HelperSharedPreference mPref;
	private static GpsListener gpsListener;
	private LocationManager locManager = null;
	private MapView mMapView = null;
	private MapController mapCon = null;
	//private MyLocOverlay mOver = null;
	private MyLocationOverlay mOver;
	private SitesOverlay sOverlay = null;
	private double mLatitude = 0.0;
	private double mLongitude = 0.0;
	private double mTargetLatitude = 0.0;
	private double mTargetLongitude = 0.0;
	private float mAccuracy = 0;
	private TextView mylocInfo;
	private boolean first_myLoc = true;
	private int mPreviousActivity;
	private PBBItems pbbFloracache;
	private int floraImageId;
	private Button mapBtn;
	private Button gpsBtn;
	private TextView mTitle;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.floracache_map);
	    
	    mMapView = (MapView)findViewById(R.id.map);
	    
	    mylocInfo = (TextView) findViewById(R.id.myloc_accuracy);
	    mTitle = (TextView) findViewById(R.id.title);
	    mTitle.setText("Refine Location");
	    
	    //mMapView.invalidate();
	    mPref = new HelperSharedPreference(this);
	    mPref.setPreferencesString("accuracy2", "100");
	    
	    mapBtn = (Button) findViewById(R.id.mapBtn);
	    gpsBtn = (Button) findViewById(R.id.gpsBtn);
	    
	    mapBtn.setOnClickListener(markerBtnListener);
		gpsBtn.setOnClickListener(gpsBtnListener);
	   
	   
	    Intent p_intent = getIntent();
		mPreviousActivity = p_intent.getExtras().getInt("from");
		
		 //get items for floracache
		if(mPreviousActivity==HelperValues.FROM_FLORACACHE){
		    Bundle bundle = getIntent().getExtras();
			pbbItem = bundle.getParcelable("pbbItem");
			mTargetLatitude = pbbItem.getLatitude();
			mTargetLongitude = pbbItem.getLongitude();
			floraImageId = p_intent.getExtras().getInt("image_id");
			pbbFloracache = pbbItem;
		}
	    
	    /*
	     * Add Mylocation Overlay
	     */
	    //mOver = new MyLocOverlay(MyLocation.this, mMapView);
	    mOver = new MyLocationOverlay(PBBChangeMyPosition.this, mMapView);
	    mOver.enableMyLocation();
	    mMapView.getOverlays().add(mOver);
	    mMapView.setSatellite(false);
	    /*
	     * Add ItemizedOverlay Overlay
	     */
	    Drawable marker = getResources().getDrawable(R.drawable.marker);
	    marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
	    sOverlay = new SitesOverlay(PBBChangeMyPosition.this, marker);	    
	    mMapView.getOverlays().add(sOverlay);
	    
	    
	   
	    mLatitude = Double.parseDouble(mPref.getPreferenceString("latitude", "0.0"));
	    mLongitude = Double.parseDouble(mPref.getPreferenceString("longitude", "0.0"));
	    
	    mapCon = mMapView.getController();
	    GeoPoint geoPoint = getPoint(mLatitude, mLongitude);
	    mapCon.animateTo(geoPoint);
	    mapCon.setZoom(19);
	   
	    gpsListener = new GpsListener();
	    locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000, 5, gpsListener);
	    
	    showButtonOnMap();
		
	     
	    // TODO Auto-generated method stub
	}
	
	private OnClickListener gpsBtnListener = new OnClickListener() {

		public void onClick(View v) {

			// If they choose to use the GPS coordinates
			mPref.setPreferencesString("latitude", Double.toString(mLatitude));
			mPref.setPreferencesString("longitude", Double.toString(mLongitude));
			mPref.setPreferencesString("accuracy", Float.toHexString(mAccuracy));
			mPref.setPreferencesBoolean("changedLoc", true);
			locManager.removeUpdates(gpsListener);
			mOver.disableMyLocation();
			finish();
		}
	};
	
	private OnClickListener markerBtnListener = new OnClickListener() {

		public void onClick(View v) {

			// If they choose to use the map's marker coordinates
			
			if(mPreviousActivity==HelperValues.FROM_FLORACACHE){
				double mapLongitude = sOverlay.getLongitude();
				double mapLatitude = sOverlay.getLatitude();
				mPref.setPreferencesString("accuracy2", "0.0");
				float dist[] = new float[1];
				dist[0]=-1;
				Location.distanceBetween(mapLatitude, mapLongitude, mTargetLatitude, mTargetLongitude, dist);
				float distLocs[] = new float[1];
				Location.distanceBetween(mapLatitude, mapLongitude, mTargetLatitude, mTargetLongitude, dist);
				Location.distanceBetween(mapLatitude, mapLongitude, mLatitude, mLongitude, distLocs);
				double mDistance = dist[0];
				double mapToGpsDistance = distLocs[0];
				
				if(mDistance < 15.0 && mDistance>=0 && mapToGpsDistance < 33.0) {
					Toast.makeText(PBBChangeMyPosition.this, "close enough! Dist: " + String.format("%5.2f", dist[0] * 3.2808399) + "ft", Toast.LENGTH_SHORT).show();	
					Intent intent2 = new Intent(PBBChangeMyPosition.this, FloracacheDetail.class);
					PBBItems pbbItem = new PBBItems();							
					intent2.putExtra("pbbItem", pbbFloracache);
					intent2.putExtra("image_id", floraImageId);
					PBBChangeMyPosition.this.startActivity(intent2);
					
					locManager.removeUpdates(gpsListener);
					mOver.disableMyLocation();
					finish();
				}
				
				else{
					if(mapToGpsDistance >= 33.0){
						Toast.makeText(PBBChangeMyPosition.this, 
								"Marker location not in range", 
								Toast.LENGTH_SHORT).show();	
					}
					else if(mDistance >= 15.0){
						Toast.makeText(PBBChangeMyPosition.this, 
								"Not close enough...", 
								Toast.LENGTH_SHORT).show();	
						locManager.removeUpdates(gpsListener);
						mOver.disableMyLocation();
						finish();
					}
				}				
			}
			else{
				mPref.setPreferencesString("accuracy", Float.toHexString(mAccuracy));
				locManager.removeUpdates(gpsListener);
				mOver.disableMyLocation();
				finish();
			}				
		}
			
	};

	
	private void showButtonOnMap() {
		
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		
		MapView.LayoutParams screenLP;

		// Zoom out
	    screenLP = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
											MapView.LayoutParams.WRAP_CONTENT,
											width-50, 10,
											MapView.LayoutParams.TOP_LEFT);

	    Button mapBtnZoomOut = new Button(getApplicationContext());
	    mapBtnZoomOut.setBackgroundDrawable(getResources().getDrawable(R.drawable.menu_zoom_in));

	    mMapView.addView(mapBtnZoomOut, screenLP);
		
	    screenLP = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
	    									MapView.LayoutParams.WRAP_CONTENT,
	    									width-50, 55,
	    									MapView.LayoutParams.TOP_LEFT);
	    // Zoom in
	    Button mapBtnZoomIn = new Button(getApplicationContext());
	    mapBtnZoomIn.setBackgroundDrawable(getResources().getDrawable(R.drawable.menu_zoom_out));

	    mMapView.addView(mapBtnZoomIn, screenLP);
	    
	    mapBtnZoomIn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mapCon.zoomIn();
			}
		});

	    mapBtnZoomOut.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mapCon.zoomOut();
			}
		});
	}
	
	private class GpsListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location loc) {
			if(loc != null) {
				mLatitude = loc.getLatitude();
				mLongitude = loc.getLongitude();
			
				
				
				
				if(mPreviousActivity==HelperValues.FROM_FLORACACHE){
					mylocInfo.setText("Adjust your location by touching the screen and placing a marker where you are.");
					mAccuracy=33;
					loc.setAccuracy(33);
					mPref.setPreferencesString("accuracy2", "100"); 
				}
				else{
					mAccuracy = loc.getAccuracy();
					mylocInfo.setText("Accuracy : " + mAccuracy + "\u00b1m");
				}
				
				GeoPoint geoPoint = getPoint(mLatitude, mLongitude);				
				mapCon.animateTo(geoPoint);
				mOver.onLocationChanged(loc);
				
			}
			// TODO Auto-generated method stub
		}
		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0), (int)(lon*1000000.0)));
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		locManager.removeUpdates(gpsListener);
		mOver.disableMyLocation();
	}
	
	// or when user press back button
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			
			new AlertDialog.Builder(PBBChangeMyPosition.this)
	   		.setTitle(getString(R.string.Message_Save_GPS))
	   		.setPositiveButton(getString(R.string.Button_GPS), new DialogInterface.OnClickListener() {
	   			public void onClick(DialogInterface dialog, int whichButton) {
	   				mPref.setPreferencesString("latitude", Double.toString(mLatitude));
	   				mPref.setPreferencesString("longitude", Double.toString(mLongitude));
	   				mPref.setPreferencesString("accuracy", Float.toHexString(mAccuracy));
	   				mPref.setPreferencesBoolean("changedLoc", true);
	   				locManager.removeUpdates(gpsListener);
	   				mOver.disableMyLocation();
	   				finish();
	   			}
	   		})
	   		.setNegativeButton(getString(R.string.Button_Cancel), new DialogInterface.OnClickListener() {
	   			public void onClick(DialogInterface dialog, int whichButton) {
	   				locManager.removeUpdates(gpsListener);
	   				mOver.disableMyLocation();		
	   				mPref.setPreferencesBoolean("changedLoc", true);
	   				finish();
	   			}
	   		})
	   		.setNeutralButton(getString(R.string.Button_Marker), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(mPreviousActivity==HelperValues.FROM_FLORACACHE){
						double mapLongitude = sOverlay.getLongitude();
						double mapLatitude = sOverlay.getLatitude();
						
						mPref.setPreferencesString("accuracy2", "0.0");
						
						float dist[] = new float[1];
						dist[0]=-1;
						Location.distanceBetween(mapLatitude, mapLongitude, mTargetLatitude, mTargetLongitude, dist);
						float distLocs[] = new float[1];
						Location.distanceBetween(mapLatitude, mapLongitude, mTargetLatitude, mTargetLongitude, dist);
						Location.distanceBetween(mapLatitude, mapLongitude, mLatitude, mLongitude, distLocs);
						double mDistance = dist[0];
						double mapToGpsDistance = distLocs[0];
						
						if(mDistance < 15.0 && mDistance>=0 && mapToGpsDistance < 33.0) {
							Toast.makeText(PBBChangeMyPosition.this, "close enough! Dist: " + String.format("%5.2f", dist[0] * 3.2808399) + "ft", Toast.LENGTH_SHORT).show();	
							Intent intent2 = new Intent(PBBChangeMyPosition.this, FloracacheDetail.class);
							PBBItems pbbItem = new PBBItems();							
							intent2.putExtra("pbbItem", pbbFloracache);
							intent2.putExtra("image_id", floraImageId);
							PBBChangeMyPosition.this.startActivity(intent2);			
						
						}
						
						else{
							if(mapToGpsDistance >= 33.0){
								Toast.makeText(PBBChangeMyPosition.this, 
										"Marker location not in range", 
										Toast.LENGTH_SHORT).show();	
							}
							else if(mDistance >= 15.0){
							Toast.makeText(PBBChangeMyPosition.this, 
									"Not close enough...", 
									Toast.LENGTH_SHORT).show();	
							}
						}
						
						locManager.removeUpdates(gpsListener);
		   				mOver.disableMyLocation();
						
						
						
						
						
						
						
				//		mPref.setPreferencesString("latitude2", Double.toString(sOverlay.getLatitude()));
		   		//		mPref.setPreferencesString("longitude2", Double.toString(sOverlay.getLongitude()));
		   		//		mPref.setPreferencesBoolean("changedLoc", true);
					}
					else{
			//			mPref.setPreferencesString("latitude", Double.toString(mLatitude));
		   	//			mPref.setPreferencesString("longitude", Double.toString(mLongitude));
					}
					mPref.setPreferencesString("accuracy", Float.toHexString(mAccuracy));
	   				
	   				finish();
				}
			})
	   		.show();			
		}
		return false;
	}
	
	
		/*
	 * Menu option(non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, getString(R.string.Menu_help)).setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, 2, 0, getString(R.string.Menu_Satellite)).setIcon(android.R.drawable.ic_menu_mapmode);
			
		return true;
	}
	
	/*
	 * Menu option selection handling(non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item){

		switch(item.getItemId()){
			case 1:
				Toast.makeText(PBBChangeMyPosition.this, getString(R.string.Alert_comingSoon), Toast.LENGTH_SHORT).show();
				return true;
			case 2:
				mMapView.setSatellite(!mMapView.isSatellite());
				return true;
		}
		return false;
	}

}
