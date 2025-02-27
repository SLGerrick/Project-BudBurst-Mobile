package edu.ucla.cens.budburstmobile.mapview;


import edu.ucla.cens.budburstmobile.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Class for compass view used in the medium level floracache
 * @author kyunghan
 *
 */
public class CompassView extends View{

	private float mDirection = 0;
	private Paint mPaint;
	private Paint mMarkerPaint;
	// private Paint mMarkerPaintMyDirection;
	private Paint mTextPaint;
	private Paint mCharPaint;
	private Paint mLinePaint;
	private boolean mFirstDraw;
	
	public CompassView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public CompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}
	
	private void init(){
		
		Resources res = this.getResources();	
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(2);
		mPaint.setColor(Color.BLACK);
		
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setStyle(Paint.Style.STROKE);
		mLinePaint.setStrokeWidth(1);
		mLinePaint.setColor(Color.BLACK);
		
		mCharPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCharPaint.setStrokeWidth(1);
		mCharPaint.setStyle(Paint.Style.STROKE);
		mCharPaint.setColor(Color.BLACK);
		mCharPaint.setTextSize(15);
		
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setStrokeWidth(1);
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setTextSize(23);
		
		mMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMarkerPaint.setColor(res.getColor(R.color.marker_color));
		mMarkerPaint.setAlpha(200);
		mMarkerPaint.setStrokeWidth(2);
		mMarkerPaint.setTextSize(20);
		mMarkerPaint.setStyle(Paint.Style.STROKE);
		mMarkerPaint.setShadowLayer(2, 1, 1, res.getColor(R.color.shadow_color));
		
		/*
		mMarkerPaintMyDirection = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMarkerPaintMyDirection.setColor(res.getColor(R.color.BudBurstBackground));
		mMarkerPaintMyDirection.setStrokeWidth(4);
		mMarkerPaintMyDirection.setStyle(Paint.Style.STROKE);
		mMarkerPaintMyDirection.setShadowLayer(2, 1, 1, res.getColor(R.color.shadow_color));
		*/
		//paint.setTextSize(30);
		
		mFirstDraw = true;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		int cxCompass = getMeasuredWidth()/2;
		int cyCompass = 120;
		float radiusCompass = 60;
		
		/*
		if(cxCompass > cyCompass){
			radiusCompass = (float) (cyCompass * 0.9);
		}
		else{
			radiusCompass = (float) (cxCompass * 0.9);
		}
		*/
		
		Log.i("K" , "cxCompass : " + cxCompass);
		// black compass circle
		canvas.drawCircle(cxCompass, cyCompass, radiusCompass, mPaint);
		//canvas.drawRect(0, 0, getMeasuredWidth(), 120, paint);
		
		String direction = getDirectionStr(mDirection);
        
        if(!mFirstDraw){
        	
        	// red line
			canvas.drawLine(cxCompass, cyCompass, 
					(float)(cxCompass + radiusCompass * Math.sin((double)(-mDirection) * Math.PI/180)), 
					(float)(cyCompass - radiusCompass * Math.cos((double)(-mDirection) * Math.PI/180)), 
					mMarkerPaint);
			
			canvas.drawText("N", (float)(cxCompass + getVarX(mDirection) + radiusCompass * Math.sin((double)(-mDirection) * Math.PI/180)), 
					(float)(cyCompass + getVarY(mDirection) - radiusCompass * Math.cos((double)(-mDirection) * Math.PI/180)), mMarkerPaint);
			
			/*
			// green arrow
			canvas.drawLine(cxCompass, cyCompass, 
					cxCompass, cyCompass - radiusCompass,
					mMarkerPaintMyDirection);
			canvas.drawLine(
					(float)(cxCompass + Math.cos(225 * (Math.PI/180))*12), 
					(float)(cyCompass - radiusCompass - Math.cos(225 * (Math.PI/180))*12),
					cxCompass,
					cyCompass - radiusCompass,
					mMarkerPaintMyDirection);
			
			canvas.drawLine(  
					(float)(cxCompass - Math.cos(135 * (Math.PI/180))*12), 
					(float)(cyCompass - radiusCompass - Math.cos(135 * (Math.PI/180))*12),
					cxCompass,
					cyCompass - radiusCompass,
					mMarkerPaintMyDirection);
			*/
			
			
			
			// black compass line
			canvas.drawLine(cxCompass-radiusCompass, cyCompass, 
					cxCompass + radiusCompass, cyCompass, mLinePaint);
			canvas.drawLine(cxCompass, cyCompass-radiusCompass, 
					cxCompass, cyCompass + radiusCompass, mLinePaint);
			
			canvas.drawLine((float)(cxCompass + Math.cos(45 * (Math.PI/180))*radiusCompass), (float)(cyCompass - Math.sin(45 * (Math.PI/180))*radiusCompass), 
							(float)(cxCompass - Math.cos(45 * (Math.PI/180))*radiusCompass), (float)(cyCompass + Math.sin(45 * (Math.PI/180))*radiusCompass), mLinePaint);
			
			canvas.drawLine((float)(cxCompass - Math.cos(135 * (Math.PI/180))*radiusCompass), (float)(cyCompass + Math.sin(135 * (Math.PI/180))*radiusCompass), 
							(float)(cxCompass + Math.cos(135 * (Math.PI/180))*radiusCompass), (float)(cyCompass - Math.sin(135 * (Math.PI/180))*radiusCompass), mLinePaint);
			
			// black text for degree
			if(direction == "N" || direction == "E" || direction == "S" || direction == "W") {
				canvas.drawText(String.valueOf(direction) + " (" + (int) mDirection + "\u00B0)", cxCompass-40, 30, mTextPaint);
			}
			else {
				canvas.drawText(String.valueOf(direction) + " (" + (int) mDirection + "\u00B0)", cxCompass-50, 30, mTextPaint);
			}
			
		}
	}
	
	private int getVarX(float degree) {
		
		int getValue = 0;
		
		if(degree > 0 && degree <= 45) {
			getValue = -8;
		}
		else if(degree > 45 && degree < 90) {
			getValue = -15;
		}
		else if(degree == 90) {
			getValue = -18;
		}
		else if(degree > 90 && degree < 135) {
			getValue = -16;
		}
		else if(degree >= 135 && degree < 180) {
			getValue = -12;
		}
		else if(degree == 180) {
			getValue = 0;
		}
		else if(degree >= 180 && degree < 225) {
			getValue = 4;
		}
		else if(degree >= 225 && degree < 270) {
			getValue = 10;
		}
		else if(degree == 270) {
			getValue = 18;
		}
		else if(degree >= 270 && degree < 315) {
			getValue = 6;
		}
		else if(degree >= 315 && degree < 360) {
			getValue = 3;
		}
		else if(degree == 0) {
			getValue = 0;
		}
		
		return getValue;
	}
	
	private int getVarY(float degree) {
		int getValue = 0;
		
		if(degree > 0 && degree <= 45) {
			getValue = -8;
		}
		else if(degree > 45 && degree < 90) {
			getValue = -6;
		}
		else if(degree == 90) {
			getValue = 0;
		}
		else if(degree > 90 && degree < 135) {
			getValue = 12;
		}
		else if(degree >= 135 && degree < 180) {
			getValue = 17;
		}
		else if(degree == 180) {
			getValue = 18;
		}
		else if(degree >= 180 && degree < 225) {
			getValue = 14;
		}
		else if(degree >= 225 && degree < 270) {
			getValue = 8;
		}
		else if(degree == 270) {
			getValue = 0;
		}
		else if(degree >= 270 && degree < 315) {
			getValue = -5;
		}
		else if(degree >= 315 && degree < 360) {
			getValue = -5;
		}
		else if(degree == 0) {
			getValue = -16;
		}
		
		return getValue;
	}
	
    private String getDirectionStr(float trueBearing) {
		String direction = "";
        
		if((trueBearing > 337.5 && trueBearing <= 360) || (trueBearing >= 0 && trueBearing < 22.5))
        	direction = "N";
        if(trueBearing >= 22.5 && trueBearing < 67.5) 
        	direction ="NE";
        if(trueBearing >= 67.5 && trueBearing < 112.5) 
        	direction = "E";
        if(trueBearing >= 112.5 && trueBearing < 157.5) 
        	direction = "SE";
        if(trueBearing >= 157.5 && trueBearing < 202.5) 
        	direction = "S";
        if(trueBearing >= 202.5 && trueBearing < 247.5) 
        	direction = "SW";
        if(trueBearing >= 247.5 && trueBearing < 292.5) 
        	direction = "W";
        if(trueBearing >= 292.5 && trueBearing < 337.5) 
        	direction = "NW";
        
        Log.i("K", "Direction : " + direction);
        
        return direction;
    }
	
	public void updateDirection(float dir)
	{
		mFirstDraw = false;
		mDirection = dir;
		invalidate();
	}
}
