package edu.ucla.cens.budburstmobile.helper;


import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.lists.ListItems;
import edu.ucla.cens.budburstmobile.lists.ListLocalDownload;
import edu.ucla.cens.budburstmobile.lists.ListUserDefinedSpeciesDownload;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.AsyncTask;

/**
 * This AsyncTask performs downloading the four kinds of local lists 
 * (Local budburst, Local Invasive, Local Endangered, Local Poisonous)
 * @author kyunghan
 *
 */
public class HelperRefreshPlantLists extends AsyncTask<ListItems, Void, Void>{

	private Context mContext;
	private int mCategory;
	private NotificationManager notificationMgr = null;
	private Notification noti = null;
	private int SIMPLE_NOTFICATION_ID = HelperValues.NOTIFI_LOCAL_LISTS;
	
	public HelperRefreshPlantLists(Context context) {
		mContext = context;
	}
	
	@Override
	protected void onPreExecute() {
		notificationMgr = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
		noti = new Notification(android.R.drawable.stat_sys_download, mContext.getString(R.string.Start_Downloading_All_Lists), System.currentTimeMillis());
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, null, PendingIntent.FLAG_CANCEL_CURRENT);
		
		noti.setLatestEventInfo(mContext, mContext.getString(R.string.List_Project_Budburst_title), mContext.getString(R.string.Downloading_PlantList), contentIntent);
		
		notificationMgr.notify(SIMPLE_NOTFICATION_ID, noti);
	}
	
	@Override
	protected Void doInBackground(ListItems... item2) {
		// TODO Auto-generated method stub
		
		HelperSharedPreference hPref = new HelperSharedPreference(mContext);
		
		ListItems item = new ListItems(Double.parseDouble(hPref.getPreferenceString("latitude", "0.0")), 
				Double.parseDouble(hPref.getPreferenceString("longitude", "0.0")));
		
		ListLocalDownload listDownloadBudburst = new ListLocalDownload(mContext, HelperValues.LOCAL_BUDBURST_LIST);
		listDownloadBudburst.execute(item2);
		
		ListLocalDownload listDownloadInvasive = new ListLocalDownload(mContext, HelperValues.LOCAL_WHATSINVASIVE_LIST);
		listDownloadInvasive.execute(item2);
		
		ListLocalDownload listDownloadPoisonous = new ListLocalDownload(mContext, HelperValues.LOCAL_POISONOUS_LIST);
		listDownloadPoisonous.execute(item2);
		
		ListLocalDownload listDownloadEndangered = new ListLocalDownload(mContext, HelperValues.LOCAL_THREATENED_ENDANGERED_LIST);
		listDownloadEndangered.execute(item2);
		
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void unused) {
		
		noti = new Notification(R.drawable.pbb_icon_small, mContext.getString(R.string.Down_Downloading_All_Lists), System.currentTimeMillis());
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, null, PendingIntent.FLAG_CANCEL_CURRENT);
		
		noti.setLatestEventInfo(mContext, mContext.getString(R.string.List_Project_Budburst_title), mContext.getString(R.string.Success_Downloading_All_Lists), contentIntent);
		
		notificationMgr.notify(SIMPLE_NOTFICATION_ID, noti);
	}

}
