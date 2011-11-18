package edu.ucla.cens.budburstmobile.onetime;

//List for Budburst Plants

import java.io.File;
import edu.ucla.cens.budburstmobile.adapter.MyListAdapterMainPage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.ucla.cens.budburstmobile.PBBHelpPage;
import edu.ucla.cens.budburstmobile.PBBSync;
import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.adapter.MyListAdapter;
import edu.ucla.cens.budburstmobile.adapter.MyListAdapterMainPage;
import edu.ucla.cens.budburstmobile.database.OneTimeDBHelper;
import edu.ucla.cens.budburstmobile.database.StaticDBHelper;
import edu.ucla.cens.budburstmobile.database.SyncDBHelper;
import edu.ucla.cens.budburstmobile.helper.HelperDrawableManager;
import edu.ucla.cens.budburstmobile.helper.HelperFunctionCalls;
import edu.ucla.cens.budburstmobile.helper.HelperListItem;
import edu.ucla.cens.budburstmobile.helper.HelperPlantItem;
import edu.ucla.cens.budburstmobile.helper.HelperSettings;
import edu.ucla.cens.budburstmobile.helper.HelperValues;
import edu.ucla.cens.budburstmobile.lists.ListDetail;
import edu.ucla.cens.budburstmobile.lists.ListGroupItem;
import edu.ucla.cens.budburstmobile.lists.ListUserDefinedSpecies;
import edu.ucla.cens.budburstmobile.myplants.PBBAddNotes;
import edu.ucla.cens.budburstmobile.myplants.PBBAddPlant;
import edu.ucla.cens.budburstmobile.myplants.PBBAddSite;
import edu.ucla.cens.budburstmobile.myplants.PBBPlantList;
import edu.ucla.cens.budburstmobile.utils.PBBItems;
import edu.ucla.cens.budburstmobile.utils.QuickCapture;

public class OneTimePBBLists extends ListActivity{
	private ArrayList<HelperPlantItem> arPlantList;
	
	private StaticDBHelper staticDBHelper = null;
	private SQLiteDatabase staticDB = null;
	private MyListAdapter mylistapdater = null;
	private ListView MyList = null;
	
	private ArrayList<ListGroupItem> mArr = new ArrayList<ListGroupItem>();
	private MyListAdapterMainPage mylistapdater2;
	private boolean isUserDefinedListOn = true;
	private boolean isEmpty = false;
	
	
	
	private Button topBtn1 = null;
	private Button topBtn2 = null;
	private Button topBtn3 = null;
	private Button topBtn4 = null;
	private EditText et1 = null;
	private Dialog dialog = null;
	
	
	//MENU contents
	final private int MENU_ADD_PLANT = 1;
	final private int MENU_ADD_QC_PLANT = 2;
	final private int MENU_SYNC = 6;
	final private int MENU_HELP = 7;
	
	//private TextView header = null;
	private TextView myTitleText = null;
	
	private int mCurrentPosition = 0;
	private int mPhenoID = 0;
	private int mPreviousActivity;
	private int mProtocolID = 0;
	private int mCategory = 0;
	private int mSpeciesID = 0;
	private HelperFunctionCalls mHelper;
	private HashMap<String, Integer> mMapUserSiteNameID = new HashMap<String, Integer>();
	private String []itemArray;
	private String mImageID = null;
	private CharSequence[] mSeqUserSite;
	private String mSpeciesName;
	
	private int mNewPlantSpeciesID;
	private String mNewPlantSpeciesName;
	private HashMap<String, Integer> mapUserSiteNameID = new HashMap<String, Integer>();
	

	
	private String mCommonName = "Unknown/Other";
	
