package com.bozuko.bozuko;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.EntryPointObject;
import com.bozuko.bozuko.datamodel.PageObject;
import com.bozuko.bozuko.datamodel.User;
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

public class GamesMapController extends MapControllerActivity implements OnClickListener{

	HashMap<String,String> gamesLoaded = new HashMap<String,String>();
	ArrayList<PageObject> games = new ArrayList<PageObject>();
	private MapController mc;
	MapView mapView;
	LocationOverLay locationoverlay;
	List<Overlay> mapOverlays;
	MapOverlay itemizedoverlay;
	String zip;
	boolean SENDING = false;
	public String errorType = "";

	@Override
	public void progressRunnableComplete(){
		
	}
	
	@Override
	public void progressRunnableError(){
		if(isFinishing()){
			return;
		}
		if(itemizedoverlay.shouldAdd()){
			mapOverlays.add(itemizedoverlay);
		}
		mapView.postInvalidate();
		if(games.size()==0){
			//makeDialog("No games near location.","",null);
			if(errorType.compareTo("facebook/auth")==0){
				makeDialog(errorMessage,errorTitle,new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						facebookSignOut();
						finish();
					}
				});
			}else{
				makeDialog(errorMessage,errorTitle,null);
			}
			
			findViewById(R.id.errormsg).setVisibility(View.VISIBLE);
		}else{
			findViewById(R.id.errormsg).setVisibility(View.GONE);
		}
		SENDING = false;
		
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapviewwithtop);
		setRequestedOrientation(1);

		mapView = (MapView) findViewById(R.id.myMapView1);
		mc = mapView.getController();
		mapView.getController().setZoom(16);
		mapView.setBuiltInZoomControls(true);
		//mapView.setTouchDelegate(this);
		Drawable drawable = this.getResources().getDrawable(R.drawable.map_pin);
		mapOverlays = mapView.getOverlays();
		itemizedoverlay = new MapOverlay(drawable,this,mapView,"default");
		
		
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		zip = mprefs.getString("zipcode", "");
		centerStuff();
		loadLocation();
		//loadGames();
		
		findViewById(R.id.currentloc).setOnClickListener(this);
	}

	UpdateReceiver mReceiver = new UpdateReceiver();
	public void onPause(){
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	
	public void onResume(){
		super.onResume();
		registerReceiver(mReceiver, new IntentFilter("MapPan"));
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(mprefs.getBoolean("ReloadMap", false)){
			games.clear();
			gamesLoaded.clear();
			itemizedoverlay.empty();
			mapOverlays.remove(itemizedoverlay);
			SharedPreferences.Editor edit = mprefs.edit();
			edit.putBoolean("ReloadMap", false);
			edit.commit();
			centerStuff();
			loadLocation();
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
			String latstr = mprefs.getString("clat","0.00"); 
			String lonstr = mprefs.getString("clon","0.00");
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
			String latstr = mprefs.getString("clat","0.00"); 
			String lonstr = mprefs.getString("clon","0.00");
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
		float lon = new Float(mprefs.getString("clon", "0.00")).floatValue();
		float lat = new Float(mprefs.getString("clat", "0.00")).floatValue();
		GeoPoint p = new GeoPoint((int) (lat * 1000000) , (int) (lon*1000000) );
		mc.animateTo(p);
		mc.setZoom(new Integer(mprefs.getString("zoom", "16")).intValue());
	}

	public void loadGames(){
		if(!SENDING){
		SENDING = true;
		progressRunnable(new Runnable(){
			public void run(){
				EntryPointObject entry = new EntryPointObject("1");
				entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(getBaseContext()));
				sendRequest(entry,0);
			}
		},"Loading. Please wait...",CANCELABLE);
		}
	}
	
	public void loadGamesBG(){
		if(!SENDING){
		SENDING = true;
		unProgressRunnable(new Runnable(){
			public void run(){
				EntryPointObject entry = new EntryPointObject("1");
				entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(getBaseContext()));
				sendRequest(entry,0);
			}
		});
		}
	}

	public float[][] getBounds(){
		GeoPoint center = this.mapView.getMapCenter();
		int latitudeSpan = this.mapView.getLatitudeSpan();
		int longtitudeSpan = this.mapView.getLongitudeSpan();
		float[][] bounds = new float[2][2];

		bounds[0][0] = ((float)center.getLatitudeE6() + (latitudeSpan/2))/1000000;
		bounds[0][1] = ((float)center.getLongitudeE6() + (longtitudeSpan/2))/1000000;

		bounds[1][0] = ((float)center.getLatitudeE6() - (latitudeSpan/2))/1000000;
		bounds[1][1] = ((float)center.getLongitudeE6() - (longtitudeSpan/2))/1000000;
		return bounds;
	}

	public void sendRequest(EntryPointObject entry,int time){
		if(!DataBaseHelper.isOnline(this,0)){
			errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
			RUNNABLE_STATE = RUNNABLE_FAILED;
			return;
		}
		try {
			ArrayList<PageObject> tempGames = new ArrayList<PageObject>();
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linkspages");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			float bounds[][] = getBounds();
			GeoPoint center = this.mapView.getMapCenter();
			url += String.format("?ll=%f,%f&limit=25&offset=%d",((float)center.getLatitudeE6())/1000000,((float)center.getLongitudeE6())/1000000,0);
			url += String.format("&bounds=%f,%f,%f,%f", bounds[1][0],bounds[1][1],bounds[0][0],bounds[0][1]);
			Log.v("MAPURL",url);
			if(url.toLowerCase().contains("null")){
				if(time<20000){
					Thread.sleep(2000);
					sendRequest(entry,time+2000);
				}
			}
			
			HttpRequest req = new HttpRequest(new URL(url + "&token=" + mprefs.getString("token", "") + "&mobileversion="+GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			JsonParser jp = req.AutoStreamJSONError();
			jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String fieldname = jp.getCurrentName();
				jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
				if ("pages".equals(fieldname)) { 
					//DO parse json
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						PageObject page = new PageObject(jp);
						//Log.v("Page",page.toString());
						if(page.requestInfo("registered").compareTo("true")==0){
							if(!gamesLoaded.containsKey(page.requestInfo("id"))){
								tempGames.add(page);
								gamesLoaded.put(page.requestInfo("id"), "1");
							}
						}
					}
					mHandler.post(new LoadMapRunnable(tempGames));
					RUNNABLE_STATE = RUNNABLE_SUCCESS;
				}else if ("title".equals(fieldname)) { 
					RUNNABLE_STATE = RUNNABLE_FAILED;
					errorTitle = jp.getText();
				}else if ("message".equals(fieldname)) {
					RUNNABLE_STATE = RUNNABLE_FAILED;
					errorMessage = jp.getText();
				}else if ("name".equals(fieldname)) { 
					RUNNABLE_STATE = RUNNABLE_FAILED;
					errorType = jp.getText();
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
			//mHandler.post(new DisplayThrowable(e));
			errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}
	
	protected class UpdateReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			loadGamesBG();
		}

	}
	
	protected class LoadMapRunnable implements Runnable{
		ArrayList<PageObject> tempGames;
		public LoadMapRunnable(ArrayList<PageObject> inArray){
			tempGames = inArray;
		}
		
		public void run(){
			games.addAll(tempGames);
			for(int page = 0; page<tempGames.size(); page++){
				PageObject v = tempGames.get(page);
				Double latp = Double.valueOf(v.requestInfo("locationlat"));
				Double lonp = Double.valueOf(v.requestInfo("locationlng"));
				GeoPoint p1 = new GeoPoint((int)(latp.floatValue()*1E6), (int) (lonp.floatValue()*1E6));
				MapItem overlayitem = new MapItem(p1, v.requestInfo("name"), v.requestInfo("locationstreet"));
				overlayitem.setObject(v);
				itemizedoverlay.addOverlay(overlayitem);
			}
			if(itemizedoverlay.shouldAdd() && !mapOverlays.contains(itemizedoverlay)){
				mapOverlays.add(itemizedoverlay);
			}
			mapView.postInvalidate();
			if(games.size()==0 && tempGames.size()==0){
				//makeDialog("No games near location.","",null);
				findViewById(R.id.errormsg).setVisibility(View.VISIBLE);
			}else{
				findViewById(R.id.errormsg).setVisibility(View.GONE);
			}
			SENDING = false;
		}
	}
	
	public void refresh(){
		loadGames();
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		centerStuff();
	}
	
	public void facebookSignOut(){
		progressRunnable(new Runnable(){
			public void run(){
				sendFacebookRequest();
			}
		},"Signing out...",NOT_CANCELABLE);
	}
	
	public void sendFacebookRequest(){
		if(!DataBaseHelper.isOnline(this,0)){
			RUNNABLE_STATE = RUNNABLE_FAILED;
			return;
		}
		
		try{
			User user = new User("1");
			user.getObject("1", BozukoDataBaseHelper.getSharedInstance(getBaseContext()));
			
			String url = GlobalConstants.BASE_URL + user.requestInfo("linkslogout");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			HttpRequest req = new HttpRequest(new URL(url + "?token=" + mprefs.getString("token", "") + "&mobile_version="+ GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			//Log.v("LOGOUT",req.AutoPlain());
			removeCookies();
			
			mHandler.post(new Runnable(){
				public void run(){
					SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					SharedPreferences.Editor edit = mprefs.edit();
					edit.putBoolean("facebook_login", false);
					edit.putString("token", "");
					edit.putBoolean("ReloadFavorites", true);
					edit.putBoolean("ReloadMap", true);
					edit.putBoolean("ReloadNearby", true);
					edit.putBoolean("ReloadPage", true);
					edit.commit();
					((BozukoApplication)getApp()).getEntry();
					
					Intent bozuko = new Intent(getBaseContext(),SettingsBozukoActivity.class);
					bozuko.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(bozuko);
				}
			});
			RUNNABLE_STATE = RUNNABLE_SUCCESS;
		}catch(Throwable t){
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}
	
	public void removeCookies(){
		try{
			CookieSyncManager.createInstance(this);
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookie();
		}catch(Throwable t){
			
		}
	}

	protected class DisplayThrowable implements Runnable{
		
		Throwable inThrowable;
		
		public DisplayThrowable(Throwable e){
			inThrowable = e;
		}
		
		public void run(){
			makeDialog(inThrowable.getLocalizedMessage() + "\n" + inThrowable.getMessage() + "\n" + inThrowable.toString(),"StackTrace",null);
		}
	}
}
