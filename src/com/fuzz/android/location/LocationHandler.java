package com.fuzz.android.location;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

public class LocationHandler{

	private final static int NOT_RUNNING = 0;
	private final static int RUNNING = 1;
	private int STATE = NOT_RUNNING;
	
	LocationManager lm;
	public Location loc;
	LocationListener agps = new LocationListenerAGPS();
	LocationListener gps = new LocationListenerAGPS();

	public LocationHandler(Context context){
		lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
	}

	public void cancel(){
		lm.removeUpdates(agps);
		lm.removeUpdates(gps);
		STATE = NOT_RUNNING;
	}
	
	public boolean isLocationEnabled(){
		boolean gpsen = false;
		boolean neten = false;
		try{
			gpsen = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}catch (IllegalArgumentException e){

		}
		try{
			neten = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}catch( IllegalArgumentException e){

		}
		if(!gpsen && !neten){
			return false;
		}
		
		return true;
	}

	LocationFinderListener delegate;

	public void setLocationFinderListener(LocationFinderListener listener){
		delegate = listener;
	}

	public void start(Context l){
		//loc = null;
		if(STATE == RUNNING){
			if(!isLocationEnabled()){
				loc = null;
				if(loc == null){
					if(delegate != null){
						STATE = NOT_RUNNING;
						delegate.LocationFailed();
					}
				}
			}else{
				getLastKnownLocation();
			}
		
			return;
		}
		STATE = RUNNING;
		if(!isLocationEnabled()){
			STATE = NOT_RUNNING;
			loc = null;
			if(loc == null){
				if(delegate != null){
					delegate.LocationFailed();
				}
			}
		}else{
			try{
				
				int failcount = 0;
				try{
					lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000*20, 5000, agps,l.getMainLooper());
				}catch(Exception e1){
					failcount++;
				}
				
				try{
					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000*20, 5000, gps,l.getMainLooper());
				}catch(Exception e){
					failcount++;
				}
				
				if(failcount==2){
					STATE = NOT_RUNNING;
					loc = null;
					if(loc == null)
						throw new Exception("Failed to get location");
				}else{
					getLastKnownLocation();
					TimerTask task = new TimerTask(){
						public void run(){
							if(loc == null){
								STATE = NOT_RUNNING;
								if(delegate != null){
									delegate.LocationFailed();
								}
							}
						}
					};
					Timer timer = new Timer();
					timer.schedule(task, 1000 * 20);
				
				}
			}catch(Exception e){
				STATE = NOT_RUNNING;
				//TODO called failed
				if(delegate != null){
					delegate.LocationFailed();
				}
			}
		}
	}

	public void getLastKnownLocation(){
		if(loc != null){
			if(delegate != null){
				delegate.LocationFound(loc);
			}
			return;
		}
		
		try{
			loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}catch(Exception e){
			try{
				loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}catch(Exception e1){

			}
		}
		if(loc == null){
			try{
				loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}catch(Exception e1){

			}
		}
		if(loc != null){
			if(delegate != null){
				delegate.LocationFound(loc);
			}
		}
	}
	
	public class LocationListenerGPS implements LocationListener{
	
	@Override
	public void onLocationChanged(Location argLocation) {
		// TODO Auto-generated method stub
		loc = argLocation;
		if(delegate != null){
			delegate.LocationFound(loc);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		if(status == LocationProvider.OUT_OF_SERVICE){
			if(delegate != null && loc == null){
				delegate.LocationFailed();
			}
		}else if(status == LocationProvider.TEMPORARILY_UNAVAILABLE){
			if(delegate != null && loc == null){
				delegate.LocationFailed();
			}
		}else{
			
		}
	}
	
	}
	
	public class LocationListenerAGPS implements LocationListener{
		
		@Override
		public void onLocationChanged(Location argLocation) {
			// TODO Auto-generated method stub
			loc = argLocation;
			if(delegate != null){
				delegate.LocationFound(loc);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			if(status == LocationProvider.OUT_OF_SERVICE){
				if(delegate != null && loc == null){
					delegate.LocationFailed();
				}
			}else if(status == LocationProvider.TEMPORARILY_UNAVAILABLE){
				if(delegate != null && loc == null){
					delegate.LocationFailed();
				}
			}else{
				
			}
		}
		
		}
	
	public interface LocationFinderListener{
		public void LocationFound(Location l);

		public void LocationFailed();
	}
}
