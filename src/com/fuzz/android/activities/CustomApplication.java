package com.fuzz.android.activities;

import com.fuzz.android.datahandler.DataApplication;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.location.LocationApplication;
import com.fuzz.android.location.LocationHandler;
import com.fuzz.android.location.LocationHandler.LocationFinderListener;
import com.fuzz.android.socialmedia.FourSquareHandler;
import com.fuzz.android.socialmedia.SocialMediaApplication;
import com.fuzz.android.socialmedia.SocialMediaHandler;
import android.app.Application;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
public class CustomApplication extends Application implements SocialMediaApplication,DataApplication,LocationApplication, LocationFinderListener{
	
	public DataBaseHelper dHandler = null;
	public SocialMediaHandler sHandler = null;
	public LocationHandler lHandler = null;
	public Location curLocation = null;
	
	public void onCreate (){
		super.onCreate();
		SharedPreferences mprefs = PreferenceManager
		.getDefaultSharedPreferences(this);
        if(!mprefs.getBoolean("load", false)){
			DataBaseHelper helper=new DataBaseHelper(this);
			try {
				helper.createDataBase();
			} catch (Throwable e) {
				e.printStackTrace();
				helper.getWritableDatabase();
			}
			helper.openDataBase();
			helper.addTable();
			helper.setAppStateForDevice();
			helper.close();
			SharedPreferences.Editor editor = mprefs.edit();
			editor.putBoolean("load", true);
			editor.commit();
		}
        getCacheDir();
        
        if(mprefs.getBoolean("LocationActive", true)){
        	startLocation();
        }
	}

	public void onTerminate (){
		super.onTerminate();
		
		if(lHandler != null){
			lHandler.cancel();
		}
		
		if(dHandler != null){
			dHandler.close();
			dHandler.setContext(null);
		}
	}

	@Override
	public SocialMediaHandler getSocialMediaHandler() {
		// TODO Auto-generated method stub
		if(sHandler == null){
			sHandler = new FourSquareHandler(this);
		}
		
		return sHandler;
	}

	@Override
	public DataBaseHelper getDataBase() {
		// TODO Auto-generated method stub
		if(dHandler == null){
			dHandler = new DataBaseHelper(this);
			dHandler.openDataBase();
		}
		
		return dHandler;
	}

	@Override
	public Location getCurrentLocation() {
		// TODO Auto-generated method stub
		return curLocation;
	}

	@Override
	public void endLocation() {
		// TODO Auto-generated method stub
		if(lHandler != null){
			lHandler.cancel();
		}
		
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor edit = mprefs.edit();
		edit.putBoolean("LocationActive",false);
		edit.commit();
	}

	@Override
	public boolean oneTime() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startLocation() {
		// TODO Auto-generated method stub
		if(lHandler == null){
			lHandler = new LocationHandler(this);
			lHandler.setLocationFinderListener(this);
		}
		
		SharedPreferences mprefs = PreferenceManager
		.getDefaultSharedPreferences(this);
		SharedPreferences.Editor edit = mprefs.edit();
		edit.putBoolean("LocationActive",true);
		edit.commit();
		
		lHandler.start(this);
	}

	@Override
	public void LocationFailed() {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void LocationFound(Location l) {
		// TODO Auto-generated method stub
		curLocation = l;
		//Log.v("TAG",curLocation.toString());
		
		if(oneTime()){
			lHandler.cancel();
		}
	}
	
}
