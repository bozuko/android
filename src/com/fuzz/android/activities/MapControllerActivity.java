package com.fuzz.android.activities;

import com.flurry.android.FlurryAgent;
import com.fuzz.android.globals.Res;
import com.google.android.maps.MapActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

@SuppressWarnings("static-access")
public class MapControllerActivity extends MapActivity implements OnCancelListener {

	public final static int OTHER_ACTIVITY = 997;
	public final static int REFRESHABLE_ACTIVITY = 998;
	public final static int AUTOCLOSEABLE_ACTIVITY = 999;
	public final static int SEARCHABLE_ACTIVITY = 996;
	public int type = OTHER_ACTIVITY;
	public boolean isRefreshable = false;
	
	public int STATE = 0;
	public final static int STATE_PAUSED = 1;
	public final static int STATE_ACTIVE = 2;
	
	public int RUNNABLE_STATE = RUNNABLE_RUNNING;
	public final static int RUNNABLE_FAILED = 1;
	public final static int RUNNABLE_SUCCESS = 0;
	public final static int RUNNABLE_RUNNING = 2;
	public final static int RUNNABLE_STOPPED = 3;
	
	public final static int CANCELABLE = 899;
	public final static int NOT_CANCELABLE = 898;
	public final static int CLOSEABLE = 897;
	protected String progressMessage = "";
	
	public String FLURRY_EVENT = "";
	public final static String FLURRY_KEY = "VMLSWLVEXVCM1AYV4GYG";
	
	protected Handler mHandler = new Handler();
	protected Thread runnable;
	
	protected String errorMessage = "Unable to connect to the internet";
	protected String errorTitle = "No Connection";
	
	
	protected Runnable mUpdateResults = new Runnable(){
		public void run(){
			closeDialogs();
			RUNNABLE_STATE = RUNNABLE_STOPPED;
			progressRunnableComplete();
		}
	};
	
	public void closeDialogs(){
		try{
			dismissDialog(CANCELABLE);
		}catch(Throwable t){
			
		}
		try{
			dismissDialog(NOT_CANCELABLE);
		}catch(Throwable t){
			
		}
		
		try{
			dismissDialog(CLOSEABLE);
		}catch(Throwable t){
			
		}
	}

	protected Runnable mUpdateError = new Runnable(){
		public void run(){
			closeDialogs();
			RUNNABLE_STATE = RUNNABLE_STOPPED;
			progressRunnableError();
		}
	};
	protected Runnable refresh = new Runnable(){
		public void run(){
			refresh();
		}
	};
	
	protected void refresh(){
		
	}
	
	public CustomApplication getApp(){
		return (CustomApplication) super.getApplication();
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		 getWindow().setFormat(PixelFormat.RGBA_8888);
        super.onCreate(savedInstanceState);
        STATE = STATE_ACTIVE;
        setContentView(Res.layout.toplayer);
        
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	
	protected void progressRunnableComplete() {
		// TODO Auto-generated method stub
		if(isFinishing()){
			return;
		}
	}
	
	protected void progressRunnableError() {
		if(isFinishing()){
			return;
		}
		makeDialog(errorMessage,errorTitle,null);
	}
	
	public void makeDialog(String message, String title, DialogInterface.OnClickListener listener){
		if(isFinishing())
			return;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getParent()!=null ? getParent() : this);
		builder.setMessage(message)
		       .setCancelable(true)
		       .setTitle(title)
		       .setPositiveButton("OK", (listener == null ? new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       } : listener));
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void setTitle(String title){
		try{
			((TextView)findViewById(Res.id.title)).setText(title);
		}catch(Exception e){
		}
	}
	
	public void setContent(View v){
		v.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		((FrameLayout)findViewById(Res.id.ContentView)).removeAllViews();
		((FrameLayout)findViewById(Res.id.ContentView)).addView(v);
	}
	
	public void setContent(int resource){
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(resource, null);
		setContent(v);
	}
	
	public void setHeader(View v){
		v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		((LinearLayout)findViewById(Res.id.Header)).removeAllViews();
		((LinearLayout)findViewById(Res.id.Header)).addView(v);
		((LinearLayout)findViewById(Res.id.Header)).setVisibility(View.VISIBLE);
	}
	
	public void setHeader(int resource){
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(resource,null);
		setHeader(v);
	}

