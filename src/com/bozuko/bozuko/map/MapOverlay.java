package com.bozuko.bozuko.map;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

@SuppressWarnings("unchecked")
public class MapOverlay extends ItemizedOverlay
{
	private ArrayList<MapItem> mOverlays = new ArrayList<MapItem>();
	Context mContext;
	Drawable marker;
	String type;
	
	public Drawable getMarker(){
		return marker;
	}
	
	public MapOverlay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
		  marker = defaultMarker;
	}
	
	public MapOverlay(Drawable defaultMarker, Context context, MapView m, String string) {
		  super(boundCenterBottom(defaultMarker));
		  marker = defaultMarker;
		  mContext = context;
		  type = string;
	}
	
	public boolean checktype(String str){
		//Log.v("CHECKTYPE",type + " " + str);
		return (str.compareTo(type) == 0);
	}
	
	public void addOverlay(MapItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}
	
	@Override
	public int size() {
	  return mOverlays.size();
	}
	
	GeoPoint p = null;
	
	@Override
	public boolean onTap(GeoPoint d, MapView mapView)  {
		
		
		if(infoWindowRect != null){
			Point selDestinationOffset = new Point();
			mapView.getProjection().toPixels(d, selDestinationOffset);
			if(infoWindowRect.contains(selDestinationOffset.x, selDestinationOffset.y)){
				MapItem item = mOverlays.get(ind);
				Intent intent = new Intent();
				intent.setClassName("com.bozuko.bozuko", "com.bozuko.bozuko.PageBozukoActivity");
				intent.putExtra("Package",(Parcelable)item.getObject());
				mContext.startActivity(intent);
			}else{
				if(p != null){
					p = null;
				}
			}
		}else{
			if(p != null){
				p = null;
			}
		}
		return super.onTap(d,mapView);
	}
	
	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
	  p = item.getPoint();
	  ind = index;
	  /*AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();*/
	  return true;
	}
	
	private int ind;
	private RectF infoWindowRect = null;
	private Paint	innerPaint, borderPaint, textPaint, textPaint2;
	
	
	private void drawInfoWindow(Canvas canvas, MapView	mapView, boolean shadow) {
		try{
		if(p != null){
		Point selDestinationOffset = new Point();
		mapView.getProjection().toPixels(p, selDestinationOffset);
    	
		MapItem item = mOverlays.get(ind);
    	//  Setup the info window with the right size & location
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		int TEXT_OFFSET_X2 = (int)(8*metrics.density);
		int INFO_WINDOW_WIDTH2 = (int)(233*metrics.density);
		int INFO_WINDOW_WIDTH = (int)(getTextPaint().measureText(item.getTitle()) + (TEXT_OFFSET_X2*2));
		if(INFO_WINDOW_WIDTH<INFO_WINDOW_WIDTH2){
			INFO_WINDOW_WIDTH = INFO_WINDOW_WIDTH2;
		}
		int INFO_WINDOW_HEIGHT = (int) (40*metrics.density);
		infoWindowRect = new RectF(0,0,INFO_WINDOW_WIDTH,INFO_WINDOW_HEIGHT);				
		int infoWindowOffsetX = selDestinationOffset.x-INFO_WINDOW_WIDTH/2;
		
		Drawable nmarker = item.getMarker(000);
		if(nmarker == null)
			nmarker = marker;
		int infoWindowOffsetY = selDestinationOffset.y-INFO_WINDOW_HEIGHT-nmarker.getIntrinsicHeight()-20;
		infoWindowRect.offset(infoWindowOffsetX,infoWindowOffsetY);

		
		Path path = new Path();
		path.moveTo(infoWindowOffsetX + INFO_WINDOW_WIDTH/2, infoWindowOffsetY );
		path.arcTo(new RectF(infoWindowOffsetX, infoWindowOffsetY, infoWindowOffsetX+10, infoWindowOffsetY+10), 270, -90);
		path.arcTo(new RectF(infoWindowOffsetX, infoWindowOffsetY+INFO_WINDOW_HEIGHT - 10, infoWindowOffsetX+10, infoWindowOffsetY+INFO_WINDOW_HEIGHT), 180, -90);
		
		path.lineTo(infoWindowOffsetX + INFO_WINDOW_WIDTH/2 - 20 , infoWindowOffsetY+INFO_WINDOW_HEIGHT);
		path.lineTo(infoWindowOffsetX + INFO_WINDOW_WIDTH/2  , infoWindowOffsetY+INFO_WINDOW_HEIGHT + 20);
		path.lineTo(infoWindowOffsetX + INFO_WINDOW_WIDTH/2 + 20 , infoWindowOffsetY+INFO_WINDOW_HEIGHT);
		
		path.arcTo(new RectF(infoWindowOffsetX+INFO_WINDOW_WIDTH - 10, infoWindowOffsetY+INFO_WINDOW_HEIGHT - 10, infoWindowOffsetX+INFO_WINDOW_WIDTH, infoWindowOffsetY+INFO_WINDOW_HEIGHT), 90, -90);
		path.arcTo(new RectF(infoWindowOffsetX+INFO_WINDOW_WIDTH - 10, infoWindowOffsetY, infoWindowOffsetX+INFO_WINDOW_WIDTH, infoWindowOffsetY + 10), 0, -90);
		
		path.close(); 
		
		//  Draw inner info window
		//canvas.drawRoundRect(infoWindowRect, 5, 5, getInnerPaint());
		canvas.drawPath(path, getInnerPaint());
		//  Draw border for info window
		//canvas.drawRoundRect(infoWindowRect, 5, 5, getBorderPaint());
		canvas.drawPath(path, getBorderPaint());	
		//  Draw the MapLocation's name
		int TEXT_OFFSET_X = (int)(8*metrics.density);
		int TEXT_OFFSET_Y = (int)(15*metrics.density);
		
		canvas.drawText(item.getTitle(),infoWindowOffsetX+TEXT_OFFSET_X,infoWindowOffsetY+TEXT_OFFSET_Y,getTextPaint());
		
		TEXT_OFFSET_X = (int)(8*metrics.density);
		TEXT_OFFSET_Y = (int)(30*metrics.density);
		canvas.drawText(item.getSnippet(),infoWindowOffsetX+TEXT_OFFSET_X,infoWindowOffsetY+TEXT_OFFSET_Y,getTextPaint2());
		
		//TEXT_OFFSET_X = (int)(8*metrics.density);
		//TEXT_OFFSET_Y = (int)(45*metrics.density);
		//canvas.drawText(item.getSnippet(),infoWindowOffsetX+TEXT_OFFSET_X,infoWindowOffsetY+TEXT_OFFSET_Y,getTextPaint2());
		}else{
			infoWindowRect= null;
		}
		}catch(IndexOutOfBoundsException e){
			p=null;
		}
    }
	
	
	
	 @Override
	public void draw(Canvas canvas, MapView	mapView, boolean shadow) {
	    super.draw(canvas, mapView, shadow);
	   	drawInfoWindow(canvas, mapView, shadow);
	}
	 
	 public Paint getInnerPaint() {
			if ( innerPaint == null) {
				innerPaint = new Paint();
				innerPaint.setARGB(225, 75, 75, 75); //gray
				innerPaint.setAntiAlias(true);
			}
			return innerPaint;
		}

		public Paint getBorderPaint() {
			if ( borderPaint == null) {
				borderPaint = new Paint();
				borderPaint.setARGB(255, 255, 255, 255);
				borderPaint.setAntiAlias(true);
				borderPaint.setStyle(Style.STROKE);
				borderPaint.setStrokeWidth(2);
			}
			return borderPaint;
		}

		public Paint getTextPaint() {
			if ( textPaint == null) {
				textPaint = new Paint();
				textPaint.setARGB(255, 255, 255, 255);
				textPaint.setAntiAlias(true);
				DisplayMetrics metrics = new DisplayMetrics();
				((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
				textPaint.setTextSize(12*metrics.density);
				textPaint.setTypeface(Typeface.DEFAULT_BOLD);
			}
			return textPaint;
		}
		
		public Paint getTextPaint2() {
			if ( textPaint2 == null) {
				textPaint2 = new Paint();
				textPaint2.setARGB(255, 255, 255, 255);
				textPaint2.setAntiAlias(true);
				DisplayMetrics metrics = new DisplayMetrics();
				((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
				textPaint2.setTextSize(12*metrics.density);
				//textPaint2.setTextSize(12);
			}
			return textPaint2;
		}

		public boolean shouldAdd() {
			// TODO Auto-generated method stub
			if(mOverlays.size() == 0)
				return false;
			
			return true;
		}

		public void empty() {
			// TODO Auto-generated method stub
			mOverlays.clear();
			populate();
		}
}
