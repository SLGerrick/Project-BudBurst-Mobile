package edu.ucla.cens.budburstmobile.lists;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.database.OneTimeDBHelper;
import edu.ucla.cens.budburstmobile.database.StaticDBHelper;
import edu.ucla.cens.budburstmobile.database.SyncDBHelper;
import edu.ucla.cens.budburstmobile.helper.HelperDrawableManager;
import edu.ucla.cens.budburstmobile.helper.HelperFunctionCalls;
import edu.ucla.cens.budburstmobile.helper.HelperPlantItem;
import edu.ucla.cens.budburstmobile.helper.HelperValues;
import edu.ucla.cens.budburstmobile.myplants.PBBAddPlant;
import edu.ucla.cens.budburstmobile.myplants.PBBAddSite;
import edu.ucla.cens.budburstmobile.myplants.PBBPlantList;
import edu.ucla.cens.budburstmobile.onetime.OneTimePhenophase;
import edu.ucla.cens.budburstmobile.utils.PBBItems;
import edu.ucla.cens.budburstmobile.utils.QuickCapture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

/**
 * The detail information on the list
 * @author kyunghan
 *
 */
public class ListDetail extends Activity {

	private static final int COMPLETE = 0;
	private int mSpeciesID;
	private int mCategory;
	private int mPreviousActivity = 0;
	private int mProtocolID;
	
	private String mCommonName;
	private String mScienceName;
	private String mImageID;
	private CharSequence[] mSeqUserSite;
	
	private TextView myTitleText;
	private ImageView speciesImage;
	private ProgressBar mSpinner;
	private TextView cName;
	private TextView sName;
	private TextView credit;
	private Intent mPintent;
	private Button myplantBtn;
	private Button sharedplantBtn;
	private LinearLayout usdaLayout;
	private PBBItems pbbItem;
	private int mHasFooter=1;
	
	private HelperFunctionCalls mHelper;
	private HashMap<String, Integer> mMapUserSiteNameID = new HashMap<String, Integer>();
	private String []itemArray;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    // set title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.listdetail);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(" Species Info");
	    
		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		mCategory = pbbItem.getCategory();
		mScienceName = pbbItem.getScienceName();
		mCommonName = pbbItem.getCommonName();
		mSpeciesID = pbbItem.getSpeciesID();
		mProtocolID = pbbItem.getProtocolID();
		mHasFooter = pbbItem.getPhenophaseID();
		mPreviousActivity = bundle.getInt("from");
		
		Log.i("K", "mCategory : " + mCategory);
		
		
    	// Show footer or not.
    	if(mHasFooter==HelperValues.FOOTER) {
    		LinearLayout footer = (LinearLayout)findViewById(R.id.lower);
    		footer.setVisibility(View.VISIBLE);
       	}
    	else
    		mCategory = 42;
    	
	    // setup the layout
		speciesImage = (ImageView) findViewById(R.id.webimage);
		mSpinner = (ProgressBar) findViewById(R.id.progressbar);
		cName = (TextView) findViewById(R.id.common_name);
		sName = (TextView) findViewById(R.id.science_name);
		credit = (TextView) findViewById(R.id.credit);
		myplantBtn = (Button) findViewById(R.id.to_myplant);
		sharedplantBtn = (Button) findViewById(R.id.to_shared_plant);
		
		
		// Call FunctionsHelper();
		mHelper = new HelperFunctionCalls();
		
		mMapUserSiteNameID = mHelper.getUserSiteIDMap(ListDetail.this);
		
