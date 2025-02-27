package edu.ucla.cens.budburstmobile.onetime;

//List for Local List Plants

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.ucla.cens.budburstmobile.PBBLogin;
import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.adapter.MyListAdapter;
import edu.ucla.cens.budburstmobile.database.OneTimeDBHelper;
import edu.ucla.cens.budburstmobile.database.StaticDBHelper;
import edu.ucla.cens.budburstmobile.database.SyncDBHelper;
import edu.ucla.cens.budburstmobile.helper.HelperFunctionCalls;
import edu.ucla.cens.budburstmobile.helper.HelperJSONParser;
import edu.ucla.cens.budburstmobile.helper.HelperLazyAdapter;
import edu.ucla.cens.budburstmobile.helper.HelperPlantItem;
import edu.ucla.cens.budburstmobile.helper.HelperValues;
import edu.ucla.cens.budburstmobile.lists.ListDetail;
import edu.ucla.cens.budburstmobile.lists.ListDownloadFromServer;
import edu.ucla.cens.budburstmobile.lists.ListItems;
import edu.ucla.cens.budburstmobile.lists.ListSubCategory;
import edu.ucla.cens.budburstmobile.myplants.DetailPlantInfo;
import edu.ucla.cens.budburstmobile.myplants.PBBAddPlant;
import edu.ucla.cens.budburstmobile.myplants.PBBAddSite;
import edu.ucla.cens.budburstmobile.myplants.PBBPlantList;
import edu.ucla.cens.budburstmobile.utils.PBBItems;

public class OneTimeAddMyPlant extends ListActivity {
	private SharedPreferences pref;
	private ArrayList<HelperPlantItem> arSpeciesList;
	private HelperLazyAdapter lazyadapter;
	private OneTimeDBHelper otDBH;
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>();
	private HelperFunctionCalls helper;
	private TextView myTitleText;
	
	private String mNewPlantSpeciesName;
	private int mNewPlantSpeciesID;
	private int mProtocolID = 1;
	private int mPreviousActivity = 0;
	private int mCurrentPosition = 0;
	private int mCategory = 0;
	private CharSequence[] mSeqUserSite;
	private ListView MyList;
	
