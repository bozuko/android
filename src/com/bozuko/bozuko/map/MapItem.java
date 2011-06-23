package com.bozuko.bozuko.map;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class MapItem extends OverlayItem{

	GeoPoint mPoint;
	String mTitle;
	String mSnippet;
	Object mapObject;
	
	public MapItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
		mPoint = point;
		mTitle = title;
		mSnippet = snippet;
		// TODO Auto-generated constructor stub
	}

	public void update(GeoPoint p, String title, String subtitle) {
		// TODO Auto-generated method stub
		mPoint = p;
		mTitle = title;
		mSnippet = subtitle;
	}
	
	public void setObject(Object inObject){
		mapObject = inObject;
	}
	
	public Object getObject(){
		return mapObject;
	}
}
