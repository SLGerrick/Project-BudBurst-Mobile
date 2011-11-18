package edu.ucla.cens.budburstmobile.adapter;

import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.helper.HelperPlantItem;
import edu.ucla.cens.budburstmobile.helper.HelperValues;
import edu.ucla.cens.budburstmobile.myplants.DetailPlantInfo;
import edu.ucla.cens.budburstmobile.utils.PBBItems;
import edu.ucla.cens.budburstmobile.lists.ListDetail;

public class MyListAdapter extends BaseAdapter{
	private Context mContext;
	private int mPreviousActivity = 0;
	private LayoutInflater mInflater;
	private ArrayList<HelperPlantItem> mArr;
	private int mLayout;
	
	public MyListAdapter(Context context, int alayout, ArrayList<HelperPlantItem> aarSrc){
		mContext = context;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mArr = aarSrc;
		mLayout = alayout;
		mPreviousActivity = 0;
	}
	
	public MyListAdapter(Context context, int alayout, ArrayList<HelperPlantItem> aarSrc, int previous){
		mContext = context;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mArr = aarSrc;
		mLayout = alayout;
		mPreviousActivity = previous;
	}
	
	public int getCount(){
		return mArr.size();
	}
	
	public String getItem(int position){
		return mArr.get(position).getCommonName();
	}
	
	public long getItemId(int position){
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent){
		if(convertView == null)
			convertView = mInflater.inflate(mLayout, parent, false);
	
		//current_position = arSrc.get(position).SpeciesID;
		
		ImageView img = (ImageView)convertView.findViewById(R.id.icon);
		img.setImageResource(mArr.get(position).getPicture());

		TextView textname = (TextView)convertView.findViewById(R.id.commonname);
		textname.setText(mArr.get(position).getCommonName());
		
		TextView textdesc = (TextView)convertView.findViewById(R.id.speciesname);
		
		if(mArr.get(position).getSpeciesName().equals("Unknown/Other")) {
			textdesc.setText("Unknown/Other");
		}
		else {
			String [] splits = mArr.get(position).getSpeciesName().split(" ");
			textdesc.setText(splits[0] + " " + splits[1]);
		}
		
		// call View from the xml and link the view to current position.
		View thumbnail = convertView.findViewById(R.id.wrap_icon);
		thumbnail.setTag(mArr.get(position));
		if(mPreviousActivity == HelperValues.FROM_ADD_REG || mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE || mPreviousActivity == 0){
		thumbnail.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				HelperPlantItem pi = (HelperPlantItem)v.getTag();
				
				if(mPreviousActivity == HelperValues.FROM_ADD_REG || mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE || mPreviousActivity == 0)
				{
					Intent intent = new Intent(mContext, DetailPlantInfo.class);
					PBBItems pbbItem = new PBBItems();
					pbbItem.setSpeciesID(pi.getSpeciesID());
					pbbItem.setCommonName(pi.getCommonName());
					pbbItem.setCategory(pi.getCategory());
					pbbItem.setProtocolID(pi.getProtocolID());
					intent.putExtra("pbbItem", pbbItem);
					mContext.startActivity(intent);
				}
				else {
					Intent intent = new Intent(mContext, ListDetail.class);
					PBBItems pbbItem = new PBBItems();
					pbbItem.setSpeciesID(pi.getSpeciesID());
					pbbItem.setCommonName(pi.getCommonName());
					pbbItem.setScienceName(pi.getSpeciesName());
					pbbItem.setCategory(pi.getCategory());
					pbbItem.setPhenophaseID(1);
					pbbItem.setProtocolID(pi.getProtocolID());
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", mPreviousActivity);
					mContext.startActivity(intent);
				}
			}
		});
		}
		return convertView;
	}
}