		getDetailInfo();
	}
	
	private void getDetailInfo() {
		/*
		 * Retreive user sites from database.
		 */
		mSeqUserSite = mHelper.getUserSite(ListDetail.this);
		
		/*
		 *  Get data from the table.
		 */
	    OneTimeDBHelper otDBH = new OneTimeDBHelper(ListDetail.this);
	    SQLiteDatabase db = otDBH.getReadableDatabase();
	    
	    Cursor cursor;
	    
	    /*
	     * If the previous activity is from "Local plants from national plant lists"
	     */
	    if(mPreviousActivity == HelperValues.FROM_LOCAL_PLANT_LISTS) {
	    	
			if(mCategory == HelperValues.LOCAL_BUDBURST_LIST) {
				
				usdaLayout = (LinearLayout)findViewById(R.id.lower_info);
				usdaLayout.setVisibility(View.GONE);
				
				LinearLayout invisibleLayout = (LinearLayout)findViewById(R.id.upper_invisible);
				invisibleLayout.setVisibility(View.VISIBLE);
				
				TextView t1 = (TextView)findViewById(R.id.science_name2);
		 	    TextView t2 = (TextView)findViewById(R.id.common_name2);
		 	    TextView note = (TextView)findViewById(R.id.text);
		 	    ImageView image2 = (ImageView)findViewById(R.id.species_image);
		 	    Button moreInfoBtn = (Button)findViewById(R.id.more_info);
				
				
				StaticDBHelper staticDB = new StaticDBHelper(ListDetail.this);
				SQLiteDatabase sDB = staticDB.getReadableDatabase();
				
				Cursor getSpeciesInfo = sDB.rawQuery("SELECT _id, species_name, common_name, description FROM species WHERE species_name = \"" + mScienceName + "\";", null);
				while(getSpeciesInfo.moveToNext()) {
					t1.setText(" " + getSpeciesInfo.getString(2) + " ");
				    t2.setText(" " + getSpeciesInfo.getString(1) + " ");
				    note.setText("" + getSpeciesInfo.getString(3) + " ");
				    image2.setImageResource(getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/s"+getSpeciesInfo.getInt(0), null, null));
				    mSpeciesID = getSpeciesInfo.getInt(0);
				}
				
				getSpeciesInfo.close();
				sDB.close();
				
				moreInfoBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						usdaLayout.setVisibility(View.VISIBLE);
					}
				});
			} 
		
			if(mHasFooter==HelperValues.FOOTER) {
	    		LinearLayout footer = (LinearLayout)findViewById(R.id.lower);
	    		footer.setVisibility(View.VISIBLE);
	       	}
	    	else
	    		mCategory = 42;
			/*
		     * Retrieve information from localPlantLists
		     */
		    cursor = db.rawQuery("SELECT common_name, science_name, county, state, usda_url, photo_url, copy_right, image_id FROM localPlantLists WHERE " 
		    		+ "science_name=\"" + mScienceName 
		    		+"\"" , null);
		    	
		    String image_url = "";
		    	
			while(cursor.moveToNext()) {
					
				/*
				 * This is how to link the page dynamically by using Pattern and Linkify.
				 */
					
				cName.setText(cursor.getString(0));
				sName.setText(cursor.getString(1));
				credit.setText(
						"\nCounty - " + cursor.getString(2) +
						"\n\nState - " + cursor.getString(3) +
						"\n\nUSDA link : " + cursor.getString(4) +
						"\n\nPhoto By - " + cursor.getString(6));
					
				mImageID = cursor.getString(7);
					
				Log.i("K", "ListDetail(imageID) : " + mImageID);
					
				/*
				 * Link the url point to the USDA webpage.
				 */
				Linkify.addLinks(credit, Linkify.WEB_URLS);
				image_url = cursor.getString(5);
					
				mCommonName = cursor.getString(0).toString();
				mScienceName = cursor.getString(1).toString();
			}
				
			otDBH.close();
			db.close();
			cursor.close();
						
			Log.i("K", "imageURL : " + image_url);
				
			/*
			 * Change the size of it...
			 */
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(140,140);
			speciesImage.setLayoutParams(layoutParams);
				
			// If there's a cached image in the sdcard, retrieve it; otherwise, show that on the webpage 
			String imagePath = HelperValues.LOCAL_LIST_PATH + mImageID + ".jpg";
			File checkExistFile = new File(imagePath);
			if(checkExistFile.exists()) {
				//speciesImage.
				speciesImage.setImageBitmap(overlay(BitmapFactory.decodeFile(imagePath)));
				mSpinner.setVisibility(View.GONE);
			}
			else {
				/*
				 *  Load image from the server
				 */
				HelperDrawableManager dm = new HelperDrawableManager(ListDetail.this, mSpinner, speciesImage);
				dm.fetchDrawableOnThread(image_url);
			}
	
	    	
			myplantBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(mCategory == HelperValues.LOCAL_BUDBURST_LIST) {
						StaticDBHelper staticDBHelper = new StaticDBHelper(ListDetail.this);
						SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
						Cursor c = staticDB.rawQuery("SELECT protocol_id FROM species WHERE _id = " + mSpeciesID, null);
						while(c.moveToNext()) {
							mProtocolID = c.getInt(0);
						}
						c.close();
						staticDB.close();
						
						popupDialog();
					}
					else {
						showProtocolDialog();
					}
				}
			});	
			
			sharedplantBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					/*
					 * We already know the Shared Plant category if we choose species from official budburst lists.
					 * - hardcoded protocolID
					 */
					Log.i("K", "ListDeatil(speciesID) : " + mSpeciesID);
					
					if(mSpeciesID == 0) { 
						
						new AlertDialog.Builder(ListDetail.this)
						.setTitle(getString(R.string.AddPlant_SelectCategory))
						.setIcon(android.R.drawable.ic_menu_more)
						.setItems(R.array.quick_capture_phenophase_category, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								String[] category = getResources().getStringArray(R.array.quick_capture_phenophase_category);
								
								/*
								 * Choose category and set the protocol_id based on that.
								 * 		<item>Trees and Shrubs</item>
										<item>Wild Flowers</item>
										<item>Grasses</item> 
								*/ 
								
								if(category[which].equals("Trees and Shrubs")) {
									mProtocolID = HelperValues.QUICK_TREES_AND_SHRUBS;
								}
								else if(category[which].equals("Wild Flowers")) {
									mProtocolID = HelperValues.QUICK_WILD_FLOWERS;
								}								
								else if(category[which].equals("Grasses")) {
									mProtocolID = HelperValues.QUICK_GRASSES;
								}
								
								showSharedProtocolDialog();
							}
						}).show();

					}
					else {
						int getSpeciesID = mSpeciesID;
						int getProtocolID = 2;
						
						StaticDBHelper staticDBH = new StaticDBHelper(ListDetail.this);
						SQLiteDatabase staticDB = staticDBH.getReadableDatabase();
						
						Cursor cursor = staticDB.rawQuery("SELECT protocol_id FROM species WHERE _id=" + getSpeciesID + ";", null);
						while(cursor.moveToNext()) {
							getProtocolID = cursor.getInt(0);
						}
						cursor.close();
						staticDB.close();
						
						mProtocolID = mHelper.toSharedProtocol(getProtocolID);
						
						showSharedProtocolDialog();
					}					
				}
			});
					
	    }
	    /*
	     * If from User defined lists
	     */
	    else if(mPreviousActivity == HelperValues.FROM_USER_DEFINED_LISTS){
	    	
	    	
	    	Log.i("K", "speciesID(Tree) : " + mSpeciesID);
	    	
	    	cursor = db.rawQuery("SELECT common_name, science_name, credit FROM userDefineLists WHERE id=" 
	    			+ mSpeciesID +";", null);
			while(cursor.moveToNext()) {
				cName.setText(cursor.getString(0));
				sName.setText(cursor.getString(1));
				credit.setText("Photo By - " + cursor.getString(2));
				
				mCommonName = cursor.getString(0).toString();
				mScienceName = cursor.getString(1).toString();
			}
			otDBH.close();
			db.close();
			cursor.close();
			
			HelperDrawableManager dm = new HelperDrawableManager(ListDetail.this, mSpinner, speciesImage);
			dm.fetchDrawableOnThread(getString(R.string.get_user_defined_tree_large_image) 
					+ mSpeciesID + ".jpg");
			
			Log.i("K", "imageURL : " + getString(R.string.get_user_defined_tree_large_image) 
					+ mSpeciesID + ".jpg");
			
			
			myplantBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					showProtocolDialog();
				}
			});
			
			sharedplantBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
					/*
					 * Ask users if they are ready to take a photo.
					 */
					new AlertDialog.Builder(ListDetail.this)
					.setTitle(getString(R.string.Menu_addQCPlant))
					.setMessage(getString(R.string.Start_Shared_Plant))
					.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							/*
							 * Move to QuickCapture
							 */
							Intent intent = new Intent(ListDetail.this, QuickCapture.class);
							
							pbbItem.setProtocolID(mHelper.toSharedProtocol(mProtocolID));
							
							intent.putExtra("pbbItem", pbbItem);
							intent.putExtra("from", HelperValues.FROM_USER_DEFINED_LISTS);
							
							startActivity(intent);
						}
					})
					.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							/*
							 * Move to Getphenophase without a photo.
							 */
							
							Intent intent = new Intent(ListDetail.this, OneTimePhenophase.class);
							
							pbbItem.setLocalImageName("");
							pbbItem.setProtocolID(mHelper.toSharedProtocol(mProtocolID));
							
							intent.putExtra("pbbItem", pbbItem);
							intent.putExtra("from", HelperValues.FROM_USER_DEFINED_LISTS);
							
							startActivity(intent);
						}
					})
					.setNegativeButton("Back", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						
						}
					})
					.show();
				}
			});
	    }
	    else {
	    	
	    	mSpeciesID = mPintent.getIntExtra("id", 0); 
	    	
	    	cursor = db.rawQuery("SELECT common_name, science_name, credit FROM userDefineLists WHERE id=" + mSpeciesID +";", null);
			while(cursor.moveToNext()) {
				cName.setText(cursor.getString(0));
				sName.setText(cursor.getString(1));
				credit.setText("Photo By - " + cursor.getString(2));
				
				mCommonName = cursor.getString(0).toString();
				mScienceName = cursor.getString(1).toString();
			}
			otDBH.close();
			db.close();
			cursor.close();
			
			/*
			 *  Load image from the server
			 */
			HelperDrawableManager dm = new HelperDrawableManager(ListDetail.this, mSpinner, speciesImage);
			dm.fetchDrawableOnThread(getString(R.string.get_user_defined_tree_large_image) + mSpeciesID + ".jpg");
	    	
	    	
	    	LinearLayout footer = (LinearLayout)findViewById(R.id.lower);
	    	footer.setVisibility(View.GONE);
	    }
	    
	}
	
	@Override
	public void onResume() {
	    // TODO Auto-generated method stub
		super.onResume();
	}
	
	private Bitmap overlay(Bitmap... bitmaps) {
		
		if (bitmaps[0].equals(null))
			return null;

		Bitmap bmOverlay = Bitmap.createBitmap(130, 130, Bitmap.Config.ARGB_4444);

		Canvas canvas = new Canvas(bmOverlay);
		for (int i = 0; i < bitmaps.length; i++)
			canvas.drawBitmap(bitmaps[i], new Matrix(), null);

		return bmOverlay;
	}
	
	private void showSharedProtocolDialog() {
		
		/*
		 * Ask users if they are ready to take a photo.
		 */
		new AlertDialog.Builder(ListDetail.this)
		.setTitle(getString(R.string.Menu_addQCPlant))
		.setMessage(getString(R.string.Start_Shared_Plant))
		.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				/*
				 * Move to QuickCapture
				 */
				Intent intent = new Intent(ListDetail.this, QuickCapture.class);
				pbbItem.setProtocolID(mProtocolID);

				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("image_id", mImageID);
				intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
				
				startActivity(intent);

			}
		})
		.setNeutralButton(getString(R.string.Button_NoPhoto), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				/*
				 * Move to Getphenophase without a photo.
				 */
				Intent intent = new Intent(ListDetail.this, OneTimePhenophase.class);
				pbbItem.setProtocolID(mProtocolID);
				pbbItem.setLocalImageName("");
				
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("image_id", mImageID);
				intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
				
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
	
	private void showProtocolDialog() {
		
		if(mCategory >= HelperValues.USER_DEFINED_TREE_LISTS) {
			//itemArray = getResources().getStringArray(R.array.category_only_trees);
			popupDialog();
		}
		else {
			itemArray = getResources().getStringArray(R.array.category);
			
			
			new AlertDialog.Builder(ListDetail.this)
			.setTitle(getString(R.string.AddPlant_SelectCategory))
			.setIcon(android.R.drawable.ic_menu_more)
			.setItems(itemArray, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String[] category = itemArray;
					StaticDBHelper staticDBHelper = new StaticDBHelper(ListDetail.this);
					SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase(); 
					
					Cursor cursor = null;
					
					/*
					 * Choose category and set the protocol_id based on that. 
					 */
					
					if(category[which].equals("Wild Flowers and Herbs")) {
						cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + HelperValues.WILD_FLOWERS + " ORDER BY common_name;",null);
						myTitleText.setText(" " + getString(R.string.AddPlant_addFlowers));
						mProtocolID = HelperValues.WILD_FLOWERS;
					}
					else if(category[which].equals("Grass")) {
						cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + HelperValues.GRASSES + " ORDER BY common_name;",null);
						myTitleText.setText(" " + getString(R.string.AddPlant_addGrass));
						mProtocolID = HelperValues.GRASSES;
					}
					else if(category[which].equals("Deciduous Trees and Shrubs")) {
						cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + HelperValues.DECIDUOUS_TREES + " OR protocol_id=" + HelperValues.DECIDUOUS_TREES_WIND + " ORDER BY common_name;",null);
						myTitleText.setText(" " + getString(R.string.AddPlant_addDecid));
						mProtocolID = HelperValues.DECIDUOUS_TREES;
					}
					else if(category[which].equals("Evergreen Trees and Shrubs")) {
						cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + HelperValues.EVERGREEN_TREES + " OR protocol_id=" + HelperValues.EVERGREEN_TREES_WIND + " ORDER BY common_name;",null);
						myTitleText.setText(" " + getString(R.string.AddPlant_addEvergreen));
						mProtocolID = HelperValues.EVERGREEN_TREES;
					}
					else if(category[which].equals("Conifer")) {
						cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + HelperValues.CONIFERS + " ORDER BY common_name;",null);
						myTitleText.setText(" " + getString(R.string.AddPlant_addConifer));
						mProtocolID = HelperValues.CONIFERS;
					}
					else {
						
					}
					
					cursor.close();
					staticDB.close();
					
					popupDialog();
					
				}
			}).show();
		}
	}
	
	private void popupDialog() {
		/*
		 * Pop up choose site dialog box
		 */
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.AddPlant_chooseSite))
		.setCancelable(true)
		.setItems(mSeqUserSite, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			
				int new_plant_site_id = mMapUserSiteNameID.get(mSeqUserSite[which].toString());
				String new_plant_site_name = mSeqUserSite[which].toString();
				
				// if category is not LOCAL_PLANT_LIST,
				// we put speciesID to Unknown(999)
				if(mCategory != HelperValues.LOCAL_BUDBURST_LIST && mPreviousActivity != HelperValues.FROM_USER_DEFINED_LISTS) {
					mSpeciesID = HelperValues.UNKNOWN_SPECIES;
				}
				
				if(new_plant_site_name == "Add New Site") {
					Intent intent = new Intent(ListDetail.this, PBBAddSite.class);
					pbbItem.setProtocolID(mProtocolID);
					pbbItem.setSpeciesID(mSpeciesID);
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
					
					startActivity(intent);
				}
				else {
					if(mHelper.checkIfNewPlantAlreadyExists(mSpeciesID, new_plant_site_id, ListDetail.this)){
						Toast.makeText(ListDetail.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}
					else{
						
						if(mHelper.insertNewMyPlantToDB(ListDetail.this, mSpeciesID, mCommonName, new_plant_site_id, new_plant_site_name, mProtocolID, mCategory)){
							Intent intent = new Intent(ListDetail.this, PBBPlantList.class);
							Toast.makeText(ListDetail.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							
							/*
							 * Clear all stacked activities.
							 */
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(ListDetail.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
}
