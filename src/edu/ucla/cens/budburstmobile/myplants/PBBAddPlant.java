package edu.ucla.cens.budburstmobile.myplants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.adapter.MyListAdapter;
import edu.ucla.cens.budburstmobile.database.OneTimeDBHelper;
import edu.ucla.cens.budburstmobile.database.StaticDBHelper;
import edu.ucla.cens.budburstmobile.database.SyncDBHelper;
import edu.ucla.cens.budburstmobile.helper.HelperFunctionCalls;
import edu.ucla.cens.budburstmobile.helper.HelperPlantItem;
import edu.ucla.cens.budburstmobile.helper.HelperValues;
import edu.ucla.cens.budburstmobile.utils.PBBItems;

public class PBBAddPlant extends ListActivity{
	private ArrayList<HelperPlantItem> arPlantList;
	
	private int mSelect = 0;
	private Integer mNewPlantSpeciesID;
	private String mNewPlantSpeciesName;
	private Integer mNewPlantSiteID; 
	private String mNewPlantSiteName;
	private Integer mProtocolID;
	
	private StaticDBHelper staticDBHelper = null;
	private SQLiteDatabase staticDB = null;
	private MyListAdapter mylistapdater = null;
	private ListView MyList = null;
	
	private Button rb1 = null;
	private Button rb2 = null;
	private Button rb3 = null;
	private Button rb4 = null;
	
	private EditText et1;
	
	//private TextView header = null;
	private TextView myTitleText = null;
	private Dialog Name_dialog = null;
	
	private HelperFunctionCalls mHelper = null;
	private CharSequence[] mSeqUserSite;
	private boolean mSelectCategory = false;
	