	public void progressRunnable(final Runnable r,String message, int type){
		if(isFinishing())
			return;
		progressMessage = message;
		showDialog(type);
		RUNNABLE_STATE = RUNNABLE_RUNNING;
		runnable = new Thread(){
			public void run(){
				try{
					r.run();
					runnable = null;
					if(RUNNABLE_STATE != RUNNABLE_SUCCESS && RUNNABLE_STATE != RUNNABLE_FAILED)
						RUNNABLE_STATE = RUNNABLE_SUCCESS;
					
					if(RUNNABLE_STATE == RUNNABLE_SUCCESS){
						mHandler.post(mUpdateResults);
					}else{
						mHandler.post(mUpdateError);
					}
				}catch(Throwable r){
					//r.printStackTrace();
					runnable = null;
					mHandler.post(mUpdateError);
				}
			}
		};
		runnable.start();
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog){
		 switch(id) {
		    case CANCELABLE:
		    	((ProgressDialog)dialog).setMessage(progressMessage);
		    	break;
		    case NOT_CANCELABLE:
		    	((ProgressDialog)dialog).setMessage(progressMessage);
		    	break;
		    case CLOSEABLE:
		    	((ProgressDialog)dialog).setMessage(progressMessage);
		    	break;
		 }
	}
	
	protected void onPrepareDialog (int id, Dialog dialog, Bundle args){
		onPrepareDialog(id,dialog);
	}
	
	protected Dialog onCreateDialog (int id, Bundle args){
		return onCreateDialog(id);
	}
	
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
	    switch(id) {
	    	case CANCELABLE:
	    		dialog = ProgressDialog.show(getParent()!=null ? getParent() : this, "", "", true, true);
	    		dialog.setOnCancelListener(this);
	    	break;
	    	case NOT_CANCELABLE:
	    		dialog = ProgressDialog.show(getParent()!=null ? getParent() : this, "", "", true, false);
	    	break;
	    	case CLOSEABLE:
	    		dialog = ProgressDialog.show(getParent()!=null ? getParent() : this, "", "", true, true);
	    		dialog.setOnCancelListener(new OnCancelListener(){
					@Override
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				});
	    	break;
	    }
	    return dialog;
	}

	
	public void unProgressRunnable(final Runnable r){
		RUNNABLE_STATE = RUNNABLE_RUNNING;
		runnable = new Thread(){
			public void run(){
				try{
					r.run();
					runnable = null;
					if(RUNNABLE_STATE != RUNNABLE_SUCCESS && RUNNABLE_STATE != RUNNABLE_FAILED)
						RUNNABLE_STATE = RUNNABLE_SUCCESS;
					
					if(RUNNABLE_STATE == RUNNABLE_SUCCESS){
						mHandler.post(mUpdateResults);
					}else{
						mHandler.post(mUpdateError);
					}
				}catch(Throwable r){
					//r.printStackTrace();
					runnable = null;
					mHandler.post(mUpdateError);
				}
			}
		};
		runnable.start();
	}

	public void error(String message){
		//TODO make error message and display
		setContent(Res.layout.error);
		((TextView)findViewById(Res.id.errormsg)).setText(message);
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		if(type == REFRESHABLE_ACTIVITY || isRefreshable){
			menu.add(0, REFRESHABLE_ACTIVITY, 0, "Refresh").setIcon(Res.drawable.ic_menu_refresh);
		}
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case REFRESHABLE_ACTIVITY:
				mHandler.post(refresh);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onResume(){
		//InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		//imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		STATE = STATE_ACTIVE;
		//findViewById(R.id.ContentView).invalidate();
		if(RUNNABLE_STATE != RUNNABLE_STOPPED && RUNNABLE_STATE != RUNNABLE_RUNNING){
			
		}
		
		if(runnable != null){
			if(!runnable.isAlive()){
				runnable = null;
			}
		}
		
		if(FLURRY_EVENT.compareTo("")!=0){
			FlurryAgent.onEvent(FLURRY_EVENT);
		}
		
		super.onResume();
	}
	
	public void onStart(){ 
		FlurryAgent.onStartSession(this, FLURRY_KEY);
		STATE = STATE_ACTIVE;
		super.onStart();
	}
	
	public void onPause(){
		STATE = STATE_PAUSED;
		super.onPause();
	}
	
	public void onStop(){
		FlurryAgent.onEndSession(this);
		STATE = STATE_PAUSED;
		super.onStop();
	}
	
	public void onDestroy(){
		STATE = STATE_PAUSED;
		super.onDestroy();
	}
	
	public void RemoveAllViews(){
		((LinearLayout)findViewById(Res.id.ContentView)).removeAllViews();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		if(shouldDisplayError()){
			error("Loading");
		}
		
		Toast.makeText(this, "Will continue in the background", Toast.LENGTH_SHORT).show();
	}

	protected boolean shouldDisplayError() {
		// TODO Auto-generated method stub
		return false;
	}

	 protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
         if (requestCode == AUTOCLOSEABLE_ACTIVITY) {
             if (resultCode == RESULT_OK) {
            	 setResult(RESULT_OK);
            	 finish();
             }
         }
     }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
