package edu.ucla.cens.budburstmobile.helper;

//list adapter used for local lists

import java.util.ArrayList;

import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.lists.ListDetail;
import edu.ucla.cens.budburstmobile.myplants.DetailPlantInfo;
import edu.ucla.cens.budburstmobile.myplants.PBBPlantList;
import edu.ucla.cens.budburstmobile.utils.PBBItems;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Lazy Adapter
 * @author kyunghan
 *
 */
public class HelperLazyAdapter extends BaseAdapter {

    private Context context;
    private Context mContext;
    private ArrayList<HelperPlantItem> localArray;
    private static LayoutInflater inflater=null;
    public HelperImageLoader imageLoader; 
    private int mPreviousActivity = 0;
	private int mPosition;
    
	public HelperLazyAdapter(Context context, ArrayList<HelperPlantItem> localArray) {
	    this.context = context;
	    this.localArray = localArray;
	    mPreviousActivity = 0;
	    mContext = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new HelperImageLoader(context.getApplicationContext());
	    // TODO Auto-generated method stub
	}
	
	//if from ADD REG or ADD_QUICK_CAPTURE
	public HelperLazyAdapter(Context context, ArrayList<HelperPlantItem> localArray, int previous) {
	    this.context = context;
	    mContext = context;
	    this.localArray = localArray;
	    mPreviousActivity = previous;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new HelperImageLoader(context.getApplicationContext());
	    // TODO Auto-generated method stub
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return localArray.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

    public static class ViewHolder{
        public TextView cname;
        public TextView sname;
        public ImageView image;
        public View thumbnail;
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        ViewHolder holder;
        if(convertView == null){
            vi = inflater.inflate(R.layout.locallist_item, null);
            holder = new ViewHolder();
            holder.cname = (TextView)vi.findViewById(R.id.common_name);
            holder.sname = (TextView)vi.findViewById(R.id.science_name);
            holder.image = (ImageView)vi.findViewById(R.id.thumbnail);
            holder.thumbnail = vi.findViewById(R.id.wrap_icon);
            
            vi.setTag(holder);
        }
        else
            holder = (ViewHolder)vi.getTag();
        
  //      mPosition = position;
        holder.thumbnail.setTag(localArray.get(position));
        holder.cname.setText(localArray.get(position).getCommonName());
        holder.sname.setText(localArray.get(position).getSpeciesName());
        holder.image.setTag(localArray.get(position).getImageURL());
        imageLoader.DisplayImage(localArray.get(position).getImageURL(), context, holder.image);
        
        
        if(mPreviousActivity == HelperValues.FROM_ADD_REG || mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE || mPreviousActivity == 0){
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				HelperPlantItem pi = (HelperPlantItem)v.getTag();
				
				if(mPreviousActivity == HelperValues.FROM_ADD_REG || mPreviousActivity == HelperValues.FROM_QUICK_CAPTURE || mPreviousActivity == 0)
				{
					Intent intent = new Intent(mContext, ListDetail.class);
					PBBItems pbbItem = new PBBItems();
					pbbItem.setSpeciesID(pi.getSpeciesID());
					pbbItem.setScienceName(pi.getSpeciesName());
					pbbItem.setCommonName(pi.getCommonName());
					pbbItem.setCategory(0);
					pbbItem.setPhenophaseID(HelperValues.NO_FOOTER);
					pbbItem.setProtocolID(pi.getProtocolID());
					intent.putExtra("pbbItem", pbbItem);
					intent.putExtra("from", HelperValues.FROM_LOCAL_PLANT_LISTS);
					mContext.startActivity(intent);
				}
			}
		});
        }
        
        return vi;
	}
}
