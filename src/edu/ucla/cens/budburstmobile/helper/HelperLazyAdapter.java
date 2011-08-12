package edu.ucla.cens.budburstmobile.helper;

import java.util.ArrayList;

import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.lists.ListDetail;
import edu.ucla.cens.budburstmobile.myplants.DetailPlantInfo;
import edu.ucla.cens.budburstmobile.myplants.PBBPlantList;
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
    private ArrayList<HelperPlantItem> localArray;
    private static LayoutInflater inflater=null;
    public HelperImageLoader imageLoader; 
	
	public HelperLazyAdapter(Context context, ArrayList<HelperPlantItem> localArray) {
	    this.context = context;
	    this.localArray = localArray;
	    
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
        
        holder.cname.setText(localArray.get(position).getCommonName());
        holder.sname.setText(localArray.get(position).getSpeciesName());
        holder.image.setTag(localArray.get(position).getImageURL());
        imageLoader.DisplayImage(localArray.get(position).getImageURL(), context, holder.image);
        
        return vi;
	}
}
