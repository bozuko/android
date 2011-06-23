package com.bozuko.bozuko;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.EntryPointObject;
import com.bozuko.bozuko.datamodel.PageObject;
import com.bozuko.bozuko.map.LocationOverLay;
import com.bozuko.bozuko.map.MapItem;
import com.bozuko.bozuko.map.MapOverlay;
import com.fuzz.android.activities.MapControllerActivity;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class GamesMapController extends MapControllerActivity {

	ArrayList<PageObject> games = new ArrayList<PageObject>();
	private MapController mc;
	MapView mapView;
	LocationOverLay locationoverlay;
	List<Overlay> mapOverlays;
	MapOverlay itemizedoverlay;
	String zip;
	
	@Override
	public void progressRunnableComplete(){
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String latstr = mprefs.getString("lat","0.00"); 
		String lonstr = mprefs.getString("lon","0.00");
		int lat = (int) (Float.parseFloat(latstr)*1000000);
		int lon = (int) (Float.parseFloat(lonstr)*1000000);

		int minLat = Integer.MAX_VALUE;
		int minLong = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int maxLong = Integer.MIN_VALUE;

		if( games.size() == 1){
			minLat  = Math.min( lat, minLat );
			minLong = Math.min( lon, minLong);
			maxLat  = Math.max( lat, maxLat );
			maxLong = Math.max( lon, maxLong );
		}

		for(int page = 0; page<games.size(); page++){
			PageObject v = games.get(page);
			Double latp = Double.valueOf(v.requestInfo("locationlat"));
			Double lonp = Double.valueOf(v.requestInfo("locationlng"));
			GeoPoint p1 = new GeoPoint((int)(latp.floatValue()*1E6), (int) (lonp.floatValue()*1E6));
			MapItem overlayitem = new MapItem(p1, v.requestInfo("name"), v.requestInfo("locationstreet"));
			overlayitem.setObject(v);
			minLat  = Math.min( p1.getLatitudeE6(), minLat );
			minLong = Math.min( p1.getLongitudeE6(), minLong);
			maxLat  = Math.max( p1.getLatitudeE6(), maxLat );
			maxLong = Math.max( p1.getLongitudeE6(), maxLong );

			itemizedoverlay.addOverlay(overlayitem);
		}
		if(itemizedoverlay.shouldAdd()){
			mapOverlays.add(itemizedoverlay);
		}
		mapView.postInvalidate();
		if(games.size()==0){
			makeDialog("No theatres near location.","",null);
		}else{
			int ablat = Math.abs( minLat - maxLat );
			int ablong = Math.abs( minLong - maxLong );
			mapView.getController().zoomToSpan(ablat, ablong);
		}
	}
	
	@Override
	public void progressRunnableError(){
		if(itemizedoverlay.shouldAdd()){
			mapOverlays.add(itemizedoverlay);
		}
		mapView.postInvalidate();
		makeDialog("Couldn't contact server. Please try again.","An Error Occurred",null);
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapview);
		setRequestedOrientation(1);
		
		mapView = (MapView) findViewById(R.id.myMapView1);
		mc = mapView.getController();
		mapView.getController().setZoom(14);
		mapView.setBuiltInZoomControls(true);
		
		Drawable drawable = this.getResources().getDrawable(R.drawable.map_pin);
		mapOverlays = mapView.getOverlays();
		itemizedoverlay = new MapOverlay(drawable,this,mapView,"default");
		
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		zip = mprefs.getString("zipcode", "");
		centerStuff();
		loadLocation();
		loadGames();
	}
	
	public void onResume(){
    	super.onResume();
    	SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
    	if(mprefs.getBoolean("ReloadMap", false)){
			SharedPreferences.Editor edit = mprefs.edit();
			edit.putBoolean("ReloadMap", false);
			edit.commit();
			loadGames();
		}
    	if(games.size()==0){
    		loadGames();
    	}
    }
	
	public void loadLocation(){
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		float lat;
		float lon;
		if(locationoverlay == null){
			Drawable drawable2 = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position_anim1);
			locationoverlay = new LocationOverLay(drawable2,this,mapView,"default");
			String latstr = mprefs.getString("lat","0.00"); 
			String lonstr = mprefs.getString("lon","0.00");
			String title = "Current Location";
			lat = Float.parseFloat(latstr);
			lon = Float.parseFloat(lonstr);
			GeoPoint p1 = new GeoPoint((int) (lat * 1000000) , (int) (lon*1000000) );
			MapItem overlayitem = new MapItem(p1, title, "");
			locationoverlay.addOverlay(overlayitem);
			mapOverlays.add(locationoverlay);
		}else{
			mapOverlays.remove(locationoverlay);
			Drawable drawable2 = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position_anim1);
			locationoverlay = new LocationOverLay(drawable2,this,mapView,"default");
			String latstr = mprefs.getString("lat","0.00"); 
			String lonstr = mprefs.getString("lon","0.00");
			String title = "Current Location";
			lat = Float.parseFloat(latstr);
			lon = Float.parseFloat(lonstr);
			GeoPoint p1 = new GeoPoint((int) (lat * 1000000) , (int) (lon*1000000) );
			MapItem overlayitem = new MapItem(p1, title, "");
			locationoverlay.addOverlay(overlayitem);
			mapOverlays.add(locationoverlay);
		}
	}

	public void centerStuff(){
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		float lon = new Float(mprefs.getString("lon", "0.00")).floatValue();
		float lat = new Float(mprefs.getString("lat", "0.00")).floatValue();
		GeoPoint p = new GeoPoint((int) (lat * 1000000) , (int) (lon*1000000) );
		mc.animateTo(p);
		mc.setZoom(new Integer(mprefs.getString("zoom", "14")).intValue());
		itemizedoverlay.empty();
		mapOverlays.remove(itemizedoverlay);
		Drawable drawable = this.getResources().getDrawable(R.drawable.map_pin);
		itemizedoverlay = new MapOverlay(drawable,this,mapView,"default");
	}
	
	public void loadGames(){
		progressRunnable(new Runnable(){
			public void run(){
				EntryPointObject entry = new EntryPointObject("1");
                entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(getBaseContext()));
				sendRequest(entry);
			}
		},"Loading. Please wait...",CANCELABLE);
	}
	
	public void sendRequest(EntryPointObject entry){
    	if(!DataBaseHelper.isOnline(this)){
    		RUNNABLE_STATE = RUNNABLE_FAILED;
		}
		try {
			games.clear();
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linkspages");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			url += String.format("?ll=%s,%s&limit=25&offset=%d",mprefs.getString("lat","0.00"),mprefs.getString("lon","0.00"),0);
			Log.v("URL",url);
			HttpRequest req = new HttpRequest(new URL(url + "&token=" + mprefs.getString("token", "")));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSON();
			JSONArray objects = json.getJSONArray("pages");
			for(int i=0; i<objects.length(); i++){
				PageObject page = new PageObject(objects.getJSONObject(i));
				Log.v("Page",page.toString());
				if(page.requestInfo("registered").compareTo("true")==0){
					games.add(page);
				}
			}
			RUNNABLE_STATE = RUNNABLE_SUCCESS;
		} catch (Throwable e) {
			e.printStackTrace();
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
    }
}
