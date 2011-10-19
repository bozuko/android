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

	
	Timer endLocation = null;
	
	public void cancel(){
		endLocation = new Timer();
		endLocation.schedule(new TimerTask(){
			public void run(){
				lm.removeUpdates(agps);
				lm.removeUpdates(gps);
				STATE = NOT_RUNNING;
				try{
					endLocation.cancel();
					endLocation.purge();
					endLocation = null;
					}catch(Throwable t){
						
					}
				endLocation = null;
			}
		}, 10000);
		
		
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
			if(endLocation!=null){
				try{
				endLocation.cancel();
				endLocation.purge();
				endLocation = null;
				}catch(Throwable t){
					
				}
				endLocation = null;
			}
			
			if(!isLocationEnabled()){
				loc = null;
				if(loc == null){
					if(delegate != null){
						STATE = NOT_RUNNING;
						delegate.LocationFailed();
					}
				}
			}else{
                if(loc != null){
                    getLastKnownLocation();
                }
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
					lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000*1, 500, agps,l.getMainLooper());
				}catch(Exception e1){
					failcount++;
				}
				
				try{
					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000*5, 500, gps,l.getMainLooper());
				}catch(Exception e){
					failcount++;
				}
				
				if(failcount==2){
					STATE = NOT_RUNNING;
					loc = null;
					if(loc == null)
						throw new Exception("Failed to get location");
				}else{
					
					TimerTask task = new TimerTask(){
						public void run(){
							
							if(loc == null){
								getLastKnownLocation();
								
								STATE = NOT_RUNNING;
								if(delegate != null){
                                    if(loc == null){
                                        STATE = NOT_RUNNING;
                                        delegate.LocationFailed();  
                                    }
								}
							}else{
								delegate.LocationFound(loc);
							}
						}
					};
					Timer timer = new Timer();
					timer.schedule(task, 1000 * 10);
				
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
		if(isBetterLocation(argLocation,loc)){
            if(loc == null){
                if(delegate != null && argLocation.getAccuracy()<500){
                    loc = argLocation;
                    delegate.LocationFound(loc);
                } 
                return;
            }
            
            loc = argLocation;
			if(delegate != null){
				delegate.LocationFound(loc);
			}
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
			if(isBetterLocation(argLocation,loc)){
                if(loc == null){
                    if(delegate != null && argLocation.getAccuracy()<500){
                        loc = argLocation;
                        delegate.LocationFound(loc);
                    } 
                    return;
                }
				loc = argLocation;
				if(delegate != null){
                    delegate.LocationFound(loc);
                }
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


	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
}
