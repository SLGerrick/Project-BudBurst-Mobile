package edu.ucla.cens.budburstmobile.floracaching;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.database.OneTimeDBHelper;
import edu.ucla.cens.budburstmobile.helper.HelperPlantItem;
import edu.ucla.cens.budburstmobile.helper.HelperSharedPreference;
import edu.ucla.cens.budburstmobile.helper.HelperValues;
import edu.ucla.cens.budburstmobile.lists.ListDetail;
import edu.ucla.cens.budburstmobile.mapview.BalloonItemizedOverlay;
import edu.ucla.cens.budburstmobile.mapview.PopupPanel;
import edu.ucla.cens.budburstmobile.mapview.SpeciesDetailMap;
import edu.ucla.cens.budburstmobile.mapview.SpeciesOverlayItem;
import edu.ucla.cens.budburstmobile.myplants.GetPhenophaseObserver;
import edu.ucla.cens.budburstmobile.myplants.GetPhenophaseShared;
import edu.ucla.cens.budburstmobile.myplants.PBBChangeMyPosition;
import edu.ucla.cens.budburstmobile.onetime.OneTimePhenophase;
import edu.ucla.cens.budburstmobile.utils.PBBItems;
import edu.ucla.cens.budburstmobile.utils.QuickCapture;

public class FloraCacheOverlay extends BalloonItemizedOverlay<SpeciesOverlayItem>{
	private Context mContext;
	private ArrayList<FloracacheItem> mPlantList = new ArrayList<FloracacheItem>();
	private ArrayList<SpeciesOverlayItem> mSItem = new ArrayList<SpeciesOverlayItem>();
	private	PopupPanel mPanel;
	private Drawable mMarker;
	private MapView mMap = null;
	private SpeciesOverlayItem speciesItem;
	private int mIndex;
	private int mImageID;
	
	private double mDistance;
	private HelperSharedPreference mPref;	
	private double mLatitude 		= 0.0;
	private double mLongitude 		= 0.0;
	
	public FloraCacheOverlay(MapView mapView, Drawable marker, ArrayList<FloracacheItem> plantList) {
		super(boundCenter(marker), mapView);
		
		mContext = mapView.getContext();
		mPlantList = plantList;
		mPref = new HelperSharedPreference(mContext);

		// read data from the table
		for(int i = 0 ; i < mPlantList.size() ; i++) {
			GeoPoint geoPoint = getPoint(mPlantList.get(i).getLatitude(), 
					mPlantList.get(i).getLongitude());

			mSItem.add(new SpeciesOverlayItem(geoPoint, 
					mPlantList.get(i).getUserSpeciesID(), 
					mPlantList.get(i).getCommonName(),
					mPlantList.get(i).getUserName(), 
					mPlantList.get(i).getFloracacheNotes(), 
					"",
					getMarker(R.drawable.full_marker), 
					mMarker, 
					mPlantList.get(i).getUserSpeciesCategoryID(),
					true));
		}
	
		populate();
	}
	
	private Drawable getMarker(int resource) {
		Drawable marker = mContext.getResources().getDrawable(resource);
		marker.setBounds(-marker.getIntrinsicWidth() / 2, marker.getIntrinsicHeight(), marker.getIntrinsicWidth() / 2, 0);
		boundCenter(marker);
		return(marker);
	}

	@Override
	protected SpeciesOverlayItem createItem(int i) {
		return(mSItem.get(i));
	}

