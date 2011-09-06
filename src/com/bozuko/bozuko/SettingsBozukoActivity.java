package com.bozuko.bozuko;

import java.net.URL;
import com.bozuko.bozuko.datamodel.Bozuko;
import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.EntryPointObject;
import com.bozuko.bozuko.datamodel.User;
import com.bozuko.bozuko.views.UserView;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.globals.GlobalFunctions;
import com.fuzz.android.http.HttpRequest;
import com.fuzz.android.ui.GroupView;
import com.fuzz.android.ui.MenuOption;
import com.fuzz.android.ui.MergeAdapter;
import com.fuzz.android.ui.OptionCell;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SettingsBozukoActivity extends BozukoControllerActivity implements OnItemClickListener {

	public View getFriendView(){
		GroupView groupView = new GroupView(this);

		User user = new User("1");
		
		try{
			user.getObject("1", BozukoDataBaseHelper.getSharedInstance(this));
		}catch(Throwable t){
			
		}
		
		UserView pageView = new UserView(this);
		pageView.display(user);
		groupView.setContentView(pageView);
		
		groupView.setImage(R.drawable.cellbutton);
		
		return groupView;
	}
	
	public View getGreenCellView(String inString){
		GroupView groupView = new GroupView(this);
		
		TextView textView = new TextView(this);
		textView.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT,(int)(44*getResources().getDisplayMetrics().density)));
		textView.setTextColor(Color.BLACK);
		textView.setTypeface(Typeface.DEFAULT_BOLD);
		textView.setTextSize(16);
		textView.setGravity(Gravity.CENTER);
		groupView.setContentView(textView);
		textView.setText(inString);
		
		groupView.setImage(R.drawable.cellbutton);
		groupView.setImageColor(Color.GREEN);
		
		return groupView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent(R.layout.listview);
		setHeader(R.layout.detailheader);
	}

	public void onResume(){
		registerReceiver(mReceiver, new IntentFilter("SETTINGSUPDATED"));
		setupView();
		super.onResume();
	}
	
	public void setupView(){
		ListView listview = (ListView)findViewById(R.id.ListView01);
		listview.setBackgroundColor(Color.argb(255, 205, 205, 205));
		listview.setCacheColorHint(Color.argb(255, 205, 205, 205));
		listview.setSelector(R.drawable.blank);
		listview.setDividerHeight(0);
		MergeAdapter mergeAdapter = new MergeAdapter();
		mergeAdapter.addView(getGroupTitleView("Profile"),false);
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(mprefs.getBoolean("facebook_login", false)){
			mergeAdapter.addView(getFriendView(),false);
			mergeAdapter.addView(getSpacer(),false);
			try{
				MenuOption option = new MenuOption(R.drawable.facebookicon, "Facebook Log Out", "", false, this.getClass().getMethod("facebook", (Class<?>[])null));
				mergeAdapter.addView(getOptionView(option,R.drawable.cellbutton),true);
			}catch(Throwable t){
				
			}
		}else{
			mergeAdapter.addView(getCellView("Please login with Facebook to create your Bozuko account",R.drawable.cellbutton),false);
			mergeAdapter.addView(getSpacer(),false);
			try{
				MenuOption option = new MenuOption(R.drawable.facebookicon, "Facebook Log In", "", false, this.getClass().getMethod("facebook", (Class<?>[])null));
				mergeAdapter.addView(getOptionView(option,R.drawable.cellbutton),true);
			}catch(Throwable t){
				
			}
		}
		
		mergeAdapter.addView(getGroupTitleView("Bozuko"),false);
		mergeAdapter.addView(getCellView("How to Play",R.drawable.cellbutton),true);
		mergeAdapter.addView(getSpacer(),false);
		mergeAdapter.addView(getCellView("About Bozuko",R.drawable.cellbutton),true);
		mergeAdapter.addView(getSpacer(),false);
		mergeAdapter.addView(getCellView("Bozuko for Business",R.drawable.cellbutton),true);
		mergeAdapter.addView(getSpacer(),false);
		mergeAdapter.addView(getCellView("Privacy Policy",R.drawable.cellbutton),true);
		mergeAdapter.addView(getSpacer(),false);
		mergeAdapter.addView(getCellView("Terms of Use",R.drawable.cellbutton),true);
		
		//Bozuko bozuko = new Bozuko("1");
		//bozuko.getObject("1", BozukoDataBaseHelper.getSharedInstance(this));
		//if(bozuko.checkInfo("linksbozuko_demo_page")){
			mergeAdapter.addView(getSpacer(),false);
			mergeAdapter.addView(getCellView("Demo Games",R.drawable.cellbutton),true);
		//}
		mergeAdapter.addView(getSpacer(),false);
		mergeAdapter.addView(getGreenCellView("Play Our Game!"),true);
		mergeAdapter.addView(getSpacer(),false);
		
		
		mergeAdapter.addView(getSubtitle(GlobalFunctions.getVersionName(this, this.getClass())),false);
		
		listview.setAdapter(mergeAdapter);
		listview.setOnItemClickListener(this);
	}
	
	private View getSubtitle(String inString) {
		// TODO Auto-generated method stub
		
		TextView textView = new TextView(this);
		textView.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		textView.setTextColor(Color.BLACK);
		textView.setTypeface(Typeface.DEFAULT_BOLD);
		textView.setTextSize(7);
		textView.setGravity(Gravity.CENTER);
		textView.setText(inString);
		return textView;
	}

	public void progressRunnableComplete(){
		if(isFinishing()){
			return;
		}
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor edit = mprefs.edit();
		edit.putBoolean("facebook_login", false);
		edit.putString("token", "");
		edit.putBoolean("ReloadFavorites", true);
		edit.putBoolean("ReloadMap", true);
		edit.putBoolean("ReloadNearby", true);
		edit.putBoolean("ReloadPage", true);
		edit.commit();
		setupView();
		((BozukoApplication)getApp()).getEntry();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Bozuko bozuko = new Bozuko("1");
		bozuko.getObject("1", BozukoDataBaseHelper.getSharedInstance(this));
		
		Object object = arg0.getItemAtPosition(arg2);
		if(object.getClass() == OptionCell.class){
			facebook();
		}else{
			GroupView view = (GroupView)object;
			TextView textView = (TextView)view.getContentView();
			String title = textView.getText().toString();
			if(title.compareTo("Privacy Policy")==0){
				openURL(bozuko.requestInfo("linksprivacy_policy"));
			}else if(title.compareTo("About Bozuko")==0){
				openURL(bozuko.requestInfo("linksabout"));
			}else if(title.compareTo("Bozuko for Business")==0){
				openURL(bozuko.requestInfo("linksbozuko_for_business"));
			}else if(title.compareTo("Terms of Use")==0){
				openURL(bozuko.requestInfo("linksterms_of_use"));
			}else if(title.compareTo("Demo Games")==0){
				Intent intent = new Intent(this,PageBozukoActivity.class);
				intent.putExtra("PageLink",bozuko.requestInfo("linksbozuko_demo_page"));
				startActivity(intent);
			}else if(title.compareTo("Play Our Game!")==0){
				//TODO
				Intent intent = new Intent(this,PageBozukoActivity.class);
				intent.putExtra("PageLink",bozuko.requestInfo("linksbozuko_page"));
				startActivity(intent);
			}else if(title.compareTo("How to Play")==0){
				//TODO
				//openURL(bozuko.requestInfo("linkshow_to_play"));
				Intent intent = new Intent(this,AboutBozukoActivity.class);
				startActivity(intent);
			}
		}
	}
	
	public void openURL(String inURL){
		String url = GlobalConstants.BASE_URL + inURL;
		Intent intent = new Intent(this,InAppWebViewControllerActivity.class);
		intent.setData(Uri.parse(url));
		intent.putExtra("FlurryEvent", "");
		startActivity(intent);
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		//menu.add(0, R.drawable.icongames, 0, "Games").setIcon(R.drawable.icongames);
		//menu.add(0, R.drawable.iconprizes, 0, "Prizes").setIcon(R.drawable.iconprizes);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.drawable.icongames:
				Intent games = new Intent(this,GamesTabController.class);
				games.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(games);
				break;
			case R.drawable.iconprizes:
				Intent prizes = new Intent(this,PrizesBozukoActivity.class);
				prizes.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(prizes);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void facebook(){
		//TODO
		EntryPointObject entry = new EntryPointObject("1");
        entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(this));
		
        SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(mprefs.getBoolean("facebook_login", false)){
			unProgressRunnable(new Runnable(){
				public void run(){
					sendRequest();
				}
			});
		}else{
			TelephonyManager mTelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);  
			String phone_id = mTelephonyMgr.getDeviceId(); // Requires
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linkslogin") + "?mobile_version="+GlobalConstants.MOBILE_VERSION + "&phone_type=android&phone_id=" + phone_id;
			Intent intent = new Intent(this,SocialMediaWebViewActivity.class);
			intent.setData(Uri.parse(url));
			intent.putExtra("FlurryEvent", "");
			startActivity(intent);
		}
	}
	
	public void sendRequest(){
		if(!DataBaseHelper.isOnline(this,0)){
			RUNNABLE_STATE = RUNNABLE_FAILED;
			return;
		}
		
		try{
			User user = new User("1");
			user.getObject("1", BozukoDataBaseHelper.getSharedInstance(this));
			
			String url = GlobalConstants.BASE_URL + user.requestInfo("linkslogout");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			HttpRequest req = new HttpRequest(new URL(url + "?token=" + mprefs.getString("token", "") + "&mobile_version="+ GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			//Log.v("LOGOUT",req.AutoPlain());
			removeCookies();
			RUNNABLE_STATE = RUNNABLE_SUCCESS;
		}catch(Throwable t){
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}

	UpdateReceiver mReceiver = new UpdateReceiver();
	public void onPause(){
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	
	protected class UpdateReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			setupView();
		}

	}
}