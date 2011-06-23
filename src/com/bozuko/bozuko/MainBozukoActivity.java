package com.bozuko.bozuko;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Intent;
import android.os.Bundle;

public class MainBozukoActivity extends BozukoControllerActivity {
	
	final Runnable mUpdateResults = new Runnable() {
		public void run() {	
			if(isFinishing())
				return;
			Intent myIntent = new Intent();
			myIntent.setClassName("com.bozuko.bozuko","com.bozuko.bozuko.GamesTabController");
			startActivity(myIntent);
			finish();
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.main);
        getCacheDir();
        
        Timer n = new Timer();
		TimerTask task = new TimerTask(){

			@Override
			public void run() {
				mHandler.post(mUpdateResults);
			}
			
		};
		n.schedule(task, 3000);
    }
}