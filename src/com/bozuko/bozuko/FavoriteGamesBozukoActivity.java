package com.bozuko.bozuko;

import java.net.URL;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.EntryPointObject;
import com.bozuko.bozuko.datamodel.PageObject;
import com.bozuko.bozuko.views.PageView;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
import com.fuzz.android.ui.CheckView;
import com.fuzz.android.ui.MergeAdapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

public class FavoriteGamesBozukoActivity extends BozukoControllerActivity implements OnItemClickListener, OnEditorActionListener {
	ArrayList<PageObject> games = new ArrayList<PageObject>();
	ArrayList<PageObject> search = new ArrayList<PageObject>();
	private int currentPage = 0;

	public void progressRunnableComplete(){
		if(((BozukoApplication)getApp()).searchTerm.trim().compareTo("")==0){
			setupList();
		}else{
			setupSearch();
		}
	}
	
	public void setupSearch(){
		ListView listview = (ListView)findViewById(R.id.ListView01);
		listview.setSelector(R.drawable.listbutton);
		listview.setItemsCanFocus(false);
		MergeAdapter mergeAdapter = new MergeAdapter();
		if(search.size()>0){
			mergeAdapter.addView(getTitleView("Search"), false);
			mergeAdapter.addAdapter(new PagesListAdapter(search));
		}
		listview.setAdapter(mergeAdapter);
		listview.setOnItemClickListener(this);
	}
	
