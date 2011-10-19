package com.bozuko.bozuko;

import java.net.URL;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.EntryPointObject;
import com.bozuko.bozuko.datamodel.User;
import com.fuzz.android.activities.ControllerActivity;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
import com.fuzz.android.ui.GroupView;
import com.fuzz.android.ui.MenuOption;
import com.fuzz.android.ui.OptionCell;

public class BozukoControllerActivity extends ControllerActivity {

	public String errorType = "";
	
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
	
	public View getTitleView(String inString){
		TextView view = new TextView(this);
		//view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		view.setText(inString);
		view.setBackgroundResource(R.drawable.listheader);
		view.setTextSize(16);
		view.setMaxLines(1);
		view.setTypeface(Typeface.DEFAULT_BOLD);
		view.setTextColor(Color.WHITE);
		return view;
	}
	
	public View getGroupTitleView(String inString){
		TextView view = new TextView(this);
		//view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		view.setText(inString);
		view.setPadding(10, 5, 10, 5);
		//view.setBackgroundResource(R.drawable.listheader);
		view.setTextSize(16);
		view.setMaxLines(1);
		view.setTypeface(Typeface.DEFAULT_BOLD);
		view.setTextColor(Color.BLACK);
		return view;
	}

	public View getSpacer(){
		View tmpView = new View(this);
		tmpView.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT,(int)(20*getResources().getDisplayMetrics().density)));
		return tmpView;
	}

	public View getCellView(String inString,int resource){
		GroupView groupView = new GroupView(this);
		
		TextView textView = new TextView(BozukoControllerActivity.this);
		textView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,(int)(44*getResources().getDisplayMetrics().density)));
		textView.setTextColor(Color.BLACK);
		textView.setTypeface(Typeface.DEFAULT_BOLD);
		textView.setTextSize(16);
		textView.setGravity(Gravity.CENTER);
		groupView.setContentView(textView);
		textView.setText(inString);
		
		groupView.setImage(resource);
		
		return groupView;
	}
	
	public View getLongCellView(String inString,int resource){
		GroupView groupView = new GroupView(this);
		
		TextView textView = new TextView(BozukoControllerActivity.this);
		textView.setPadding(0, 10, 0, 10);
		textView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		textView.setTextColor(Color.GRAY);
		//textView.setTypeface(Typeface.DEFAULT_BOLD);
		textView.setTextSize(14);
		textView.setGravity(Gravity.CENTER);
		groupView.setContentView(textView);
		textView.setText(Html.fromHtml(inString));
		
		groupView.setImage(resource);
		
		return groupView;
	}

	public View getOptionView(MenuOption option,int resource){
		OptionCell optionView = new OptionCell(BozukoControllerActivity.this);
		optionView.display(option,resource);
		return optionView;
	}

	public void invoke(MenuOption option){
		try {
			option.method.invoke(this,(Object[]) null);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}

	public String challengeResponse(String url,String challenge){
		url = url.replace(GlobalConstants.BASE_URL, "");
		return BozukoDataBaseHelper.sha1(url+challenge);
	}

	public void notLoggedIn(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getParent()!=null ? getParent() : this);
		builder.setMessage("Not Logged In")
		       .setCancelable(true)
		       .setTitle("Would you like to log in?")
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		                facebook();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void facebook(){
		//TODO
		EntryPointObject entry = new EntryPointObject("1");
        entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(BozukoControllerActivity.this));
		
        SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(mprefs.getBoolean("facebook_login", false)){
			
		}else{
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linkslogin");
			Intent intent = new Intent(this,SocialMediaWebViewActivity.class);
			intent.setData(Uri.parse(url));
			intent.putExtra("FlurryEvent", "");
			startActivity(intent);
		}
	}
	
	public void facebookSignOut(){
		progressRunnable(new Runnable(){
			public void run(){
				sendFacebookRequest();
			}
		},"Signing out...",NOT_CANCELABLE);
	}
	
	public void sendFacebookRequest(){
		if(!DataBaseHelper.isOnline(this,0)){
			RUNNABLE_STATE = RUNNABLE_FAILED;
			return;
		}
		
		try{
			User user = new User("1");
			user.getObject("1", BozukoDataBaseHelper.getSharedInstance(BozukoControllerActivity.this));
			
			String url = GlobalConstants.BASE_URL + user.requestInfo("linkslogout");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			HttpRequest req = new HttpRequest(new URL(url + "?token=" + mprefs.getString("token", "") + "&mobile_version="+ GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			removeCookies();
			
			mHandler.post(new Runnable(){
				public void run(){
					SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(BozukoControllerActivity.this);
					SharedPreferences.Editor edit = mprefs.edit();
					edit.putBoolean("facebook_login", false);
					edit.putString("token", "");
					edit.putBoolean("ReloadFavorites", true);
					edit.putBoolean("ReloadMap", true);
					edit.putBoolean("ReloadNearby", true);
					edit.putBoolean("ReloadPage", true);
					edit.commit();
					((BozukoApplication)getApp()).getEntry();
					
					Intent bozuko = new Intent(BozukoControllerActivity.this,SettingsBozukoActivity.class);
					bozuko.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(bozuko);
				}
			});
			RUNNABLE_STATE = RUNNABLE_SUCCESS;
		}catch(Throwable t){
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}
	
	public void removeCookies(){
		try{
			CookieSyncManager.createInstance(this);
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookie();
		}catch(Throwable t){
			
		}
	}
	
	public void refresh(){
		
	}

	protected class DisplayThrowable implements Runnable{
		
		Throwable inThrowable;
		
		public DisplayThrowable(Throwable e){
			inThrowable = e;
		}
		
		public void run(){
			//makeDialog(inThrowable.getLocalizedMessage() + "\n" + inThrowable.getMessage() + "\n" + inThrowable.toString(),"StackTrace",null);
		}
	}

	public class SimpleAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	public void onPause(){
		super.onPause();
		((BozukoApplication)getApp()).saveData();
	}
	
	public void onResume(){
		super.onResume();
		((BozukoApplication)getApp()).loadData();
	}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		((BozukoApplication)getApp()).loadData();
	}
}