	private PBBItems pbbItem;
	
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>(); 
		
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.flora_observer);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText("  " + getString(R.string.AddPlant_top10));
		
		mHelper = new HelperFunctionCalls();
		
		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		
		// setting the radio buttons layout
		rb1 = (Button)findViewById(R.id.option1);
		rb2 = (Button)findViewById(R.id.option2);
		rb3 = (Button)findViewById(R.id.option3);
		rb4 = (Button)findViewById(R.id.option4);
		
		rb1.setOnClickListener(radio_listener);
		rb2.setOnClickListener(radio_listener);
		rb3.setOnClickListener(radio_listener);
		rb4.setOnClickListener(radio_listener);
		
		// show the top 10 lists first
		top10List();

		//Get User site name and id using Map.
		HelperFunctionCalls helper = new HelperFunctionCalls();
		mapUserSiteNameID = helper.getUserSiteIDMap(PBBAddPlant.this);
	}
	
	private void top10List() {
		staticDBHelper = new StaticDBHelper(PBBAddPlant.this);
		staticDB = staticDBHelper.getReadableDatabase();
		
		myTitleText.setText(" " + getString(R.string.AddPlant_top10));
		//header.setText("'TOP 10' list of the plants.");
		arPlantList = new ArrayList<HelperPlantItem>();
	 	Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species ORDER BY common_name;", null);
		while(cursor.moveToNext()){
			Integer id = cursor.getInt(0);
			if(id == 70 || id == 69 || id == 45 || id == 59 || id == 60 || id == 19 || id == 32 || id == 34 || id == 24) {
				String scienceName = cursor.getString(1);
				String commonName = cursor.getString(2);
							
				int resID = getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/s"+id, null, null);
				
				HelperPlantItem pi;
				pi = new HelperPlantItem();
				pi.setPicture(resID);
				pi.setCommonName(commonName);
				pi.setSpeciesName(scienceName);
				pi.setSpeciesID(id);
				
				arPlantList.add(pi);
			}
		}
			
		// add plant at the last.
		HelperPlantItem pi = new HelperPlantItem();
		pi.setPicture(getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/pbb_icon_main2", null, null));
		pi.setCommonName("Unknown/Other");
		pi.setSpeciesName("Unknown/Other");
		pi.setSpeciesID(HelperValues.UNKNOWN_SPECIES);
		
		arPlantList.add(pi);
			
		mylistapdater = new MyListAdapter(PBBAddPlant.this, R.layout.plantlist_item2, arPlantList);
		MyList = getListView(); 
		MyList.setAdapter(mylistapdater);
		cursor.close();		
		staticDB.close();
	}

	
	private OnClickListener radio_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			staticDBHelper = new StaticDBHelper(PBBAddPlant.this);
			staticDB = staticDBHelper.getReadableDatabase();
			
			if(v == rb1) {
				mSelectCategory = false;
				top10List();
			}
			else if (v == rb2) {
				mSelectCategory = false;
				myTitleText.setText(" " + getString(R.string.AddPlant_all));
				//header.setText("'ALL' list of the plants.");
				//Rereive syncDB and add them to arUserPlatList arraylist
				arPlantList = new ArrayList<HelperPlantItem>();
		 		Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name FROM species ORDER BY common_name;", null);
				while(cursor.moveToNext()){
					Integer id = cursor.getInt(0);
					
					// if id is 999, skip that.
					if(id == HelperValues.UNKNOWN_SPECIES) 
						continue;
				
					String scienceName = cursor.getString(1);
					String commonName = cursor.getString(2);
									
					int resID = getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/s"+id, null, null);
						
					HelperPlantItem pi = new HelperPlantItem();
					//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
					pi.setPicture(resID);
					pi.setCommonName(commonName);
					pi.setSpeciesName(scienceName);
					pi.setSpeciesID(id);
					arPlantList.add(pi);
				}
				
				// add plant at the last.
				HelperPlantItem pi = new HelperPlantItem();
				pi.setPicture(getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/pbb_icon_main2", null, null));
				pi.setCommonName("Unknown/Other");
				pi.setSpeciesName("Unknown/Other");
				pi.setSpeciesID(HelperValues.UNKNOWN_SPECIES);
				arPlantList.add(pi);
				
				mylistapdater = new MyListAdapter(PBBAddPlant.this, R.layout.plantlist_item2, arPlantList);
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				cursor.close();
			}
			else if(v == rb3){
				mSelectCategory = true;
				new AlertDialog.Builder(PBBAddPlant.this)
				.setTitle(getString(R.string.AddPlant_SelectCategory))
				.setIcon(android.R.drawable.ic_menu_more)
				.setItems(R.array.category, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] category = getResources().getStringArray(R.array.category);
						StaticDBHelper staticDBHelper = new StaticDBHelper(PBBAddPlant.this);
						SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase(); 
						
						arPlantList = new ArrayList<HelperPlantItem>();
						Cursor cursor = null;

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

						//header.setText("By Group > " + category[which]);
						while(cursor.moveToNext()){
							Integer id = cursor.getInt(0);
							String species_name = cursor.getString(1);
							String common_name = cursor.getString(2);
							Integer protocol_id = cursor.getInt(3);
							
							int resID = getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/s"+id, null, null);
							
							//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
							HelperPlantItem pi = new HelperPlantItem();
							//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
							pi.setPicture(resID);
							pi.setCommonName(common_name);
							pi.setSpeciesName(species_name);
							pi.setSpeciesID(id);
							arPlantList.add(pi);
						}
						
						HelperPlantItem pi = new HelperPlantItem();
						pi.setPicture(getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/pbb_icon_main2", null, null));
						pi.setCommonName("Unknown/Other");
						pi.setSpeciesName("Unknown/Other");
						pi.setSpeciesID(HelperValues.UNKNOWN_SPECIES);
						arPlantList.add(pi);
						
						mylistapdater = new MyListAdapter(PBBAddPlant.this, R.layout.plantlist_item2, arPlantList);
						MyList = getListView(); 
						MyList.setAdapter(mylistapdater);
						
						cursor.close();
						staticDB.close();
						staticDBHelper.close();
						
					}
				})
				.setNegativeButton(getString(R.string.Button_back), null)
				.show();
				
			}
			else {
				mSelectCategory = false;
				myTitleText.setText(" " + getString(R.string.AddPlant_local));
				
				arPlantList = new ArrayList<HelperPlantItem>();
				
				OneTimeDBHelper otDBH = new OneTimeDBHelper(PBBAddPlant.this);
				SQLiteDatabase otDB = otDBH.getReadableDatabase();
				Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name " +
												"FROM species " +
												"ORDER BY common_name;", null);
				
				while(cursor.moveToNext()) {
					String sName = cursor.getString(1);
					
					Cursor cursor2 = otDB.rawQuery("SELECT science_name " +
												"FROM localPlantLists " +
												"WHERE category=1 " +
													"AND science_name=\"" + sName + "\"", null);

					// If there is no local budburst lists (possibly not downloaded yet),
					// there will be some text indicating to download the local budburst lists.
					if(cursor2.getCount() > 0) {
						int resID = getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/s"+cursor.getInt(0), null, null);
						
						String species_name = cursor.getString(1);
						String common_name = cursor.getString(2);
						
						HelperPlantItem pi = new HelperPlantItem();
						//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
						pi.setPicture(resID);
						pi.setCommonName(common_name);
						pi.setSpeciesName(species_name);
						pi.setSpeciesID(cursor.getInt(0));
					
						arPlantList.add(pi);
					}
					else {
						//Toast.makeText(PBBAddPlant.this, "Download local budburst lists", Toast.LENGTH_SHORT).show();
					}
					
					cursor2.close();
				}
				
				otDBH.close();
				otDB.close();
								
				mylistapdater = new MyListAdapter(PBBAddPlant.this, R.layout.plantlist_item2, arPlantList);
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				cursor.close();
			}
			
			staticDB.close();
			staticDBHelper.close();
		}
	};
	

	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		
		mNewPlantSpeciesID = arPlantList.get(position).getSpeciesID();
		mNewPlantSpeciesName = arPlantList.get(position).getCommonName();
		
		//Retreive user sites from database.
		mSeqUserSite = mHelper.getUserSite(PBBAddPlant.this);
		
		if(mNewPlantSpeciesID == HelperValues.UNKNOWN_SPECIES) {
			addCustomName();
		}
		else {
			staticDBHelper = new StaticDBHelper(PBBAddPlant.this);
			SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase();
			Cursor c = staticDB.rawQuery("SELECT protocol_id FROM species WHERE _id = " + mNewPlantSpeciesID, null);
			while(c.moveToNext()) {
				mProtocolID = c.getInt(0);
			}
			c.close();
			staticDB.close();
			popupDialog();
		}
	}
	
	private void plantCategory() {
		
		new AlertDialog.Builder(PBBAddPlant.this)
		.setTitle(getString(R.string.AddPlant_SelectCategory))
		.setIcon(android.R.drawable.ic_menu_more)
		.setCancelable(true)
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
				
				popupDialog();
			}
		})
		.setNegativeButton(getString(R.string.Button_back), null)
		.show();
		
	}
	
	private void popupDialog() {
		//Pop up choose site dialog box

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.AddPlant_chooseSite))
		.setCancelable(true)
		.setItems(mSeqUserSite, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			
				mNewPlantSiteID = mapUserSiteNameID.get(mSeqUserSite[which].toString());
				mNewPlantSiteName = mSeqUserSite[which].toString();
				
				if(mNewPlantSiteName == "Add New Site") {
					Intent intent = new Intent(PBBAddPlant.this, PBBAddSite.class);
					
					pbbItem.setCommonName(mNewPlantSpeciesName);
					pbbItem.setSpeciesID(mNewPlantSpeciesID);
					pbbItem.setProtocolID(mProtocolID);
					pbbItem.setCategory(HelperValues.LOCAL_BUDBURST_LIST);
					
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
					
					startActivity(intent);
				}
				else {
					if(mHelper.checkIfNewPlantAlreadyExists(mNewPlantSpeciesID, mNewPlantSiteID, PBBAddPlant.this)){
						Toast.makeText(PBBAddPlant.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{

						if(mHelper.insertNewMyPlantToDB(PBBAddPlant.this, mNewPlantSpeciesID, mNewPlantSpeciesName, mNewPlantSiteID, mNewPlantSiteName, mProtocolID, HelperValues.LOCAL_BUDBURST_LIST)){
							Intent intent = new Intent(PBBAddPlant.this, PBBPlantList.class);
							Toast.makeText(PBBAddPlant.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							
							//clear all stacked activities.
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(PBBAddPlant.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
	
	private void addCustomName() {
		Name_dialog = new Dialog(PBBAddPlant.this);
		
		Name_dialog.setContentView(R.layout.species_name_custom_dialog);
		Name_dialog.setTitle(getString(R.string.GetPhenophase_PBB_message));
		Name_dialog.setCancelable(true);
		Name_dialog.show();
		
		et1 = (EditText)Name_dialog.findViewById(R.id.custom_common_name);
		Button doneBtn = (Button)Name_dialog.findViewById(R.id.custom_done);
		
		doneBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String common_name = et1.getText().toString();
				
				if(common_name.equals("")) {
					common_name = "Unknown/Other";
				}
				mNewPlantSpeciesName = common_name;
				if(mSelectCategory) {
					popupDialog();
				}
				else {
					plantCategory();
				}
				Name_dialog.dismiss();
			}
		});
		
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    	staticDB.close();
	    }
	    return super.onKeyDown(keyCode, event);
	}
}