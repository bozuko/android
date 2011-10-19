package com.bozuko.bozuko;

import java.net.URL;

import org.json.JSONObject;
import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.User;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class FeedbackBozukoActivity extends BozukoControllerActivity implements OnEditorActionListener, OnClickListener {

	String url;
	boolean doCheck = true;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent(R.layout.feedback);
		setHeader(R.layout.detailsubmitheader);
		url = getIntent().getStringExtra("URL");
		doCheck = getIntent().getBooleanExtra("DoInputCheck", true);
		
		EditText feedback = (EditText)findViewById(R.id.feedback);
		feedback.setHint(getIntent().getStringExtra("HintText"));
		
		feedback.requestFocus();
		
		
		findViewById(R.id.submit).setOnClickListener(this);
	}
	
	public void onResume(){
		super.onResume();
		
		EditText feedback = (EditText)findViewById(R.id.feedback);
		
		feedback.requestFocus();
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(feedback, 0);
	}

	public void feedback(final String string){
		if(doCheck){
			if(string.trim().compareTo("")==0){
				makeDialog("Please enter a message","Input Error",null);
			}else{
				progressRunnable(new Runnable(){
					public void run(){
						sendRequest(string);
					}
				},"Sending...",CANCELABLE);
			}
		}else{
			progressRunnable(new Runnable(){
				public void run(){
					sendRequest(string);
				}
			},"Sending...",CANCELABLE);
		}
	}
	
	public void sendRequest(String message){
		if(!DataBaseHelper.isOnline(this,0)){
    		RUNNABLE_STATE = RUNNABLE_FAILED;
    		errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
    		return;
		}
		try {
			String request = GlobalConstants.BASE_URL + url;
			TelephonyManager mTelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);  
			String phone_id = mTelephonyMgr.getDeviceId(); // Requires
			
			User user = new User("1");
			user.getObject("1", BozukoDataBaseHelper.getSharedInstance(this));
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			HttpRequest req = new HttpRequest(new URL(request));
			req.add("token", mprefs.getString("token", ""));
			req.add("message", message);
			req.add("phone_type", "android");
			req.add("phone_id", phone_id);
			req.add("mobile_version", GlobalConstants.MOBILE_VERSION);
			req.add("challenge_response", challengeResponse(request,user.requestInfo("challenge")));
			req.add("ll", mprefs.getString("clat","0.00")+","+mprefs.getString("clon","0.00"));
			if(request.contains("feedback")){
				req.setMethodType("PUT");
				
				JSONObject json = req.AutoJSONError();
				try{
					if(json.getBoolean("success")){
						RUNNABLE_STATE = RUNNABLE_SUCCESS;
					}else{
						errorTitle = "Request Failed";
						errorMessage = "Unable to check you in.";
						RUNNABLE_STATE = RUNNABLE_FAILED;
					}
				}catch(Throwable t){
					errorTitle = json.getString("title");
					errorMessage = json.getString("message");
					errorType = json.getString("name");
					RUNNABLE_STATE = RUNNABLE_FAILED;
				}
			}else{
				String string = req.AutoPlain();
				if(req.conn.getResponseCode()!= 200){
					JSONObject json = new JSONObject(string);
					errorTitle = json.getString("title");
					errorMessage = json.getString("message");
					errorType = json.getString("name");
					RUNNABLE_STATE = RUNNABLE_FAILED;
				}else{
					//@SuppressWarnings("unused")
					//JSONArray array = new JSONArray(string);
					RUNNABLE_STATE = RUNNABLE_SUCCESS;
				}
				
				
			}
			
			
		} catch (Throwable e) {
			mHandler.post(new DisplayThrowable(e));
			//e.printStackTrace();
			errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}
	
	public void progressRunnableComplete(){
		if(isFinishing()){
			return;
		}
		finish();
	}
	
	public void progressRunnableError(){
		if(isFinishing()){
			return;
		}
		if(errorType.compareTo("facebook/auth")==0){
			makeDialog(errorMessage,errorTitle,new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					facebookSignOut();
					finish();
				}
			});
		}else if(errorType.compareTo("auth/mobile")==0){
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			if(mprefs.getBoolean("facebook_login", false)){
				((BozukoApplication)getApp()).getUser();
			}
			
			makeDialog(errorMessage,errorTitle,null);
		}else{
			makeDialog(errorMessage,errorTitle,null);
		}
	}

	@Override
	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		EditText feedback = (EditText)findViewById(R.id.feedback);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		feedback(feedback.getText().toString());
	}
}
