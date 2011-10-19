package com.bozuko.bozuko.map;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import com.bozuko.bozuko.BozukoApplication;
import com.bozuko.bozuko.R;
import com.bozuko.bozuko.datamodel.PageObject;
import com.fuzz.android.activities.MapControllerActivity;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

@SuppressWarnings("unchecked")
public class MapOverlay extends ItemizedOverlay
{
	private ArrayList<MapItem> mOverlays = new ArrayList<MapItem>();
	//Context mContext;
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
		 // mContext = context;
		  type = string;
	}
	
	public boolean checktype(String str){
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
				Object object = item.getObject();
				if(object!=null){
					((BozukoApplication)((MapControllerActivity)mapView.getContext()).getApp()).currentPageObject = (PageObject)object;
				
					Intent intent = new Intent();
					intent.setClassName("com.bozuko.bozuko", "com.bozuko.bozuko.PageBozukoActivity");
					//intent.putExtra("Package",(Parcelable)item.getObject());
					mapView.getContext().startActivity(intent);
				}else{
//					String latstr = ((BozukoApplication)((MapControllerActivity)mapView.getContext()).getApp()).currentPageObject.requestInfo("locationlat"); 
//					String lonstr = ((BozukoApplication)((MapControllerActivity)mapView.getContext()).getApp()).currentPageObject.requestInfo("locationlng");
//					String title = item.getTitle();
//					
//					Intent map = new Intent(Intent.ACTION_VIEW);
//					map.setData(Uri.parse("geo:0,0?q="+ latstr +","+ lonstr +" (" + title + ")"));
//					mapView.getContext().startActivity(map);
					((Activity)mapView.getContext()).finish();
				}
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
	
	public boolean onTouchEvent(MotionEvent event,MapView mapView){
		if(event.getAction() == MotionEvent.ACTION_UP){
			//Intent intent = new Intent("MapPan");
			//mapView.getContext().sendBroadcast(intent);
		}
		
		return super.onTouchEvent(event, mapView);
	}
	
	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
	  p = item.getPoint();
	  ind = index;
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
		((Activity) mapView.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		int TEXT_OFFSET_X2 = (int)(8*metrics.density);
		int INFO_WINDOW_WIDTH2 = (int)(233*metrics.density);
		int INFO_WINDOW_WIDTH = (int)(getTextPaint(mapView.getContext()).measureText(item.getTitle()) + (TEXT_OFFSET_X2*2));
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
		canvas.drawPath(path, getInnerPaint(mapView.getContext()));
		//  Draw border for info window
		//canvas.drawRoundRect(infoWindowRect, 5, 5, getBorderPaint());
		canvas.drawPath(path, getBorderPaint(mapView.getContext()));	
		//  Draw the MapLocation's name
		int TEXT_OFFSET_X = (int)(8*metrics.density);
		int TEXT_OFFSET_Y = (int)(15*metrics.density);
		
		canvas.drawText(item.getTitle(),infoWindowOffsetX+TEXT_OFFSET_X,infoWindowOffsetY+TEXT_OFFSET_Y,getTextPaint(mapView.getContext()));
		
		TEXT_OFFSET_X = (int)(8*metrics.density);
		TEXT_OFFSET_Y = (int)(30*metrics.density);
		canvas.drawText(item.getSnippet(),infoWindowOffsetX+TEXT_OFFSET_X,infoWindowOffsetY+TEXT_OFFSET_Y,getTextPaint2(mapView.getContext()));
		
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.outHeight = 24;
		opts.outWidth = 18;
		opts.inPurgeable = true;
		//TEXT_OFFSET_X = (int)(8*metrics.density);
		//TEXT_OFFSET_Y = (int)(45*metrics.density);
		//canvas.drawText(item.getSnippet(),infoWindowOffsetX+TEXT_OFFSET_X,infoWindowOffsetY+TEXT_OFFSET_Y,getTextPaint2());
		 Bitmap bitmap = BitmapFactory.decodeResource(mapView.getContext().getResources(), R.drawable.arrowwhite,opts);
	   //  Rect dst = new Rect(INFO_WINDOW_WIDTH-23,INFO_WINDOW_HEIGHT/2 - 24/2,18,24);
	   //  canvas.drawBitmap(bitmap, null, dst, new Paint());
	     canvas.drawBitmap(bitmap, infoWindowOffsetX+(INFO_WINDOW_WIDTH-28), infoWindowOffsetY+((INFO_WINDOW_HEIGHT/2) - (24/2)), null);
		
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
	 
	 public Paint getInnerPaint(Context mContext) {
			if ( innerPaint == null) {
				innerPaint = new Paint();
				innerPaint.setARGB(225, 75, 75, 75); //gray
				innerPaint.setAntiAlias(true);
			}
			return innerPaint;
		}

		public Paint getBorderPaint(Context mContext) {
			if ( borderPaint == null) {
				borderPaint = new Paint();
				borderPaint.setARGB(255, 255, 255, 255);
				borderPaint.setAntiAlias(true);
				borderPaint.setStyle(Style.STROKE);
				borderPaint.setStrokeWidth(2);
			}
			return borderPaint;
		}

		public Paint getTextPaint(Context mContext) {
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
		
		public Paint getTextPaint2(Context mContext) {
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
