package com.bozuko.bozuko.map;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.drawable.Drawable;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

@SuppressWarnings("unchecked")
public class LocationOverLay extends ItemizedOverlay
{
	private ArrayList<MapItem> mOverlays = new ArrayList<MapItem>();
	Context mContext;
	Drawable marker;
	String type;
	
	public Drawable getMarker(){
		return marker;
	}
	
	public LocationOverLay(Drawable defaultMarker) {
		  super(boundCenterBottom(defaultMarker));
		  marker = defaultMarker;
	}
	
	public LocationOverLay(Drawable defaultMarker, Context context, MapView m, String string) {
		  super(boundCenterBottom(defaultMarker));
		  marker = defaultMarker;
		  mContext = context;
		  type = string;
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
}
