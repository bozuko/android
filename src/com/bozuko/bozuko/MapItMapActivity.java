package com.bozuko.bozuko;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.bozuko.bozuko.datamodel.PageObject;
import com.bozuko.bozuko.map.LocationOverLay;
import com.bozuko.bozuko.map.MapItem;
import com.bozuko.bozuko.map.MapOverlay;
import com.fuzz.android.activities.MapControllerActivity;
import com.fuzz.android.globals.GlobalFunctions;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapItMapActivity extends MapControllerActivity implements OnClickListener {
	private MapController mc;
	MapView mapView;
	LocationOverLay locationoverlay;
	List<Overlay> mapOverlays;
	MapOverlay itemizedoverlay;
	PageObject page;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent(R.layout.mapview);
		setHeader(R.layout.detailsubmitheader);
		setRequestedOrientation(1);

		mapView = (MapView) findViewById(R.id.myMapView1);
		mc = mapView.getController();
		mapView.getController().setZoom(14);
		mapView.setBuiltInZoomControls(true);

		Drawable drawable = this.getResources().getDrawable(R.drawable.map_pin);
		mapOverlays = mapView.getOverlays();
		itemizedoverlay = new MapOverlay(drawable,this,mapView,"default");

		page = (PageObject)getIntent().getParcelableExtra("Package");
		String latstr = page.requestInfo("locationlat"); 
		String lonstr = page.requestInfo("locationlng");
		latstr = GlobalFunctions.sanitize(latstr);
		lonstr = GlobalFunctions.sanitize(lonstr);
		String title = page.requestInfo("name");
		String subtitle = page.requestInfo("locationstreet");

		float lat = Float.parseFloat(latstr);
		float lon = Float.parseFloat(lonstr);
		GeoPoint p = new GeoPoint((int) (lat * 1000000) , (int) (lon*1000000) );
		MapItem overlayitem = new MapItem(p, title, subtitle);
		overlayitem.setObject(null);

		itemizedoverlay.addOverlay(overlayitem);
		p = new GeoPoint((int) (lat * 1000000) , (int) (lon*1000000) );

		int minLat = Integer.MAX_VALUE;
		int minLong = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int maxLong = Integer.MIN_VALUE;
		
		minLat  = Math.min( p.getLatitudeE6(), minLat );
		minLong = Math.min( p.getLongitudeE6(), minLong);
		maxLat  = Math.max( p.getLatitudeE6(), maxLat );
		maxLong = Math.max( p.getLongitudeE6(), maxLong );


		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		String latstr2 = mprefs.getString("clat","0.00"); 
		String lonstr2 = mprefs.getString("clon","0.00");
		String title2 = "Current Location";
		float lat2 = Float.parseFloat(latstr2);
		float lon2 = Float.parseFloat(lonstr2);
		p = new GeoPoint((int) (lat2 * 1000000) , (int) (lon2*1000000) );
		minLat  = Math.min( p.getLatitudeE6(), minLat );
		minLong = Math.min( p.getLongitudeE6(), minLong);
		maxLat  = Math.max( p.getLatitudeE6(), maxLat );
		maxLong = Math.max( p.getLongitudeE6(), maxLong );

		mc.setZoom(new Integer(12).intValue());
		MapItem overitem = new MapItem(p, title2, "");
		Drawable drawable2 = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position_anim1);
		locationoverlay = new LocationOverLay(drawable2,this,mapView,"default");
		
		locationoverlay.addOverlay(overitem);
		mapOverlays.add(locationoverlay);

		int ablat = Math.abs( minLat - maxLat );
		int ablong = Math.abs( minLong - maxLong );
		mapView.getController().zoomToSpan(ablat, ablong);
		int midlat = ((ablat/2) + minLat);
		int midlong = ((ablong/2) + minLong);
		mc.animateTo(new GeoPoint(midlat,midlong));
		mapView.setBuiltInZoomControls(true);

		if(itemizedoverlay.shouldAdd()){
			mapOverlays.add(itemizedoverlay);
		}

		mapView.postInvalidate();
		
		findViewById(R.id.currentloc).setOnClickListener(this);
		
		findViewById(R.id.submit).setOnClickListener(this);
		((Button)findViewById(R.id.submit)).setText("Google Maps");
	}
	
	public void centerStuff(){
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		float lon = new Float(mprefs.getString("clon", "0.00")).floatValue();
		float lat = new Float(mprefs.getString("clat", "0.00")).floatValue();
		GeoPoint p = new GeoPoint((int) (lat * 1000000) , (int) (lon*1000000) );
		mc.animateTo(p);
		mc.setZoom(new Integer(mprefs.getString("zoom", "16")).intValue());
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(R.id.currentloc == arg0.getId()){
			centerStuff();
		}else{
			String latstr = page.requestInfo("locationlat"); 
			String lonstr = page.requestInfo("locationlng");
			String title = page.requestInfo("name");
			
			Intent map = new Intent(Intent.ACTION_VIEW);
			map.setData(Uri.parse("geo:0,0?q="+ latstr +","+ lonstr +" (" + title + ")"));
			startActivity(map);
		}
	}
}
