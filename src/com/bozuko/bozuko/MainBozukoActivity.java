package com.bozuko.bozuko;

import java.net.URL;
import org.json.JSONObject;
import com.bozuko.bozuko.datamodel.Bozuko;
import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.EntryPointObject;
import com.bozuko.bozuko.datamodel.User;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class MainBozukoActivity extends BozukoControllerActivity {
	
	//Timer timer = new Timer();
	
	public void progressRunnableComplete(){
	
		if(isFinishing()){
			return;
		}
		
		Intent myIntent = new Intent();
		myIntent.setClassName("com.bozuko.bozuko","com.bozuko.bozuko.TabController");
		startActivity(myIntent);
		finish();
	}
	
	public void progressRunnableError(){

		if(isFinishing()){
			return;
		}
		
		makeDialog(errorMessage,errorTitle,new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.main);
        getCacheDir();
        ((BozukoApplication)getApp()).searchTerm = "";
        
        unProgressRunnable(new Runnable(){
        	public void run(){
        		sendRequest();
        	}
        });

    }
    
    public void sendRequest(){
    	if(!DataBaseHelper.isOnline(this,0)){
    		errorMessage = "Please check your connection. Could not initialize Bozuko.";
    		errorTitle = "Connection Error";
			RUNNABLE_STATE = RUNNABLE_FAILED;
			return;
		}
		try {
			String url = GlobalConstants.API_URL;
			
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			HttpRequest req = new HttpRequest(new URL(url + "?token=" + mprefs.getString("token", "") + "&mobile_version=" + GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSONError();
			try{
				errorTitle = json.getString("title");
				errorMessage = json.getString("message");
				errorType = json.getString("name");
				RUNNABLE_STATE = RUNNABLE_FAILED;
			}catch(Throwable t){
				EntryPointObject entry = new EntryPointObject(json);
				entry.add("entryid", "1");
				BozukoDataBaseHelper.getSharedInstance(this).eraseTable("entrypoint");
				entry.saveToDb("1", BozukoDataBaseHelper.getSharedInstance(this));
				getBozukoLinks(entry);
				getUserInfo(entry);
				RUNNABLE_STATE = RUNNABLE_SUCCESS;
			}
		} catch (Throwable e) {
			RUNNABLE_STATE = RUNNABLE_FAILED;
			errorMessage = "Could not initialize Bozuko.";
    		errorTitle = "Request Error";
			//e.printStackTrace();
		}
	}
    
    public void getBozukoLinks(EntryPointObject entry){
    	try{
    		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			
    		String url = GlobalConstants.BASE_URL + entry.requestInfo("linksbozuko");
    		HttpRequest req = new HttpRequest(new URL(url + "?token=" + mprefs.getString("token", "") + "&mobile_version=" + GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSONError();
			try{
				errorTitle = json.getString("title");
				errorMessage = json.getString("message");
				errorType = json.getString("name");
				RUNNABLE_STATE = RUNNABLE_FAILED;
			}catch(Throwable t){
				Bozuko bozuko = new Bozuko(json);
				bozuko.add("bozukoid", "1");
				BozukoDataBaseHelper.getSharedInstance(this).eraseTable("bozuko");
				bozuko.saveToDb("1", BozukoDataBaseHelper.getSharedInstance(this));
				RUNNABLE_STATE = RUNNABLE_SUCCESS;
			}
    	}catch(Throwable t){
    		RUNNABLE_STATE = RUNNABLE_FAILED;
			errorMessage = "Could not initialize Bozuko.";
    		errorTitle = "Request Error";
    	}
    }
    
    public void getUserInfo(EntryPointObject entry){
    	try{
			if(entry.checkInfo("linksuser")){
				SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
				
				String url = GlobalConstants.BASE_URL + entry.requestInfo("linksuser");
				HttpRequest req = new HttpRequest(new URL(url + "?token=" + mprefs.getString("token", "") + "&mobile_version=" + GlobalConstants.MOBILE_VERSION));
				req.setMethodType("GET");
				JSONObject json = req.AutoJSONError();
				try{
					json.getString("title");
					
				}catch(Throwable t){
					User user = new User(json);
				
					user.add("userid", "1");
					user.add("bozukoid", user.requestInfo("id"));
					user.remove("id");
					//Log.v("UserObject",user.toString());
					BozukoDataBaseHelper.getSharedInstance(this).eraseTable("user");
					user.saveToDb("1", BozukoDataBaseHelper.getSharedInstance(this));
				}
			}
		}catch(Throwable t){
				
		}
    }
}