	private PBBItems pbbItem;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.flora_observer);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.pbb_title);
		
		ViewGroup v = (ViewGroup) findViewById(R.id.title_bar).getParent().getParent();
		v = (ViewGroup)v.getChildAt(0);
		v.setPadding(0, 0, 0, 0);

		myTitleText = (TextView) findViewById(R.id.my_title);
		myTitleText.setText(getString(R.string.AddPlant_top10));
		
		Bundle bundle = getIntent().getExtras();
		pbbItem = bundle.getParcelable("pbbItem");
		mPhenoID = pbbItem.getPhenophaseID();
		mPreviousActivity = bundle.getInt("from");
		
		topBtn1 = (Button)findViewById(R.id.option1);
		topBtn2 = (Button)findViewById(R.id.option2);
		topBtn3 = (Button)findViewById(R.id.option3);
		topBtn4 = (Button)findViewById(R.id.option4);
		
		topBtn1.setOnClickListener(radio_listener);
		topBtn2.setOnClickListener(radio_listener);
		topBtn3.setOnClickListener(radio_listener);
		topBtn4.setOnClickListener(radio_listener);
		
		//helper stuff
		// Call FunctionsHelper();
		mHelper = new HelperFunctionCalls();		
		mMapUserSiteNameID = mHelper.getUserSiteIDMap(OneTimePBBLists.this);
		mapUserSiteNameID = mHelper.getUserSiteIDMap(this);
		
		//Check if site table is empty
		staticDBHelper = new StaticDBHelper(OneTimePBBLists.this);
		staticDB = staticDBHelper.getReadableDatabase();
		 
		arPlantList = new ArrayList<HelperPlantItem>();
 		Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species ORDER BY common_name;",null);
 		while(cursor.moveToNext()){
			Integer id = cursor.getInt(0);
			if(id == 70 || id == 69 || id == 45 || id == 59 || id == 60 || id == 19 || id == 32 || id == 34 || id == 24) {
				String species_name = cursor.getString(1);
				String common_name = cursor.getString(2);
				int protocol_id = cursor.getInt(3);
				int resID = getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/s"+id, null, null);
				
				HelperPlantItem pi;
				//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
				pi = new HelperPlantItem();
				pi.setPicture(resID);
				pi.setCommonName(common_name);
				pi.setSpeciesName(species_name);
				pi.setSpeciesID(id);
				pi.setCategory(HelperValues.LOCAL_BUDBURST_LIST);
				pi.setProtocolID(protocol_id);
				arPlantList.add(pi);
			}
		}
 		
		// add plant at the last.
		HelperPlantItem pi = new HelperPlantItem();
		pi.setPicture(getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/pbb_icon_main2", null, null));
		pi.setCommonName("Unknown/Other");
		pi.setProtocolID(HelperValues.LOCAL_BUDBURST_LIST);
		pi.setCategory(HelperValues.LOCAL_BUDBURST_LIST);
		pi.setSpeciesName("Unknown/Other");
		pi.setSpeciesID(999);
		arPlantList.add(pi);
		if(mPreviousActivity == HelperValues.FROM_ADD_REG || mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE)
			mylistapdater = new MyListAdapter(OneTimePBBLists.this, R.layout.plantlist_item2, arPlantList);
		else 
			//from Local Budburst, so that ListDetail will work correctly
			mylistapdater = new MyListAdapter(OneTimePBBLists.this, R.layout.plantlist_item2, arPlantList, HelperValues.FROM_LOCAL_PLANT_LISTS);
		MyList = getListView(); 
		MyList.setAdapter(mylistapdater);
		
		//Close DB and cursor
		staticDB.close();
		cursor.close();
		
	}

	private OnClickListener radio_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			staticDBHelper = new StaticDBHelper(OneTimePBBLists.this);
			staticDB = staticDBHelper.getReadableDatabase();
			
			if(v == topBtn1) {
				//header.setText("'TOP 10' list of the plants.");
				myTitleText.setText(getString(R.string.AddPlant_top10));
				arPlantList = new ArrayList<HelperPlantItem>();
		 		Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species ORDER BY common_name;", null);
				while(cursor.moveToNext()){
					Integer id = cursor.getInt(0);
					if(id == 70 || id == 69 || id == 45 || id == 59 || id == 60 || id == 19 || id == 32 || id == 34 || id == 24) {
						String species_name = cursor.getString(1);
						String common_name = cursor.getString(2);
						int protocol_id = cursor.getInt(3);
									
						int resID = getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/s"+id, null, null);
						
						HelperPlantItem pi;
						//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
						pi = new HelperPlantItem();
						pi.setPicture(resID);
						pi.setCommonName(common_name);
						pi.setSpeciesName(species_name);
						pi.setSpeciesID(id);
						pi.setProtocolID(protocol_id);
						arPlantList.add(pi);
					}
				}
				
				// add plant at the last.
				HelperPlantItem pi = new HelperPlantItem();
				pi.setPicture(getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/pbb_icon_main2", null, null));
				pi.setCommonName("Unknown/Other");
				pi.setSpeciesName("Unknown/Other");
				pi.setSpeciesID(999);
				arPlantList.add(pi);
				
				mylistapdater = new MyListAdapter(OneTimePBBLists.this, R.layout.plantlist_item2, arPlantList);
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				cursor.close();
				
			}
			else if (v == topBtn2) {
				//header.setText("'ALL' list of the plants.");
				myTitleText.setText(getString(R.string.AddPlant_all));
				//Rereive syncDB and add them to arUserPlatList arraylist
				arPlantList = new ArrayList<HelperPlantItem>();
		 		Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species ORDER BY common_name;", null);
				while(cursor.moveToNext()){
					Integer id = cursor.getInt(0);
				
					String species_name = cursor.getString(1);
					String common_name = cursor.getString(2);
					int protocol_id = cursor.getInt(3);
									
					int resID = getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/s"+id, null, null);
						
					HelperPlantItem pi;
					//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
					pi = new HelperPlantItem();
					pi.setPicture(resID);
					pi.setCommonName(common_name);
					pi.setSpeciesName(species_name);
					pi.setSpeciesID(id);
					pi.setProtocolID(protocol_id);
					arPlantList.add(pi);
				}
				
				// add plant at the last.
				HelperPlantItem pi = new HelperPlantItem();
				pi.setPicture(getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/pbb_icon_main2", null, null));
				pi.setCommonName("Unknown/Other");
				pi.setSpeciesName("Unknown/Other");
				pi.setSpeciesID(999);
				arPlantList.add(pi);
				
				mylistapdater = new MyListAdapter(OneTimePBBLists.this, R.layout.plantlist_item2, arPlantList);
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				cursor.close();
			}
			else if(v == topBtn3){
				//header.setText("By Group.");
				new AlertDialog.Builder(OneTimePBBLists.this)
				.setTitle("Select Category")
				.setIcon(android.R.drawable.ic_menu_more)
				.setItems(R.array.category, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String[] category = getResources().getStringArray(R.array.category);
						StaticDBHelper staticDBHelper = new StaticDBHelper(OneTimePBBLists.this);
						SQLiteDatabase staticDB = staticDBHelper.getReadableDatabase(); 
						
						arPlantList = new ArrayList<HelperPlantItem>();
						Cursor cursor = null;

						if(category[which].equals("Wild Flowers and Herbs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + HelperValues.WILD_FLOWERS + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addFlowers));
						}
						else if(category[which].equals("Grass")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + HelperValues.GRASSES + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addGrass));
						}
						else if(category[which].equals("Deciduous Trees and Shrubs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + HelperValues.DECIDUOUS_TREES + " OR protocol_id=" + HelperValues.DECIDUOUS_TREES_WIND + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addDecid));
						}
						else if(category[which].equals("Evergreen Trees and Shrubs")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + HelperValues.EVERGREEN_TREES + " OR protocol_id=" + HelperValues.EVERGREEN_TREES_WIND + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addEvergreen));
						}
						else if(category[which].equals("Conifer")) {
							cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species WHERE protocol_id=" + HelperValues.CONIFERS + " ORDER BY common_name;",null);
							myTitleText.setText(getString(R.string.AddPlant_addConifer));
						}
						else {
						}
						
						//header.setText(" " + category[which]);
						while(cursor.moveToNext()){
							Integer id = cursor.getInt(0);
							String species_name = cursor.getString(1);
							String common_name = cursor.getString(2);
							Integer protocol_id = cursor.getInt(3);
										
							HelperPlantItem pi;
							
							int resID = getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/s"+id, null, null);
							
							//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
							pi = new HelperPlantItem();
							pi.setPicture(resID);
							pi.setCommonName(common_name);
							pi.setSpeciesName(species_name);
							pi.setSpeciesID(id);
							pi.setProtocolID(protocol_id);
							arPlantList.add(pi);
						}
						
						// add plant at the last.
						HelperPlantItem pi = new HelperPlantItem();
						pi.setPicture(getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/pbb_icon_main2", null, null));
						pi.setCommonName("Unknown/Other");
						pi.setSpeciesName("Unknown/Other");
						pi.setSpeciesID(999);
						arPlantList.add(pi);
						
						mylistapdater = new MyListAdapter(OneTimePBBLists.this, R.layout.plantlist_item2, arPlantList);
						MyList = getListView(); 
						MyList.setAdapter(mylistapdater);
						
						cursor.close();
						staticDB.close();
						staticDBHelper.close();
						
					}
				})
				.setNegativeButton("Back", null)
				.show();
				
			}
			
			else {
				myTitleText.setText(" " + getString(R.string.AddPlant_local));
				
				arPlantList = new ArrayList<HelperPlantItem>();
				
				OneTimeDBHelper otDBH = new OneTimeDBHelper(OneTimePBBLists.this);
				SQLiteDatabase otDB = otDBH.getReadableDatabase();
				Cursor cursor = staticDB.rawQuery("SELECT _id, species_name, common_name, protocol_id FROM species ORDER BY common_name;", null);
				if(cursor.equals(null)|| cursor.getCount()==0){
					Toast.makeText(OneTimePBBLists.this, "Please Download Lists", Toast.LENGTH_SHORT).show();
				}
				//Toast.makeText(OneTimePBBLists.this, "Cursor Count: "+cursor.getCount(), Toast.LENGTH_SHORT).show();
				HelperListItem iItem = new HelperListItem();
				ArrayList<HelperListItem> listArr = new ArrayList<HelperListItem>();
				mArr = otDBH.getListGroupItem(OneTimePBBLists.this);

				
				while(cursor.moveToNext()) {
					String sName = cursor.getString(1);
					
					Cursor cursor2 = otDB.rawQuery("SELECT science_name FROM localPlantLists WHERE category=1 AND science_name=\"" + sName + "\"", null);
					if(cursor2.getCount() > 0) {
						int resID = getResources().getIdentifier("edu.ucla.cens.budburstmobile:drawable/s"+cursor.getInt(0), null, null);
						
						String species_name = cursor.getString(1);
						String common_name = cursor.getString(2);
						int protocol_id = cursor.getInt(3);
						
						HelperPlantItem pi;
						//pi = aPicture, String aCommonName, String aSpeciesName, int aSpeciesID
						pi = new HelperPlantItem();
						pi.setPicture(resID);
						pi.setCommonName(common_name);
						pi.setSpeciesName(species_name);
						pi.setProtocolID(protocol_id);
						pi.setSpeciesID(cursor.getInt(0));
						arPlantList.add(pi);
					}
					
					cursor2.close();
				}
				

				
				
				otDBH.close();
				otDB.close();
				if(arPlantList.equals(null) || arPlantList.isEmpty()) {
					iItem = new HelperListItem();
					iItem.setHeaderText(getString(R.string.List_User_Plant_Header));
					iItem.setTitle("No list yet");
					iItem.setImageURL("yellow_triangle_exclamation50");
					iItem.setDescription("Please download the user defined lists. Menu->Settings->Download User Defined List");
					listArr.add(iItem);
					mylistapdater2 = new MyListAdapterMainPage(OneTimePBBLists.this, R.layout.onetime_list ,listArr);
					ListView MyList = getListView();
					MyList.setAdapter(mylistapdater2);
					isEmpty = true;
				}
				else{
				mylistapdater = new MyListAdapter(OneTimePBBLists.this, R.layout.plantlist_item2, arPlantList);
				isEmpty = false;
				MyList = getListView(); 
				MyList.setAdapter(mylistapdater);
				}
				cursor.close();
			
			}
			
			staticDBHelper.close();
		}
	};
	
	
	/**
	 * Menu option(non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		super.onCreateOptionsMenu(menu);
	//	menu.add(0, MENU_ADD_PLANT, 0, getString(R.string.Menu_addPlant)).setIcon(android.R.drawable.ic_menu_add);
	//	menu.add(0, MENU_ADD_QC_PLANT, 0, getString(R.string.Menu_addQCPlant)).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, MENU_SYNC, 0, getString(R.string.Menu_sync)).setIcon(R.drawable.ic_menu_refresh);
		menu.add(0, MENU_HELP, 0, getString(R.string.Menu_help)).setIcon(android.R.drawable.ic_menu_help);
			
		return true;
	}
	
	/**
	 * Menu option selection handling(non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	public boolean onOptionsItemSelected(MenuItem item){
		Intent intent;
		switch(item.getItemId()){
			case MENU_ADD_PLANT:
				intent = new Intent(OneTimePBBLists.this, OneTimeMainPage.class);
				pbbItem = new PBBItems();
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
				startActivity(intent);
				return true;
			case MENU_ADD_QC_PLANT:
				/*
				 * Ask users if they are ready to take a photo.
				 */
				new AlertDialog.Builder(OneTimePBBLists.this)
				.setTitle(getString(R.string.Menu_addQCPlant))
				.setMessage(getString(R.string.Start_Shared_Plant))
				.setPositiveButton(getString(R.string.Button_Photo), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						/*
						 * Move to QuickCapture
						 */
						Intent intent = new Intent(OneTimePBBLists.this, QuickCapture.class);
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
						Intent intent = new Intent(OneTimePBBLists.this, OneTimeMainPage.class);
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

				return true;
			case MENU_SYNC:
				intent = new Intent(OneTimePBBLists.this, PBBSync.class);
				intent.putExtra("sync_instantly", true);
				intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
				startActivity(intent);
				finish();
				return true;
			case MENU_HELP:
				intent = new Intent(OneTimePBBLists.this, PBBHelpPage.class);
				intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
				startActivity(intent);
				return true;
		}
		return false;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id){

		mCurrentPosition = position;
		
		if(isEmpty){
			Intent intent = new Intent(OneTimePBBLists.this, HelperSettings.class);
			intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
			startActivity(intent);
		}
		
		/*
		 * If user chooses Unknown/Plant, show the popup dialog adding common name.
		 */
		else
		{
		if(arPlantList.get(position).getSpeciesID() == HelperValues.UNKNOWN_SPECIES) {
			dialog = new Dialog(OneTimePBBLists.this);
			
			dialog.setContentView(R.layout.species_name_custom_dialog);
			dialog.setTitle(getString(R.string.GetPhenophase_PBB_message));
			dialog.setCancelable(true);
			dialog.show();
			
			et1 = (EditText)dialog.findViewById(R.id.custom_common_name);
			Button doneBtn = (Button)dialog.findViewById(R.id.custom_done);
			doneBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mCommonName = et1.getText().toString();
					if(mCommonName.equals("")) {
						mCommonName = "Unknown/Other";
					}
					
					if(mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE || mPreviousActivity == HelperValues.FROM_PLANT_LIST) 
					{
						
						new AlertDialog.Builder(OneTimePBBLists.this)
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
								
								Intent intent = new Intent(OneTimePBBLists.this, OneTimePhenophase.class);
								
								pbbItem.setCommonName(mCommonName);
								pbbItem.setScienceName("Unknown/Other");
								pbbItem.setProtocolID(mProtocolID);
								pbbItem.setPhenophaseID(mPhenoID);
								pbbItem.setSpeciesID(HelperValues.UNKNOWN_SPECIES);
								
								intent.putExtra("pbbItem", pbbItem);
								intent.putExtra("from", HelperValues.FROM_QUICK_CAPTURE);
								startActivity(intent);
							}
						})
						.setNegativeButton("Back", null)
						.show();
					}
					else {
						if(mPreviousActivity == HelperValues.FROM_ADD_REG){
							mCategory = arPlantList.get(mCurrentPosition).getCategory();
							
							mSpeciesID = arPlantList.get(mCurrentPosition).getSpeciesID();
							showProtocolDialog(mCurrentPosition);
						}
						else
						{
							Intent intent = new Intent(OneTimePBBLists.this, PBBAddNotes.class);
	
							pbbItem.setCommonName(mCommonName);
							pbbItem.setScienceName("Unknown/Other");
							pbbItem.setProtocolID(arPlantList.get(mCurrentPosition).getProtocolID());
							pbbItem.setPhenophaseID(mPhenoID);
							pbbItem.setSpeciesID(HelperValues.UNKNOWN_SPECIES);
							
							pbbItem.setCategory(HelperValues.TABLE_BUDBURSTS);
							
							intent.putExtra("pbbItem", pbbItem);
							intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
							startActivity(intent);
						}
					}
					
					dialog.dismiss();					
				}
			});
		}
		
		
		/*
		 * If user chooses one of the official species..
		 */
		else {
			
			if(mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE || mPreviousActivity == HelperValues.FROM_PLANT_LIST) 
			{
				
				int getSpeciesID = arPlantList.get(position).getSpeciesID();
				int getProtocolID = 2;
				
				StaticDBHelper staticDBH = new StaticDBHelper(OneTimePBBLists.this);
				SQLiteDatabase staticDB = staticDBH.getReadableDatabase();
				
				/*
				 * We already know the Shared Plant category if we choose species from official budburst lists.
				 * - hardcoded protocolID
				 */
				
				Cursor cursor = staticDB.rawQuery("SELECT protocol_id FROM species WHERE _id=" + getSpeciesID + ";", null);
				while(cursor.moveToNext()) {
					getProtocolID = cursor.getInt(0);
				}
				
				switch(getProtocolID) {
				case HelperValues.WILD_FLOWERS:
					mProtocolID = HelperValues.QUICK_WILD_FLOWERS; 
					break;
				case HelperValues.DECIDUOUS_TREES:
					mProtocolID = HelperValues.QUICK_TREES_AND_SHRUBS;
					break;
				case HelperValues.DECIDUOUS_TREES_WIND:
					mProtocolID = HelperValues.QUICK_TREES_AND_SHRUBS;
					break;
				case HelperValues.EVERGREEN_TREES:
					mProtocolID = HelperValues.QUICK_TREES_AND_SHRUBS;
					break;
				case HelperValues.EVERGREEN_TREES_WIND:
					mProtocolID = HelperValues.QUICK_TREES_AND_SHRUBS;
					break;
				case HelperValues.CONIFERS:
					mProtocolID = HelperValues.QUICK_TREES_AND_SHRUBS;
					break;
				case HelperValues.GRASSES:
					mProtocolID = HelperValues.QUICK_GRASSES;
					break;
				}
				
				cursor.close();
				staticDB.close();
				if(mPreviousActivity == HelperValues.FROM_PLANT_LIST) {
					Intent intent = new Intent(OneTimePBBLists.this, ListDetail.class);
					
					pbbItem.setCommonName(arPlantList.get(mCurrentPosition).getCommonName());
					pbbItem.setScienceName(arPlantList.get(mCurrentPosition).getSpeciesName());
					pbbItem.setProtocolID(mProtocolID);
					pbbItem.setPhenophaseID(mPhenoID);
					pbbItem.setSpeciesID(arPlantList.get(mCurrentPosition).getSpeciesID());
					pbbItem.setCategory(HelperValues.LOCAL_BUDBURST_LIST);
					Log.d("---------TEST--------", "Plant list, ");
					Log.d("---------TEST--------", "Plant list, "+mPhenoID+" "+arPlantList.get(mCurrentPosition).getCommonName());
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
					
					startActivity(intent);
				}
				
			}
			else if(mPreviousActivity == HelperValues.FROM_ADD_REG) {
				mCategory = arPlantList.get(mCurrentPosition).getCategory();
				
				mSpeciesID = arPlantList.get(mCurrentPosition).getSpeciesID();
	//			if(mCategory == HelperValues.LOCAL_BUDBURST_LIST) {
	/*				StaticDBHelper staticDBHelper = new StaticDBHelper(OneTimePBBLists.this);
					SQLiteDatabase staticDB2 = staticDBHelper.getReadableDatabase();
					Cursor c = staticDB.rawQuery("SELECT protocol_id FROM species WHERE _id = " + mSpeciesID, null);
					while(c.moveToNext()) {
						mProtocolID = c.getInt(0);
					}
					c.close();
					staticDB2.close();
			*/		
					popupDialog(mCurrentPosition);
	//			}
	//			else {
	//				showProtocolDialog();
	//			}
				
	//			startActivity(intent);
			}
			if(mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE){
				Intent intent = new Intent(OneTimePBBLists.this, OneTimePhenophase.class);
				
				pbbItem.setCommonName(arPlantList.get(mCurrentPosition).getCommonName());
				pbbItem.setScienceName(arPlantList.get(mCurrentPosition).getSpeciesName());
				pbbItem.setProtocolID(mProtocolID);
				pbbItem.setPhenophaseID(mPhenoID);
				pbbItem.setSpeciesID(arPlantList.get(mCurrentPosition).getSpeciesID());
				pbbItem.setCategory(HelperValues.LOCAL_BUDBURST_LIST);
				
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", HelperValues.FROM_QUICK_CAPTURE);
				startActivity(intent);
			}
/*			else {
				Intent intent = new Intent(OneTimePBBLists.this, PBBAddNotes.class);
				
				pbbItem.setCommonName(arPlantList.get(position).getCommonName());
				pbbItem.setScienceName(arPlantList.get(position).getSpeciesName());
				pbbItem.setProtocolID(arPlantList.get(position).getProtocolID());
				pbbItem.setPhenophaseID(mPhenoID);
				pbbItem.setSpeciesID(arPlantList.get(position).getSpeciesID());
				pbbItem.setCategory(HelperValues.LOCAL_BUDBURST_LIST);
				
				intent.putExtra("pbbItem", pbbItem);
				intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
				startActivity(intent);
			}
			*/
		}
		
		}
	}
	
	private void popupDialog(int position) {
		//Pop up choose site dialog box
		mNewPlantSpeciesID = arPlantList.get(position).getSpeciesID();
		mNewPlantSpeciesName = arPlantList.get(position).getCommonName();
		mSpeciesName = arPlantList.get(position).getSpeciesName();
		mSpeciesID = arPlantList.get(position).getSpeciesID();
		mCommonName = mNewPlantSpeciesName;
		mSeqUserSite = mHelper.getUserSite(OneTimePBBLists.this);
		mCategory = arPlantList.get(position).getCategory();
		mImageID = arPlantList.get(position).getImageURL();
		
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
					Intent intent = new Intent(OneTimePBBLists.this, PBBAddSite.class);
					pbbItem.setSpeciesID(mSpeciesID);
					pbbItem.setCommonName(arPlantList.get(mCurrentPosition).getCommonName());
					pbbItem.setProtocolID(arPlantList.get(mCurrentPosition).getProtocolID());
					
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", HelperValues.FROM_PLANT_LIST);
					
					startActivity(intent);
				}
				else {
					if(mHelper.checkIfNewPlantAlreadyExists(mNewPlantSpeciesID, 
							new_plant_site_id, OneTimePBBLists.this)){
						Toast.makeText(OneTimePBBLists.this, getString(R.string.AddPlant_alreadyExists), Toast.LENGTH_LONG).show();
					}else{
						if(mHelper.insertNewMyPlantToDB(OneTimePBBLists.this, 
								arPlantList.get(mCurrentPosition).getSpeciesID(),
								arPlantList.get(mCurrentPosition).getCommonName(),									
								new_plant_site_id, 
								new_plant_site_name, 
								arPlantList.get(mCurrentPosition).getProtocolID(), 	
								1										 											
								)){
							Intent intent = new Intent(OneTimePBBLists.this, PBBPlantList.class);
							Toast.makeText(OneTimePBBLists.this, getString(R.string.AddPlant_newAdded), Toast.LENGTH_SHORT).show();
							//clear all stacked activities.
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}else{
							Toast.makeText(OneTimePBBLists.this, getString(R.string.Alert_dbError), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		})
		.setNegativeButton(getString(R.string.Button_cancel), null)
		.show();
	}
	
	
	
	
	private void showProtocolDialog(int position) {
		
		mCurrentPosition = position;
		if(mCategory >= HelperValues.USER_DEFINED_TREE_LISTS) {
			//itemArray = getResources().getStringArray(R.array.category_only_trees);
			popupDialog(position);
		}
		else {
			itemArray = getResources().getStringArray(R.array.category);
			
			
			new AlertDialog.Builder(OneTimePBBLists.this)
			.setTitle(getString(R.string.AddPlant_SelectCategory))
			.setIcon(android.R.drawable.ic_menu_more)
			.setItems(itemArray, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String[] category = itemArray;
					StaticDBHelper staticDBHelper = new StaticDBHelper(OneTimePBBLists.this);
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
					
					popupDialog(mCurrentPosition);
					
				}
			}).show();
		}
	}
	
}
