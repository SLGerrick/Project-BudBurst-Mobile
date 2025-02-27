package edu.ucla.cens.budburstmobile.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import edu.ucla.cens.budburstmobile.R;
import edu.ucla.cens.budburstmobile.lists.ListUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class HelperDrawableManager {
	private static final int COMPLETE = 0;
	private static final int IN_COMPLETE = 1;
	private ProgressBar mSpinner;
	private ImageView mImageView;
	private String mUrl;
	private File cacheDir;
	private final HashMap<String, Bitmap> drawableMap;
	private final Handler handler = new Handler() {
		public void handleMessage(Message message) {
			
			switch(message.what) {
			case COMPLETE:
				//mImageView.setImageDrawable((Drawable) message.obj);
				mImageView.setImageBitmap((Bitmap) message.obj);
				mImageView.setVisibility(View.VISIBLE);
				mSpinner.setVisibility(View.GONE);
				break;
			case IN_COMPLETE:
				mImageView.setImageResource(R.drawable.no_photo);
				mImageView.setVisibility(View.VISIBLE);
				mSpinner.setVisibility(View.GONE);
				break;
			}
		}
	};
	
	public HelperDrawableManager(Context context, ProgressBar spinner, ImageView imageView) {
		mSpinner = spinner;
		mImageView = imageView;
		drawableMap = new HashMap<String, Bitmap>();
		
		mSpinner.setVisibility(View.VISIBLE);
		mImageView.setVisibility(View.GONE);
		
		//Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"LazyList");
        else {
        	cacheDir=context.getCacheDir();
        }

        if(!cacheDir.exists())
            cacheDir.mkdirs();
		
	}
	
	private Bitmap getBitmap(String url) 
	{
		//I identify images by hashcode. Not a perfect solution, good for the demo.
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		
		//from SD cache
		Bitmap b = decodeFile(f);
	       
		if(b != null)
			return b;
		//from web
		try {
			Bitmap bitmap = null;
			InputStream is = new URL(url).openStream();
			
			Log.i("K", is.toString());
			
			OutputStream os = new FileOutputStream(f);
			ListUtils.CopyStream(is, os);
			os.close();
			bitmap = decodeFile(f);
			return bitmap;
			 
		} catch (Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	 
	 
	//decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f){
		
	    try {
	    	//decode image size
	    	BitmapFactory.Options option = new BitmapFactory.Options();
	    	option.inJustDecodeBounds = true;
	    	BitmapFactory.decodeStream(new FileInputStream(f),null, option);
	    	//Find the correct scale value. It should be the power of 2.
	            
	    	final int REQUIRED_SIZE = 200;
	            
	    	int width_tmp = option.outWidth; 
	    	int height_tmp = option.outHeight;
	    	int scale = 1;
	    	while(true){
	    		if(width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
	    			break;
	    		width_tmp /= 2;
	    		height_tmp /= 2;
	    		scale *= 2;
	    	}
	    	//decode with inSampleSize
	            
	    	BitmapFactory.Options option2 = new BitmapFactory.Options();
	            
	    	option2.inSampleSize = scale;
	            
	    	return BitmapFactory.decodeStream(new FileInputStream(f), null, option2);
	        
	    } catch (FileNotFoundException e) {}

	    return null;
	}
	
	public synchronized void fetchDrawableOnThread(String url) {
		Log.i("K", "url : " + url);
		
		mUrl = url;
	
		Thread thread = new Thread() {
			@Override
			public void run() {
				
				Bitmap bitmap = getBitmap(mUrl);
				Message message = null;
				
				if(bitmap == null) {
					message = handler.obtainMessage(IN_COMPLETE, bitmap);
				}
				else {
					message = handler.obtainMessage(COMPLETE, bitmap);
				}
				handler.sendMessage(message);
			}
		};
		
		thread.start();
	}	
}
