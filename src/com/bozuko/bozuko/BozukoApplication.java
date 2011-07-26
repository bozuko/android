package com.bozuko.bozuko;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.json.JSONObject;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.bozuko.bozuko.datamodel.Bozuko;
import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.EntryPointObject;
import com.bozuko.bozuko.datamodel.GameObject;
import com.bozuko.bozuko.datamodel.PageObject;
import com.bozuko.bozuko.datamodel.User;
import com.fuzz.android.activities.CustomApplication;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;

public class BozukoApplication extends CustomApplication {

	public PageObject currentPageObject;
	public GameObject currentGameObject;
	
	public String searchTerm = "";
	
	@Override
	public void LocationFailed() {
		// TODO Auto-generated method stub
		
		if(!lHandler.isLocationEnabled()){
			endLocation();
			try{
				Toast.makeText(this, "Check you gps and network connections.", Toast.LENGTH_LONG).show();
			}catch(Throwable t){
				
			}
		}else if(lHandler.loc == null){
			endLocation();
			try{
				Toast.makeText(this, "Your current location is temporarily unavailable", Toast.LENGTH_LONG).show();
			}catch(Throwable t){
				
			}
		}
		
		Intent broad = new Intent("LOCATIONSUPDATED");
		broad.putExtra("FAIL", true);
		sendBroadcast(broad);
	}

	@Override
	public void LocationFound(Location l) {
		super.LocationFound(l);
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor edit = mprefs.edit();
		if(mprefs.getBoolean("LocationActive", false)){
			edit.putString("lat", String.valueOf(l.getLatitude()));
			edit.putString("lon", String.valueOf(l.getLongitude()));
			edit.putString("clat", String.valueOf(l.getLatitude()));
			edit.putString("clon", String.valueOf(l.getLongitude()));
			//edit.putString("zipcode", "");
			Thread th = new Thread(){
				public void run(){
					SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					try{
						Geocoder geoCoder = new Geocoder(getBaseContext());
						Location cur = getCurrentLocation();
						List<Address> addresses;
						//String zipcode = mprefs.getString("zipcode", "");
						try {
							addresses = geoCoder.getFromLocation(cur.getLatitude(), cur.getLongitude(), 1);
							Address address = addresses.get(0);
							SharedPreferences.Editor edit = mprefs.edit();
							edit.putString("zipcode", address.getPostalCode());
							edit.putString("location", address.getLocality() + ", " + address.getAdminArea());
							edit.commit();
							//if(zipcode.compareTo(address.getPostalCode())!=0){
								Intent broad = new Intent("LOCATIONSUPDATED");
								broad.putExtra("FAIL", false);
								sendBroadcast(broad);
							//}
						} catch (IOException e) {
							SharedPreferences.Editor edit = mprefs.edit();
							edit.putString("zipcode", "Current Location");
							edit.putString("location", "");
							edit.commit();
							Intent broad = new Intent("LOCATIONSUPDATED");
							broad.putExtra("FAIL", false);
							sendBroadcast(broad);
							e.printStackTrace();
						}
					}catch(Throwable t){
						SharedPreferences.Editor edit = mprefs.edit();
						edit.putString("zipcode", "Current Location");
						edit.putString("location", "");
						edit.commit();
						Intent broad = new Intent("LOCATIONSUPDATED");
						broad.putExtra("FAIL", false);
						sendBroadcast(broad);
					}
				}
			};
			th.start();
		}else{
			edit.putString("clat", String.valueOf(l.getLatitude()));
			edit.putString("clon", String.valueOf(l.getLongitude()));
		}
		edit.commit();		
	}

	@Override
	public void onCreate (){
		super.onCreate();

		//getEntry();
		startLocation();
	}
	
	public void getEntry(){
		Thread th = new Thread(){
			public void run(){
				sendRequest();
				Intent intent = new Intent("SETTINGSUPDATED");
				sendBroadcast(intent);
			}
		};
		th.start();
	}

	public void sendRequest(){
		if(!DataBaseHelper.isOnline(this)){
			return;
		}
		try {
			String url = GlobalConstants.API_URL;
			
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			HttpRequest req = new HttpRequest(new URL(url + "?token=" + mprefs.getString("token", "") + "&mobile_version=" + GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSON();
			EntryPointObject entry = new EntryPointObject(json);
			entry.add("entryid", "1");
			BozukoDataBaseHelper.getSharedInstance(this).eraseTable("entrypoint");
			entry.saveToDb("1", BozukoDataBaseHelper.getSharedInstance(this));
			
			url = GlobalConstants.BASE_URL + entry.requestInfo("linksbozuko");
			req = new HttpRequest(new URL(url + "?token=" + mprefs.getString("token", "") + "&mobile_version=" + GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			json = req.AutoJSON();
			Bozuko bozuko = new Bozuko(json);
			bozuko.add("bozukoid", "1");
			BozukoDataBaseHelper.getSharedInstance(this).eraseTable("bozuko");
			bozuko.saveToDb("1", BozukoDataBaseHelper.getSharedInstance(this));
			
			if(entry.checkInfo("linksuser")){
				url = GlobalConstants.BASE_URL + entry.requestInfo("linksuser");
				req = new HttpRequest(new URL(url + "?token=" + mprefs.getString("token", "") + "&mobile_version=" + GlobalConstants.MOBILE_VERSION));
				req.setMethodType("GET");
				json = req.AutoJSON();
				User user = new User(json);
				
				user.add("userid", "1");
				user.add("bozukoid", user.requestInfo("id"));
				user.remove("id");
				Log.v("UserObject",user.toString());
				BozukoDataBaseHelper.getSharedInstance(this).eraseTable("user");
				user.saveToDb("1", BozukoDataBaseHelper.getSharedInstance(this));
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTerminate(){
		super.onTerminate();
	}
}