	private Bitmap LoadImageFromWebOperation(String url) {
		try {
			URL imageURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)imageURL.openConnection();
			conn.setDoInput(true);
			conn.connect();
			
			InputStream is = conn.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			
			return bitmap; 
			
		}
		catch(Exception e) {
			return null;
		}
	}
	
	private GeoPoint getPoint(double lat, double lon) {
		return(new GeoPoint((int)(lat*1000000.0), (int)(lon*1000000.0)));
	}
		
	@Override
	protected boolean onBalloonTap(int index) {
		// Observed species are from PlantList and Shared Plant
		
		mIndex = index;
		
		
		// calculate distance from mylocation to the marker
		double latitude = mPlantList.get(index).getLatitude();
		double longitude = mPlantList.get(index).getLongitude();
		
		HelperSharedPreference hPref = new HelperSharedPreference(mContext);
		double curLat = Double.parseDouble(hPref.getPreferenceString("latitude", "0.0"));
		double curLon = Double.parseDouble(hPref.getPreferenceString("longitude", "0.0"));
		
		float[] distResult = new float[1];
		
		Location.distanceBetween(latitude, longitude, curLat, curLon, distResult);

		// set to 10 meters. but GPS varies...so set to 12.
		if(distResult[0] <= 15.0) {
			showDialog();
		}
		else {
			if(distResult[0] <= 33.0) {
				//TODO add the pbbchangelocation here
				pbbchangelocation(latitude, longitude);
		//		showDialog();
			}
			
			
			else
				Toast.makeText(mContext, "Not close enough. Dist: " + String.format("%5.2f", distResult[0] * 3.2808399) + "ft", Toast.LENGTH_SHORT).show();	
		}
			
		hideBalloon();
		
		return true;
	}
	
	private void pbbchangelocation(double mTargetLatitude, double mTargetLongitude){
		//Change location activity and floracache information being sent
		
		AlertDialog.Builder alert = new AlertDialog.Builder( mContext );
		
		
		new AlertDialog.Builder( mContext )
   		.setTitle("So Close!")
   		.setMessage("You are not close enough. Would you like to refine your location using a touch-map?")
   		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
   			public void onClick(DialogInterface dialog, int whichButton) {
				//refine location with map
   				Intent intentChange = new Intent(mContext, PBBChangeMyPosition.class);
				intentChange.putExtra("from", HelperValues.FROM_FLORACACHE);
				intentChange.putExtra("targetLongitude", mPlantList.get(mIndex).getLongitude());
				intentChange.putExtra("targetLatitude", mPlantList.get(mIndex).getLatitude());
				PBBItems pbbItemChange = new PBBItems();
				pbbItemChange.setCommonName(mPlantList.get(mIndex).getCommonName());
				pbbItemChange.setScienceName(mPlantList.get(mIndex).getScienceName());
				pbbItemChange.setSpeciesID(mPlantList.get(mIndex).getUserSpeciesID());
				pbbItemChange.setProtocolID(mPlantList.get(mIndex).getProtocolID());
				pbbItemChange.setCategory(mPlantList.get(mIndex).getUserSpeciesCategoryID());
				pbbItemChange.setIsFloracache(HelperValues.IS_FLORACACHE_YES); // set floracacheID to easy value
				pbbItemChange.setFloracacheID(mPlantList.get(mIndex).getFloracacheID());
				pbbItemChange.setLatitude(mPlantList.get(mIndex).getLatitude());
				pbbItemChange.setLongitude(mPlantList.get(mIndex).getLongitude());
				intentChange.putExtra("pbbItem", pbbItemChange);
				mContext.startActivity(intentChange);
   			}
   		})
   		.setNegativeButton("No", new DialogInterface.OnClickListener() {
   			public void onClick(DialogInterface dialog, int whichButton) {
   				//don't refine location				   				
   				
   			}
   		}).show();
		
//		boolean changedLoc = mPref.getPreferenceBoolean("changedLoc");
// 		Toast.makeText(mContext, "Not close enough. Dist: ", Toast.LENGTH_SHORT).show();	
//		while(!changedLoc){
//			changedLoc = mPref.getPreferenceBoolean("changedLoc");
//		}
		