	public void setupList(){
		ListView listview = (ListView)findViewById(R.id.ListView01);
		
		if(games.size()==0){
			setContent(R.layout.no_favorites);
			return;
		}
		
		if(listview == null){
			setContent(R.layout.searchlistview);
			listview = (ListView)findViewById(R.id.ListView01);
		}
		listview.setDivider(getResources().getDrawable(R.drawable.pxdividinglinewhite));
		listview.setItemsCanFocus(false);
		 ((EditText)findViewById(R.id.search)).setOnEditorActionListener(this);
		 ((EditText)findViewById(R.id.search)).setSingleLine(true);
		// ((EditText)findViewById(R.id.search)).setFocusable(false);
		 ((EditText)findViewById(R.id.search)).setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		 InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	    	imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.search)).getWindowToken(), 0);
		listview.setSelector(R.drawable.listbutton);
		//listview.setItemsCanFocus(true);
		MergeAdapter mergeAdapter = new MergeAdapter();
		if(games.size()>0){
			mergeAdapter.addView(getTitleView("Favorites"), false);
			mergeAdapter.addAdapter(new PagesListAdapter(games));
		}
		listview.setAdapter(mergeAdapter);
		listview.setOnItemClickListener(this);
		
		if(((BozukoApplication)getApp()).searchTerm.trim().compareTo("")!=0){
			((EditText)findViewById(R.id.search)).setText(((BozukoApplication)getApp()).searchTerm);
			doQuery(((BozukoApplication)getApp()).searchTerm);
		}
	}
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		// TODO Auto-generated method stub
		if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
       }
		String string = ((EditText)v).getText().toString();
		if(actionId == EditorInfo.IME_ACTION_SEARCH){
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			doQuery(string);
			return true;
		}else if(event.getKeyCode() == KeyEvent.KEYCODE_SEARCH){
			doQuery(string);
		}else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER){
			doQuery(string);
		}else if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
			doQuery(string);
		}
		return false;
	}
	
	public void doQuery(String query){
		((BozukoApplication)getApp()).searchTerm = query;
		
		if(query.trim().compareTo("")==0){
			setupList();
		}else{
			progressRunnable(new Runnable(){
				public void run(){
					EntryPointObject entry = new EntryPointObject("1");
	                entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(getBaseContext()));
	        		sendSearchRequest(entry);
				}
			},"Searching...",NOT_CANCELABLE);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent(R.layout.no_favorites);


	}

	public void getGames(){
		progressRunnable(new Runnable(){
			public void run(){
				EntryPointObject entry = new EntryPointObject("1");
				entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(getBaseContext()));
				sendRequest(entry);
			}
		},"Loading...",CANCELABLE);
	}

	public void onResume(){
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(mprefs.getBoolean("facebook_login", false)){
			if(games == null){
				SharedPreferences.Editor edit = mprefs.edit();
				edit.putBoolean("ReloadFavorites", false);
				edit.commit();
				getGames();
			}else if(games.size() == 0){
				SharedPreferences.Editor edit = mprefs.edit();
				edit.putBoolean("ReloadFavorites", false);
				edit.commit();
				getGames();
			}else if(mprefs.getBoolean("ReloadFavorites", false)){
				SharedPreferences.Editor edit = mprefs.edit();
				edit.putBoolean("ReloadFavorites", false);
				edit.commit();
				getGames();
			}else{
				if(((EditText)findViewById(R.id.search)).getText().toString().compareTo(((BozukoApplication)getApp()).searchTerm)!=0){
					doQuery(((BozukoApplication)getApp()).searchTerm);
				}
			}
		}else{
			//REMOVE THEM
			games.clear();
			setContent(R.layout.no_favorites);
		}
		super.onResume();
	}

	public void sendRequest(EntryPointObject entry){
		if(!DataBaseHelper.isOnline(this,0)){
			errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
			RUNNABLE_STATE = RUNNABLE_FAILED;
			return;
		}
		try {
			games.clear();
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linkspages");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			url += String.format("?ll=%s,%s&limit=25&favorites=true&offset=%d&token=%s",mprefs.getString("clat","0.00"),mprefs.getString("clon","0.00"),currentPage,mprefs.getString("token", ""));
			//Log.v("URL",url);
			HttpRequest req = new HttpRequest(new URL(url+"&mobile_version="+GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSONError();
			try{
			JSONArray objects = json.getJSONArray("pages");
			for(int i=0; i<objects.length(); i++){
				PageObject page = new PageObject(objects.getJSONObject(i));

				//Log.v("Page",page.toString());
				if(page.requestInfo("registered").compareTo("true")==0){
					games.add(page);
				}
			}
			RUNNABLE_STATE = RUNNABLE_SUCCESS;
			}catch(Throwable t){
				errorTitle = json.getString("title");
				errorMessage = json.getString("message");
				errorType = json.getString("name");
				RUNNABLE_STATE = RUNNABLE_FAILED;
			}
		} catch (Throwable e) {
			mHandler.post(new DisplayThrowable(e));
			e.printStackTrace();
			errorMessage = "Failed to get places from server.";
    		errorTitle = "Request Error";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}
	
	public void sendSearchRequest(EntryPointObject entry){
		if(!DataBaseHelper.isOnline(this,0)){
			errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
			RUNNABLE_STATE = RUNNABLE_FAILED;
			return;
		}
		try {
			search.clear();
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linkspages");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			url += String.format("?ll=%s,%s&limit=25&favorites=true&offset=%d&token=%s",mprefs.getString("clat","0.00"),mprefs.getString("clon","0.00"),currentPage,mprefs.getString("token", ""));
			//Log.v("URL",url);
			HttpRequest req = new HttpRequest(new URL(url+ "&query=" + ((BozukoApplication)getApp()).searchTerm+"&mobile_version="+GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSONError();
			try{
			JSONArray objects = json.getJSONArray("pages");
			for(int i=0; i<objects.length(); i++){
				PageObject page = new PageObject(objects.getJSONObject(i));

				//Log.v("Page",page.toString());
				if(page.requestInfo("registered").compareTo("true")==0){
					search.add(page);
				}
			}
			RUNNABLE_STATE = RUNNABLE_SUCCESS;
		}catch(Throwable t){
			errorTitle = json.getString("title");
			errorMessage = json.getString("message");
			errorType = json.getString("name");
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
		} catch (Throwable e) {
			e.printStackTrace();
			mHandler.post(new DisplayThrowable(e));
			errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}

	private class PagesListAdapter extends BaseAdapter{
		ArrayList<PageObject> pages;

		public PagesListAdapter(ArrayList<PageObject> inArray){
			pages = inArray;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return pages.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return pages.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View masterConvertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			CheckView groupView = null;
			View convertView = null;
			if(masterConvertView != null){
				groupView = (CheckView)masterConvertView;
				convertView = groupView.getContentView();
			}else{
				groupView = new CheckView(getBaseContext());
			}

			PageView movieView = null;
			if (convertView == null) {
				movieView = new PageView(FavoriteGamesBozukoActivity.this);
				groupView.setContentView(movieView);
			}
			else {
				movieView = (PageView) convertView;
			}
			movieView.display((PageObject)getItem(position));
			groupView.showArrow(true);
			return groupView;
		}

	}

	public void update(PageObject page) {
		// TODO Auto-generated method stub
		ListView listview = (ListView)findViewById(R.id.ListView01);
		games.remove(page);
		((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		PageObject page = (PageObject)arg0.getItemAtPosition(arg2);
		((BozukoApplication)getApp()).currentPageObject = page;
		Intent intent = new Intent(this,PageBozukoActivity.class);
		//intent.putExtra("Package", page);
		startActivity(intent);
	}
	
	public void refresh(){
		getGames();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			// Take care of calling this method on earlier versions of
			// the platform where it doesn't exist.
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if(games.size()>0){
			if(((BozukoApplication)getApp()).searchTerm.compareTo("")!=0){
				((BozukoApplication)getApp()).searchTerm = "";
				setupList();
			}else{
				finish();
			}
		}else{
			finish();
		}
	}
}