	private PBBItems pbbItem;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.whatsinvasive);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" Whats Invasive");
		
		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		mCategory = pbbItem.getCategory();
		mPreviousActivity = bundle.getInt("from");
		
	    arSpeciesList = new ArrayList<HelperPlantItem>();
	    otDBH = new OneTimeDBHelper(OneTimeAddMyPlant.this);
	    
	}
	
	@Override
	public void onResume() {
		super.onResume();
		helper = new HelperFunctionCalls();
		mapUserSiteNameID = helper.getUserSiteIDMap(OneTimeAddMyPlant.this);
		//Get User site name and id using Map.
		pref = getSharedPreferences("userinfo",0);
		showExistedSpecies();
	}
	

	public void showExistedSpecies() {
		
		arSpeciesList = null;
		arSpeciesList = new ArrayList<HelperPlantItem>();
		
		/*
		 * Open database and read data from the localPlantLists table.
		 */
		OneTimeDBHelper otDBH = new OneTimeDBHelper(this);
		SQLiteDatabase otDB = otDBH.getReadableDatabase();

		Cursor cursor = otDB.rawQuery("SELECT category, common_name, science_name, photo_url " +
				"FROM localPlantLists WHERE category=" + mCategory + 
				" ORDER BY LOWER(common_name) ASC;", null);
		
		while(cursor.moveToNext()) {
			HelperPlantItem pi = new HelperPlantItem();
			pi.setCommonName(cursor.getString(1)); 
			pi.setSpeciesName(cursor.getString(2));
			pi.setImageURL(cursor.getString(3));
			pi.setCategory(cursor.getInt(0));
			arSpeciesList.add(pi);
		}
		
		otDBH.close();
		otDB.close();
		cursor.close();
		
		/*
		 * Connect to the adapter
		 */
		lazyadapter = new HelperLazyAdapter(this, arSpeciesList, mPreviousActivity);
		MyList = getListView();
		MyList.setAdapter(lazyadapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		//String imagePath = Values.WI_PATH + arSpeciesList.get(position).imageUrl + ".jpg";
		
		/*
		 * If the previous activity is from "Add_PLANT", we just add a new species and end
		 */
		unKnownPlantDialog(position);
	}
	
	private void unKnownPlantDialog(int position) {
		
		mCurrentPosition = position;
		
		/*
		 * Choosing category dialog is only for FROM_PLANT_LIST
		 */
		if(mPreviousActivity == HelperValues.FROM_ADD_REG) {
			
			new AlertDialog.Builder(OneTimeAddMyPlant.this)
			.setTitle(getString(R.string.AddPlant_SelectCategory))
			.setIcon(android.R.drawable.ic_menu_more)
			.setItems(R.array.category, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String[] category = getResources().getStringArray(R.array.category);
		
					if(category[which].equals("Wild Flowers and Herbs")) {
						mProtocolID = HelperValues.WILD_FLOWERS;
					}
					else if(category[which].equals("Grass")) {
						mProtocolID = HelperValues.GRASSES;
					}
					else if(category[which].equals("Deciduous Trees and Shrubs")) {
						mProtocolID = HelperValues.DECIDUOUS_TREES;
					}
					else if(category[which].equals("Evergreen Trees and Shrubs")) {
						mProtocolID = HelperValues.EVERGREEN_TREES;
					}
					else if(category[which].equals("Conifer")) {
						mProtocolID = HelperValues.CONIFERS;
					}
					else {
					}
					
					popupDialog(mCurrentPosition);
				}
			})
			.setNegativeButton(getString(R.string.Button_back), null)
			.show();
		}
		else if(mPreviousActivity == HelperValues.FROM_PLANT_LIST) {
			mNewPlantSpeciesID = HelperValues.UNKNOWN_SPECIES;
			mNewPlantSpeciesName = arSpeciesList.get(position).getCommonName();
			
			mSeqUserSite = helper.getUserSite(OneTimeAddMyPlant.this);			
			
			Intent intent = new Intent(OneTimeAddMyPlant.this, ListDetail.class);
			
			pbbItem.setCommonName(arSpeciesList.get(mCurrentPosition).getCommonName());
			pbbItem.setScienceName(arSpeciesList.get(mCurrentPosition).getSpeciesName());
			pbbItem.setProtocolID(mProtocolID);
			pbbItem.setPhenophaseID(0);
			pbbItem.setSpeciesID(arSpeciesList.get(mCurrentPosition).getSpeciesID());
			pbbItem.setCategory(mCategory);
			Log.d("---------TEST--------", "Local List ");
		//	Log.d("---------TEST--------", "Plant list, "+mPhenoID+" "+arSpeciesList.get(mCurrentPosition).getCommonName());
			intent.putExtra("pbbItem", pbbItem);
			intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
			
			startActivity(intent);
		}
		else {
			
			
			new AlertDialog.Builder(OneTimeAddMyPlant.this)
			.setTitle("Select Category")
			.setIcon(android.R.drawable.ic_menu_more)
			.setItems(R.array.quick_capture_phenophase_category, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String[] _category = getResources().getStringArray(R.array.quick_capture_phenophase_category);
					if(_category[which].equals("Trees and Shrubs")) {
						mProtocolID = 1;
					}
					else if(_category[which].equals("Wild Flowers")) {
						mProtocolID = 2;
					}
					else {
						mProtocolID = 3;
					}
					
					Intent intent = new Intent(OneTimeAddMyPlant.this, OneTimePhenophase.class);

					pbbItem.setCommonName(arSpeciesList.get(mCurrentPosition).getCommonName());
					pbbItem.setScienceName(arSpeciesList.get(mCurrentPosition).getSpeciesName());
					pbbItem.setCategory(mCategory);
					pbbItem.setProtocolID(mProtocolID);
					pbbItem.setSpeciesID(HelperValues.UNKNOWN_SPECIES);
					intent.putExtra("pbbItem", pbbItem);
					
					intent.putExtra("from", mPreviousActivity);
					startActivity(intent);
				}
			})
			.setNegativeButton("Back", null)
			.show();
		}	
	}
	
	private void popupDialog(int position) {
		/*
		 * Pop up choose site dialog box
		 */
		mNewPlantSpeciesID = HelperValues.UNKNOWN_SPECIES;
		mNewPlantSpeciesName = arSpeciesList.get(position).getCommonName();
		
		mSeqUserSite = helper.getUserSite(OneTimeAddMyPlant.this);
		
		/*
		 * Pop up choose site dialog box
		 */
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.AddPlant_chooseSite))
		.setItems(mSeqUserSite, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				int new_plant_site_id = mapUserSiteNameID.get(mSeqUserSite[which].toString());
				String new_plant_site_name = mSeqUserSite[which].toString();
				
				if(new_plant_site_name == "Add New Site") {
					Intent intent = new Intent(OneTimeAddMyPlant.this, PBBAddSite.class);

					pbbItem.setCommonName(arSpeciesList.get(mCurrentPosition).getCommonName());
					pbbItem.setScienceName(arSpeciesList.get(mCurrentPosition).getSpeciesName());
					pbbItem.setCategory(mCategory);
					pbbItem.setProtocolID(mProtocolID);
					pbbItem.setSpeciesID(HelperValues.UNKNOWN_SPECIES);
					intent.putExtra("pbbItem", pbbItem);
					
					intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
					startActivity(intent);
				}
				else {
					if(helper.checkIfNewPlantAlreadyExists(mNewPlantSpeciesID, new_plant_site_id, OneTimeAddMyPlant.this)){
						Toast.makeText(OneTimeAddMyPlant.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{
						
						if(helper.insertNewMyPlantToDB(OneTimeAddMyPlant.this, mNewPlantSpeciesID, mNewPlantSpeciesName, new_plant_site_id, new_plant_site_name, mProtocolID, mCategory)){
							Intent intent = new Intent(OneTimeAddMyPlant.this, PBBPlantList.class);
							Toast.makeText(OneTimeAddMyPlant.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							
						
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(OneTimeAddMyPlant.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
						
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}

}