/*		double mapLatitude = Double.parseDouble(mPref.getPreferenceString("latitude2", "0.0"));
		double mapLongitude = Double.parseDouble(mPref.getPreferenceString("longitude2", "0.0"));
		double gpsLatitude = Double.parseDouble(mPref.getPreferenceString("latitude", "0.0"));
		double gpsLongitude = Double.parseDouble(mPref.getPreferenceString("longitude", "0.0"));
//		mLongitude = mapLongitude;
//		mLatitude = mapLatitude;
//		mAccuracy = mPref.getPreferencesString("accuracy", Float.toHexString(mAccuracy));
		
		float dist[] = new float[1];
		dist[0]=-1;
		Location.distanceBetween(mapLatitude, mapLongitude, mTargetLatitude, mTargetLongitude, dist);
		float distLocs[] = new float[1];
		Location.distanceBetween(mapLatitude, mapLongitude, mTargetLatitude, mTargetLongitude, dist);
		Location.distanceBetween(mapLatitude, mapLongitude, gpsLatitude, gpsLongitude, distLocs);
		mDistance = dist[0];
		double mapToGpsDistance = distLocs[0];
		if(mDistance < 15.0 && mDistance>=0 && mapToGpsDistance < 100) {
			Toast.makeText(mContext, "close enough! Dist: " + String.format("%5.2f", dist[0] * 3.2808399) + "ft", Toast.LENGTH_SHORT).show();	
			
			Intent intent2 = new Intent(mContext, FloracacheDetail.class);
			PBBItems pbbItem = new PBBItems();
			pbbItem.setCommonName(mPlantList.get(mIndex).getCommonName());
			pbbItem.setScienceName(mPlantList.get(mIndex).getScienceName());
			pbbItem.setSpeciesID(mPlantList.get(mIndex).getUserSpeciesID());
			pbbItem.setProtocolID(mPlantList.get(mIndex).getProtocolID());
			pbbItem.setCategory(mPlantList.get(mIndex).getUserSpeciesCategoryID());
			pbbItem.setIsFloracache(HelperValues.IS_FLORACACHE_YES); // set floracacheID to easy value
			pbbItem.setFloracacheID(mPlantList.get(mIndex).getFloracacheID());
			pbbItem.setLatitude(mPlantList.get(mIndex).getLatitude());
			pbbItem.setLongitude(mPlantList.get(mIndex).getLongitude());
			
			intent2.putExtra("pbbItem", pbbItem);
			intent2.putExtra("image_id", mImageID);
			mContext.startActivity(intent2);			
		
		}
		
		else{
			Toast.makeText(mContext, 
					"Not close enough...", 
					Toast.LENGTH_SHORT).show();	
		}
		*/
	}
	
	
	private void showDialog() {
		
		/*
		 * Intent intent = new Intent(FloraCacheMidLevel.this, FloracacheDetail.class);
			PBBItems pbbItem = new PBBItems();
			pbbItem.setCommonName(mListArr.get(mIndex).getCommonName());
			pbbItem.setScienceName(mListArr.get(mIndex).getSpeciesName());
			pbbItem.setSpeciesID(mListArr.get(mIndex).getSpeciesID());
			pbbItem.setProtocolID(mListArr.get(mIndex).getProtocolID());
			pbbItem.setCategory(mListArr.get(mIndex).getCategory());
			pbbItem.setFloracacheID(mListArr.get(mIndex).getFloracacheID());
			pbbItem.setIsFloracache(HelperValues.IS_FLORACACHE_YES);
			pbbItem.setSpeciesImageID(mImageID);
			
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("image_id", mImageID);
			startActivity(intent);
		 */
		
		if(mPlantList.get(mIndex).getUserSpeciesCategoryID() != HelperValues.LOCAL_BUDBURST_LIST) {
			OneTimeDBHelper oDBH = new OneTimeDBHelper(mContext);
			mImageID = oDBH.getImageID(mContext, mPlantList.get(mIndex).getScienceName(), mPlantList.get(mIndex).getUserSpeciesCategoryID());
		}
		
		Intent intent = new Intent(mContext, FloracacheDetail.class);
		PBBItems pbbItem = new PBBItems();
		pbbItem.setCommonName(mPlantList.get(mIndex).getCommonName());
		pbbItem.setScienceName(mPlantList.get(mIndex).getScienceName());
		pbbItem.setSpeciesID(mPlantList.get(mIndex).getUserSpeciesID());
		pbbItem.setProtocolID(mPlantList.get(mIndex).getProtocolID());
		pbbItem.setCategory(mPlantList.get(mIndex).getUserSpeciesCategoryID());
		pbbItem.setIsFloracache(HelperValues.IS_FLORACACHE_YES); // set floracacheID to easy value
		pbbItem.setFloracacheID(mPlantList.get(mIndex).getFloracacheID());
		pbbItem.setLatitude(mPlantList.get(mIndex).getLatitude());
		pbbItem.setLongitude(mPlantList.get(mIndex).getLongitude());
		
		intent.putExtra("pbbItem", pbbItem);
		intent.putExtra("image_id", mImageID);
		intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
		
		mContext.startActivity(intent);
	}


	@Override
	public int size() {
		return(mSItem.size());
	}
	
	
	void toggleHeart() {
		SpeciesOverlayItem focus=getFocus();
		
		if (focus!=null) {
			focus.toggleHeart();
		}
		
		mMap.invalidate();
	}	
}
