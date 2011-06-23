package com.fuzz.android.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.SeekBar;

public class CustomSeekBar extends SeekBar {

	Drawable thumb;
	public CustomSeekBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CustomSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//this.
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		 	int thumbWidth = thumb.getIntrinsicWidth();
	        int thumbHeight = thumb.getIntrinsicHeight();
	        int paddingleft = getPaddingLeft();
	        int paddingright = getPaddingRight();
	        int progress = getProgress();
	        
	        //Log.v("PaddingLeft", paddingleft + "");
	        //Log.v("PaddingRight", paddingright + "");
	        //Log.v("thumbWidth", thumbWidth + "");
	        //Log.v("progress", progress + "");
	        
	        int available = getWidth() - paddingleft - paddingright;
		 	int max = getMax();
		 	float scale = max > 0 ? (float) progress / (float) max : 0;
	        available -= thumbWidth;
	        int trackHeight = Math.min(getMeasuredHeight(), thumbHeight - getPaddingTop() - getPaddingBottom());
	        int gap = (trackHeight - thumbHeight) / 2;
	        //int gap = 0;
	        // The extra space for the thumb to move on the track
	        
	        //Log.v("max", max + "");
	        //Log.v("scale", scale + "");
	        
	        available += getThumbOffset() * 2;

	        //Log.v("available", available + "");
	        
	        int thumbPos = (int) (scale * available);
	        
	        //Log.v("thumbPos", thumbPos + "");

	        int topBound;
	        if (gap == Integer.MIN_VALUE) {
	            Rect oldBounds = thumb.getBounds();
	            topBound = oldBounds.top;
	            //bottomBound = oldBounds.bottom;
	        } else {
	            topBound = gap;
	           // bottomBound = gap + thumbHeight;
	        }
	        
	       // canvas.translate(mPaddingLeft - mThumbOffset, mPaddingTop);
		//canvas.translate(getLeft() - getThumbOffset(), getTop());
	    Paint p = getTextPaint();
	    String text = getTime(progress);
//	    float w = p.measureText(text);
	    
	    int yes = 0;
		if(metrics.density <= 1.0){
			yes = 1;
		}
		
		int yes2 = 0;
		if(metrics.density == 1.0){
			yes2 = 1;
		}
	    
		canvas.drawText(text , thumbPos + 3 - ((12 * yes) + (4 * yes2)) ,topBound + thumbHeight/2 - 5, p);
	}
	
	public String getTime(int progress){
		progress *= 5;
		int hours = progress/60;
		int minutes = progress%60;
		return hours + ":" + (minutes < 10 ? "0" + minutes : minutes);
	}
	
	public synchronized void setProgress (int progress){
		super.setProgress(progress/5);
	}
	
	Paint textPaint;
	
	public Paint getTextPaint() {
		if ( textPaint == null) {
			textPaint = new Paint();
			textPaint.setARGB(255, 0, 0, 0);
			textPaint.setAntiAlias(true);
			DisplayMetrics metrics = new DisplayMetrics();
			((Activity)this.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
			textPaint.setTextSize(14*metrics.density);
			textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		}
		return textPaint;
	}
	
	@Override
	public void setThumb(Drawable thumb2) { 
		//Log.v("CustomSeekBar","setThumb");
		thumb = thumb2;
		super.setThumb(thumb2);
	}
	
}
