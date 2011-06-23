package com.fuzz.android.location;

import android.location.Location;

public interface LocationApplication {
	
	public Location getCurrentLocation();
	
	public void startLocation();
	
	public void endLocation();
	
	public boolean oneTime();
}
