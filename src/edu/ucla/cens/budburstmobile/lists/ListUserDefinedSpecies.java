package edu.ucla.cens.budburstmobile.lists;

//List for User Defined Species plants

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.adapter.MyListAdapterWithIndex;
import edu.ucla.cens.budburstmobile.database.OneTimeDBHelper;
import edu.ucla.cens.budburstmobile.database.SyncDBHelper;
import edu.ucla.cens.budburstmobile.helper.HelperFunctionCalls;
import edu.ucla.cens.budburstmobile.helper.HelperJSONParser;
import edu.ucla.cens.budburstmobile.helper.HelperPlantItem;
import edu.ucla.cens.budburstmobile.helper.HelperValues;
import edu.ucla.cens.budburstmobile.myplants.PBBAddNotes;
import edu.ucla.cens.budburstmobile.myplants.PBBAddSite;
import edu.ucla.cens.budburstmobile.myplants.PBBPlantList;
import edu.ucla.cens.budburstmobile.onetime.OneTimeAddMyPlant;
import edu.ucla.cens.budburstmobile.onetime.OneTimeMainPage;
import edu.ucla.cens.budburstmobile.onetime.OneTimePhenophase;
import edu.ucla.cens.budburstmobile.utils.PBBItems;
import edu.ucla.cens.budburstmobile.utils.QuickCapture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListUserDefinedSpecies extends ListActivity {
	
	private ArrayList<HelperPlantItem> mArr;
	private MyListAdapterWithIndex mAdapter;
	private ListView mListView;
	private HelperFunctionCalls mHelper;
	
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>();
	
	//private String mImagePath;
	private String mDate;
	private String mNewPlantSpeciesName;
	
	private int mCurrentPosition = 0;
	private int mProtocolID;
	private int mPreviousActivity = 0;
	private int mNewPlantSpeciesID;
	private int mCategory;
	
	private Double mLatitude;
	private Double mLongitude;
	
	private CharSequence[] mSeqUserSite;
	
	private PBBItems pbbItem;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    setContentView(R.layout.locallist);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);
		
		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		mCategory = pbbItem.getCategory();
		mPreviousActivity = bundle.getInt("from");
		
		TextView myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" " + getString(R.string.Title_User_Defined_List));
	
		mHelper = new HelperFunctionCalls();
		mapUserSiteNameID = mHelper.getUserSiteIDMap(this);
		
		getLists();
		// TODO Auto-generated method stub
	}
	
	private int setProtocol(int protocolID) {
		int getProtocolID = 0;
		
		switch(protocolID) {
		case 2:
			getProtocolID = HelperValues.QUICK_WILD_FLOWERS;
			break;
		case 4:case 6:
			getProtocolID = HelperValues.QUICK_TREES_AND_SHRUBS;
			break;
		case 3:
			getProtocolID = HelperValues.QUICK_GRASSES;
			break;
		}
		
		return getProtocolID;
		
	}
	
	public void getLists() {
		// initialize treeLists
		mArr = new ArrayList<HelperPlantItem>();
		
		// open database and put all tree lists into the PlantItem
		OneTimeDBHelper otDBH = new OneTimeDBHelper(ListUserDefinedSpecies.this);
		SQLiteDatabase otDB = otDBH.getReadableDatabase();
		
		Cursor cursor;
		cursor = otDB.rawQuery("SELECT id, common_name, science_name, credit, protocol_id FROM userDefineLists WHERE category = " + mCategory + " ORDER BY common_name;", null);
		while(cursor.moveToNext()) {
			//public HelperPlantItem(int aSpeciesID, String aCommonName, String aSpeciesName, String aCredit) {
			HelperPlantItem pi = new HelperPlantItem();
			pi.setSpeciesID(cursor.getInt(0));
			pi.setCommonName(cursor.getString(1));
			pi.setSpeciesName(cursor.getString(2));
			pi.setCredit(cursor.getString(3));
			pi.setProtocolID(cursor.getInt(4));
			mArr.add(pi);
		}
		if(mPreviousActivity == HelperValues.FROM_ADD_REG || mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE)
			mAdapter = new MyListAdapterWithIndex(ListUserDefinedSpecies.this, R.layout.plantlist_item, mArr, mPreviousActivity);
		else
			mAdapter = new MyListAdapterWithIndex(ListUserDefinedSpecies.this, R.layout.plantlist_item, mArr, mPreviousActivity);
		mListView = getListView();
		// need to add setFastScrollEnalbed(true) for showing the index box in the list...
		mListView.setFastScrollEnabled(true);
		mListView.setAdapter(mAdapter);
		
		cursor.close();
		otDBH.close();
		otDB.close();
		
		if(mArr.size() == 0) {
			TextView instruction = (TextView)findViewById(R.id.instruction);
			instruction.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		
		mCurrentPosition = position;
		
		/*
		 * Act differently based on the previous activity
		 *  1. FROM_PLANT_LIST : directly add the species into the database.
		 *  2. others : move to ListsDetail page.
		 */
		
		if(mPreviousActivity == HelperValues.FROM_ADD_REG) {
			showPlantDialog(position);
		}
		else if(mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE) {
			
			Intent intent = new Intent(ListUserDefinedSpecies.this, OneTimePhenophase.class);

			pbbItem.setSpeciesID(mArr.get(mCurrentPosition).getSpeciesID());
			pbbItem.setCommonName(mArr.get(mCurrentPosition).getCommonName());
			pbbItem.setScienceName(mArr.get(mCurrentPosition).getSpeciesName());
			pbbItem.setProtocolID(mHelper.toSharedProtocol(mArr.get(mCurrentPosition).getProtocolID()));
			
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("from", HelperValues.FROM_USER_DEFINED_LISTS);
			
			startActivity(intent);
		}
		else {
			//if(mPreviousActivity == HelperValues.FROM_PLANT_LIST) {
			Intent intent = new Intent(ListUserDefinedSpecies.this, ListDetail.class);
			pbbItem.setSpeciesID(mArr.get(mCurrentPosition).getSpeciesID());
			pbbItem.setCommonName(mArr.get(mCurrentPosition).getCommonName());
			pbbItem.setScienceName(mArr.get(mCurrentPosition).getSpeciesName());
			pbbItem.setProtocolID(mArr.get(mCurrentPosition).getProtocolID());
			
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("from", HelperValues.FROM_USER_DEFINED_LISTS);

			startActivity(intent);
		}
	}
	
	
	private void showPlantDialog(int position) {
		
		mCurrentPosition = position;
		/*
		 * If the previous activity is from MY_PLANT, show the popup message.
		 */
		if(mPreviousActivity == HelperValues.FROM_ADD_REG) {
			popupDialog(mCurrentPosition);
		}
		/*
		 * Else, move to AddNotes page.
		 */
		else {
			Intent intent = new Intent(ListUserDefinedSpecies.this, PBBAddNotes.class);
		
			pbbItem.setSpeciesID(mArr.get(mCurrentPosition).getSpeciesID());
			pbbItem.setCommonName(mArr.get(mCurrentPosition).getCommonName());
			pbbItem.setScienceName(mArr.get(mCurrentPosition).getSpeciesName());
			pbbItem.setProtocolID(mProtocolID);
			pbbItem.setDate(mDate);
			pbbItem.setLatitude(mLatitude);
			pbbItem.setLongitude(mLongitude);
			
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("from", HelperValues.FROM_QUICK_CAPTURE);
			
			startActivity(intent);
		}
	}
	
	private void popupDialog(int position) {
		//Pop up choose site dialog box
		mNewPlantSpeciesID = mArr.get(position).getSpeciesID();
		mNewPlantSpeciesName = mArr.get(position).getCommonName();
		
		mSeqUserSite = mHelper.getUserSite(ListUserDefinedSpecies.this);
		
		//Pop up choose site dialog box
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.AddPlant_chooseSite))
		.setItems(mSeqUserSite, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				int new_plant_site_id = mapUserSiteNameID.get(mSeqUserSite[which].toString());
				String new_plant_site_name = mSeqUserSite[which].toString();
				
				if(new_plant_site_name == "Add New Site") {
					Intent intent = new Intent(ListUserDefinedSpecies.this, PBBAddSite.class);
					pbbItem.setSpeciesID(mArr.get(mCurrentPosition).getSpeciesID());
					pbbItem.setCommonName(mArr.get(mCurrentPosition).getCommonName());
					pbbItem.setProtocolID(mArr.get(mCurrentPosition).getProtocolID());
					
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", HelperValues.FROM_PLANT_LIST);					
					startActivity(intent);
				}
				else {
					if(mHelper.checkIfNewPlantAlreadyExists(mArr.get(mCurrentPosition).getSpeciesID(), 
							new_plant_site_id, ListUserDefinedSpecies.this)){
						Toast.makeText(ListUserDefinedSpecies.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{
						if(mHelper.insertNewMyPlantToDB(ListUserDefinedSpecies.this, mArr.get(mCurrentPosition).getSpeciesID(), 
								mNewPlantSpeciesName, new_plant_site_id, new_plant_site_name, 
								mArr.get(mCurrentPosition).getProtocolID(), mCategory)){
							Intent intent = new Intent(ListUserDefinedSpecies.this, PBBPlantList.class);
							Toast.makeText(ListUserDefinedSpecies.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							//clear all stacked activities.
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(ListUserDefinedSpecies.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
}
