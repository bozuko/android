package com.bozuko.bozuko;

import java.net.URL;
import org.json.JSONObject;

import com.bozuko.bozuko.datamodel.GameObject;
import com.bozuko.bozuko.datamodel.PageObject;
import com.bozuko.bozuko.views.GameView;
import com.bozuko.bozuko.views.PageHeaderView;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
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
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PageBozukoActivity extends BozukoControllerActivity implements OnItemClickListener{

	PageObject page;
	String pageLink;
	
	public void progressRunnableComplete(){
		setupView();
	}

	public View getGreyCellView(String inString){
		GroupView groupView = new GroupView(this);

		TextView textView = new TextView(getBaseContext());
		textView.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		textView.setTextColor(Color.WHITE);
		textView.setTypeface(Typeface.DEFAULT_BOLD);
		textView.setTextSize(14);
		textView.setGravity(Gravity.CENTER);
		groupView.setContentView(textView);
		textView.setText(inString);

		groupView.setImage(R.drawable.cellbutton);
		groupView.setImageColor(Color.DKGRAY);

		return groupView;
	}

	public View getHeaderView(){
		GroupView groupView = new GroupView(this);


		PageHeaderView pageView = new PageHeaderView(this);
		pageView.display(page);
		groupView.setContentView(pageView);

		if(page.requestInfo("is_place").compareTo("false")==0){
			groupView.setImage(R.drawable.cellbtn);
		}else{
			groupView.setImage(R.drawable.cellbutton);
		}
		

		return groupView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent(R.layout.listview);
		setHeader(R.layout.detailheader);

		page = ((BozukoApplication)getApp()).currentPageObject;

		if(getIntent().hasExtra("PageLink")){
			pageLink = getIntent().getStringExtra("PageLink");
			page = null;
		}else{
			setupView();
		}
	}

	public void setupView(){
		ListView listview = (ListView)findViewById(R.id.ListView01);
		listview.setBackgroundColor(Color.argb(255, 205, 205, 205));
		listview.setCacheColorHint(Color.argb(255, 205, 205, 205));
		listview.setSelector(R.drawable.blank);
		listview.setDividerHeight(0);
		
		listview.setItemsCanFocus(true);
		MergeAdapter mergeAdapter = new MergeAdapter();

		mergeAdapter.addView(getSpacer(), false);
		mergeAdapter.addView(getHeaderView(),true);

		if(page.games.size()>0){
			mergeAdapter.addView(getSpacer(), false);
			mergeAdapter.addAdapter(new GameListAdapter());
			
			if(page.checkInfo("announcement") && page.requestInfo("announcement").compareTo("") != 0){
				mergeAdapter.addView(getSpacer(), false);
				mergeAdapter.addView(getGreyCellView(page.requestInfo("announcement")),false);
			}
		}else{
			mergeAdapter.addView(getSpacer(), false);
			mergeAdapter.addView(getLongCellView("<font color='#000000'><b>Bummer!</b></font><BR>This place has no games.<BR>Recommend Bozuko to this business.",R.drawable.tableviewtopbutton), false);
			mergeAdapter.addView(getCellView("Recommend",R.drawable.tableviewbtmbutton), true);
		}

		
		


		if(page.games.size()>0){
			if(page.checkInfo("linksfeedback") && page.checkInfo("share_url")){
				mergeAdapter.addView(getSpacer(), false);
				mergeAdapter.addView(getCellView("Feedback",R.drawable.tableviewtopbutton), true);
				mergeAdapter.addView(getCellView("Share",R.drawable.tableviewbtmbutton), true);
			}else if(page.checkInfo("linksfeedback")){
				mergeAdapter.addView(getSpacer(), false);
				mergeAdapter.addView(getCellView("Feedback",R.drawable.cellbutton), true);
			}else if(page.checkInfo("share_url")){
				mergeAdapter.addView(getSpacer(), false);
				mergeAdapter.addView(getCellView("Share",R.drawable.cellbutton), true);
			}
		}


		try{
			if(page.checkInfo("linksfacebook_checkin") && page.requestInfo("is_place").compareTo("false")!=0){
				mergeAdapter.addView(getSpacer(), false);
				MenuOption option = new MenuOption(R.drawable.facebookicon, "Facebook Check In", "", false, this.getClass().getMethod("checkIn", (Class<?>[])null));
				mergeAdapter.addView(getOptionView(option,R.drawable.cellbutton),true);
			}
		}catch(Throwable t){

		}
		mergeAdapter.addView(getSpacer(), false);
		listview.setAdapter(mergeAdapter);
		listview.setOnItemClickListener(this);
	}
	
	UpdateReceiver mReceiver = new UpdateReceiver();
	public void onPause(){
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	public void onResume(){
		registerReceiver(mReceiver, new IntentFilter("LIKECHANGED"));
		if(page != null){
			//setupView();
		}else{
			//LOAD DATA
			progressRunnable(new Runnable(){
				public void run(){
					sendRequest();
				}
			},"Loading...",CANCELABLE);
		}
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(mprefs.getBoolean("ReloadPage",false)){
			SharedPreferences.Editor edit = mprefs.edit();
			edit.putBoolean("ReloadPage", false);
			edit.commit();
			if(page.checkInfo("linkspage")){
			progressRunnable(new Runnable(){
				public void run(){
					sendRequest();
				}
			},"Loading...",CANCELABLE);
			}
		}

		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if(arg1.getClass() == OptionCell.class){
			OptionCell cell = (OptionCell)arg1;
			invoke(cell.option);
		}else if(arg1.getClass() == GroupView.class){
			View innerView = ((GroupView)arg1).getContentView();
			if(innerView.getClass() == TextView.class){
				String text = ((TextView)innerView).getText().toString();
				if(text.compareTo("Share")==0){
					Intent sendIntent = new Intent(Intent.ACTION_VIEW);
					sendIntent.setData(Uri.parse("mailto:?subject=Bozuko&body="+page.requestInfo("share_url")));
					startActivity(Intent.createChooser(sendIntent, "Share"));
				}else if(text.compareTo("Feedback")==0){
					SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
					if(mprefs.getBoolean("facebook_login", false)){
						Intent intent = new Intent(this,FeedbackBozukoActivity.class);
						intent.putExtra("URL", page.requestInfo("linksfeedback"));
						intent.putExtra("HintText", "Tell us what you think about this game and press \"Submit\"");
						startActivity(intent);
					}else{
						notLoggedIn();
					}
				}else if(text.compareTo("Recommend")==0){
					SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
					if(mprefs.getBoolean("facebook_login", false)){
						Intent intent = new Intent(this,FeedbackBozukoActivity.class);
						intent.putExtra("URL", page.requestInfo("linksrecommend"));
						intent.putExtra("HintText", "Think this place should rock Bozuko? Tell them here and hit submit.");
						startActivity(intent);
					}else{
						notLoggedIn();
					}
				}
			}else if(innerView.getClass() == PageHeaderView.class){
				//TODO map
				if(page.requestInfo("is_place").compareTo("false")!=0){
				Intent intent = new Intent(this,MapItMapActivity.class);
				intent.putExtra("Package", page);
				startActivity(intent);
				}
			}else if(innerView.getClass() == GameView.class){
				Object object = arg0.getItemAtPosition(arg2);
				if(object.getClass() == GameObject.class){
					SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
					if(mprefs.getBoolean("facebook_login", false)){
						((BozukoApplication)getApp()).currentGameObject = (GameObject)object;
						Intent intent = new Intent(this,GameEntryBozukoActivity.class);
						startActivity(intent);
					}else{
						notLoggedIn();
					}
				}
			}
		}
	}

	public void likeUs(){
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(mprefs.getBoolean("facebook_login", false)){
			Intent intent = new Intent(this,SocialMediaWebViewActivity.class);
			intent.setData(Uri.parse(page.requestInfo("like_url")));
			startActivity(intent);
		}else{
			notLoggedIn();
		}
	}

	public void checkIn(){
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(mprefs.getBoolean("facebook_login", false)){
			Intent intent = new Intent(this,FeedbackBozukoActivity.class);
			intent.putExtra("URL", page.requestInfo("linksfacebook_checkin"));
			intent.putExtra("HintText", "Type a message, then\npress \"Submit\" to Check In");
			intent.putExtra("DoInputCheck", false);
			startActivity(intent);
		}else{
			notLoggedIn();
		}
	}

	private class GameListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return page.games.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return page.games.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View masterConvertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			GroupView groupView = null;
			View convertView = null;
			if(masterConvertView != null){
				groupView = (GroupView)masterConvertView;
				convertView = groupView.getContentView();
			}else{
				groupView = new GroupView(getBaseContext());
			}

			GameView movieView = null;
			if (convertView == null) {
				movieView = new GameView(getBaseContext());
				groupView.setContentView(movieView);
			}
			else {
				movieView = (GameView) convertView;
			}
			movieView.display((GameObject)getItem(position));
			int resource = R.drawable.tableviewtopbutton;
			if(position > 0 && position < page.games.size()-1){
				resource = R.drawable.tableviewmidbutton;
			}else if(position > 0){
				resource = R.drawable.tableviewbtmbutton;
			}
			if(getCount()==1){
				resource = R.drawable.cellbutton;
			}

			groupView.setImage(resource);
			groupView.showArrow(true);
			return groupView;
		}

	}

	public void sendRequest(){
		if(!DataBaseHelper.isOnline(this)){
			errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
		try {
			String url;
			if(pageLink != null){
				url = GlobalConstants.BASE_URL + pageLink;
			}else{
				
				
				url = GlobalConstants.BASE_URL + page.requestInfo("linkspage");
			}
			Log.v("URL", url);
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			HttpRequest req = new HttpRequest(new URL(url + "?token=" + mprefs.getString("token", "") + "&mobile_version="+GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSONError();
			try{
			if(page == null){
				page = new PageObject(json);
			}else{
				page.processJson(json, "");
			}
			((BozukoApplication)getApp()).currentPageObject=page;
			RUNNABLE_STATE = RUNNABLE_SUCCESS;
		}catch(Throwable t){
			errorTitle = json.getString("title");
			errorMessage = json.getString("message");
			errorType = json.getString("name");
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
		} catch (Throwable e) {
			e.printStackTrace();
			errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		
		menu.add(0, R.drawable.icongames, 0, "Games").setIcon(R.drawable.icongames);
		menu.add(0, R.drawable.iconprizes, 0, "Prizes").setIcon(R.drawable.iconprizes);
		menu.add(0, R.drawable.iconbozuko, 0, "Bozuko").setIcon(R.drawable.iconbozuko);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.drawable.icongames:
				Intent games = new Intent(this,GamesTabController.class);
				games.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(games);
				finish();
				break;
			case R.drawable.iconprizes:
				Intent prizes = new Intent(this,PrizesBozukoActivity.class);
				prizes.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(prizes);
				finish();
				break;
			case R.drawable.iconbozuko:
				Intent bozuko = new Intent(this,SettingsBozukoActivity.class);
				bozuko.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(bozuko);
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected class UpdateReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			if(page.checkInfo("linkspage")){
			progressRunnable(new Runnable(){
				public void run(){
					sendRequest();
				}
			},"Loading...",CANCELABLE);
			}
		}
	}
}