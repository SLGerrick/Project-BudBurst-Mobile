package edu.ucla.cens.budburstmobile.adapter;

//used only to generate lists for listUserDefinedSpecies

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.helper.HelperFunctionCalls;
import edu.ucla.cens.budburstmobile.helper.HelperPlantItem;
import edu.ucla.cens.budburstmobile.helper.HelperValues;
import edu.ucla.cens.budburstmobile.lists.ListDetail;
import edu.ucla.cens.budburstmobile.myplants.DetailPlantInfo;
import edu.ucla.cens.budburstmobile.onetime.OneTimeAddMyPlant;
import edu.ucla.cens.budburstmobile.utils.PBBItems;

public class MyListAdapterWithIndex extends ArrayAdapter implements SectionIndexer {
	private Context mContext;
	private LayoutInflater Inflater;
	private ArrayList<HelperPlantItem> items;
	private int resourceID;
	private HashMap<String, Integer> alphaIndexer;
	private String[] sections;
	private String oldChar = "";
	private int mPreviousActivity =0;

	public MyListAdapterWithIndex(Context context, int resource, ArrayList<HelperPlantItem> items){
		super(context, 0, items);
		
		resourceID = resource;
		mContext = context;
		Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = items;
		
		alphaIndexer = new HashMap<String, Integer>();
		int size = items.size();
		
		for(int i = 0 ; i < size ; i++) {
			HelperPlantItem pi = items.get(i);
			String firstChar = pi.getCommonName().substring(0,1);
			firstChar = firstChar.toUpperCase();
			
			if(oldChar.equals(firstChar)) {
				//nothing...
			}
			else {
				alphaIndexer.put(firstChar, i);
				oldChar = firstChar;
			}
		}
		
		Set<String> sectionLetters = alphaIndexer.keySet();
		
		// create a list from the set to sort
		ArrayList<String> sectionLists = new ArrayList<String>(sectionLetters);
		
		Collections.sort(sectionLists);
		
		sections = new String[sectionLists.size()];
		sectionLists.toArray(sections);
	}
	
	public MyListAdapterWithIndex(Context context, int resource, ArrayList<HelperPlantItem> items, int previous){
		super(context, 0, items);
		
		mPreviousActivity = previous;
		resourceID = resource;
		mContext = context;
		Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = items;
		
		alphaIndexer = new HashMap<String, Integer>();
		int size = items.size();
		
		for(int i = 0 ; i < size ; i++) {
			HelperPlantItem pi = items.get(i);
			String firstChar = pi.getCommonName().substring(0,1);
			firstChar = firstChar.toUpperCase();
			
			if(oldChar.equals(firstChar)) {
				//nothing...
			}
			else {
				alphaIndexer.put(firstChar, i);
				oldChar = firstChar;
			}
		}
		
		Set<String> sectionLetters = alphaIndexer.keySet();
		
		// create a list from the set to sort
		ArrayList<String> sectionLists = new ArrayList<String>(sectionLetters);
		
		Collections.sort(sectionLists);
		
		sections = new String[sectionLists.size()];
		sectionLists.toArray(sections);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		if(convertView == null)
			convertView = Inflater.inflate(resourceID, parent, false);
		
		ImageView img = (ImageView)convertView.findViewById(R.id.icon);
		String imagePath = HelperValues.TREE_PATH + items.get(position).getSpeciesID() + ".jpg";
		Log.i("K", "imagePath : " + imagePath);
		
		HelperFunctionCalls helper = new HelperFunctionCalls();
		img.setImageBitmap(helper.showImage(mContext, imagePath));

		TextView cname = (TextView)convertView.findViewById(R.id.commonname);
		cname.setText(items.get(position).getCommonName());
		
		TextView sname = (TextView)convertView.findViewById(R.id.speciesname);
		sname.setText(items.get(position).getSpeciesName());
		
		// we don' need to show the stat of phenophase in this view
		TextView phenoStat = (TextView)convertView.findViewById(R.id.pheno_stat);
		TextView phenoObsr = (TextView)convertView.findViewById(R.id.pheno_observed);
		
		phenoStat.setTextSize(12);
		phenoStat.setText(helper.getProtocolStr(items.get(position).getProtocolID()) + " ");
		phenoStat.setTextAppearance(mContext, Typeface.ITALIC);
		//phenoStat.setVisibility(View.GONE);
		phenoObsr.setVisibility(View.GONE);
		
		/*
		 *  Call View from the xml and link the view to current position.
		 */
		
		View thumbnail = convertView.findViewById(R.id.wrap_icon);
	//	thumbnail.set
		thumbnail.setTag(items.get(position));
		
		if(mPreviousActivity == HelperValues.FROM_ADD_REG || mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE){
			
			thumbnail.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					HelperPlantItem pi = (HelperPlantItem)v.getTag();
					
					Toast.makeText(mContext, "testing species name: "+pi.getSpeciesName(), Toast.LENGTH_SHORT );
					
					Intent intent = new Intent(mContext, ListDetail.class);
					PBBItems pbbItem = new PBBItems();
					pbbItem.setSpeciesID(pi.getSpeciesID());
					pbbItem.setCommonName(pi.getCommonName());				
					pbbItem.setCategory(pi.getCategory());
					pbbItem.setProtocolID(pi.getProtocolID());
					pbbItem.setPhenophaseID(HelperValues.NO_FOOTER);
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", HelperValues.FROM_USER_DEFINED_LISTS);
			//		intent.putExtra("footer", 0);
					mContext.startActivity(intent);
					
				}
			});
		}
		
		return convertView;
	}

	@Override
	public int getPositionForSection(int section) {
		// TODO Auto-generated method stub
		return alphaIndexer.get(sections[section]);
	}

	@Override
	public int getSectionForPosition(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object[] getSections() {
		// TODO Auto-generated method stub
		return sections;
	}
}
