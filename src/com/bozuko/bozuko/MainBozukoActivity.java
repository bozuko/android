package com.bozuko.bozuko;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;
import com.bozuko.bozuko.datamodel.Bozuko;
import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.EntryPointObject;
import com.bozuko.bozuko.datamodel.User;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class MainBozukoActivity extends BozukoControllerActivity {
	
	Timer timer = new Timer();
	
	public void progressRunnableComplete(){
		try{
		timer.cancel();
		timer.purge();
		timer = null;
		}catch(Throwable t){
			
		}
		if(isFinishing()){
			return;
		}
		
		Intent myIntent = new Intent();
		myIntent.setClassName("com.bozuko.bozuko","com.bozuko.bozuko.GamesTabController");
		startActivity(myIntent);
		finish();
	}
	
	public void progressRunnableError(){
		if(isFinishing()){
			if(timer != null){
				try{
				timer.cancel();
				timer.purge();
				timer = null;
				}catch(Throwable t){
					
				}
			}
			
			return;
		}
		if(timer == null){
			makeDialog("Please check your connection. Could not initialize Bozuko.","Connection Error",new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			});
		}else{
			 unProgressRunnable(new Runnable(){
		        	public void run(){
		        		sendRequest();
		        	}
		        });
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.main);
        getCacheDir();
        ((BozukoApplication)getApp()).searchTerm = "";
        ((BozukoApplication)getApp()).getEntry();
        
//        Timer n = new Timer();
//		TimerTask task = new TimerTask(){
//
//			@Override
//			public void run() {
//				mHandler.post(mUpdateResults);
//			}
//			
//		};
//		n.schedule(task, 3000);
        
        unProgressRunnable(new Runnable(){
        	public void run(){
        		sendRequest();
        	}
        });
        timer.schedule(new TimerTask(){
        	public void run(){
        		timer.cancel();
        		timer.purge();
        		timer = null;
        	}
        }, 15000);
    }
    
    public void sendRequest(){
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
			
			try{
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
			}catch(Throwable t){
				
			}
			
			RUNNABLE_STATE = RUNNABLE_SUCCESS;
		} catch (Throwable e) {
			RUNNABLE_STATE = RUNNABLE_FAILED;
			e.printStackTrace();
		}
	}
}