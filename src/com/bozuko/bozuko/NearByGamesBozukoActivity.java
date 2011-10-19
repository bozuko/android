package com.bozuko.bozuko;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.EntryPointObject;
import com.bozuko.bozuko.datamodel.PageObject;
import com.bozuko.bozuko.views.PageView;
import com.bozuko.bozuko.views.PrizeView;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
import com.fuzz.android.ui.CheckView;
import com.fuzz.android.ui.MergeAdapter;

public class NearByGamesBozukoActivity extends BozukoControllerActivity implements OnItemClickListener, OnEditorActionListener {
	ArrayList<PageObject> featured = new ArrayList<PageObject>();
	ArrayList<PageObject> games = new ArrayList<PageObject>();
	ArrayList<PageObject> otherPlaces = new ArrayList<PageObject>();
	
	
	ArrayList<PageObject> searchFeatured = new ArrayList<PageObject>();
	ArrayList<PageObject> searchGames = new ArrayList<PageObject>();
	ArrayList<PageObject> searchPlaces = new ArrayList<PageObject>();
	
	PrizeView loadMore;
	
	
	String lat = "0.0";
	String lon = "0.0";
	private int currentPage = 0;
	private int searchCurrentPage = 0;

	boolean loaded = false;
	boolean loadedSearch = false;

	String nextSearchURL = "";
	String nextURL = "";
	
	public void onDestroy(){
//		Intent backToIntent = new Intent("destroyURLImage");
//		backToIntent.putExtra("parentClass",this.getClass().toString());
//		sendBroadcast(backToIntent);
		
		ListView listview = (ListView)findViewById(R.id.ListView01);
		listview.setAdapter(new SimpleAdapter());
		featured.clear();
		games.clear();
		otherPlaces.clear();
		searchFeatured.clear();
		searchGames.clear();
		searchPlaces.clear();
		super.onDestroy();
	}
	
	public void progressRunnableComplete(){
		SENDING=false;
		if(isFinishing()){
			return;
		}
		
		
		loadMore.display(null);
		if(((BozukoApplication)getApp()).searchTerm.trim().compareTo("")==0){
			if(!loaded){
				loaded = true;
			setupList();
			}
		}else{
			if(!loadedSearch){
				loadedSearch = true;
			setupSearch();
			}
		}	
	}

	public void progressRunnableError(){
		SENDING=false;
		if(isFinishing()){
			return;
		}
		super.progressRunnableError();
		ListView listview = (ListView)findViewById(R.id.ListView01);
		listview.setSelector(R.drawable.listbutton);
		listview.setItemsCanFocus(false);
		MergeAdapter mergeAdapter = new MergeAdapter();
		listview.setAdapter(mergeAdapter);
		listview.setOnItemClickListener(this);
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.search)).getWindowToken(), 0);

	}

	public void setupSearch(){
		ListView listview = (ListView)findViewById(R.id.ListView01);
		listview.setSelector(R.drawable.listbutton);
		listview.setItemsCanFocus(false);
		MergeAdapter mergeAdapter = new MergeAdapter();
		if(searchFeatured.size()>0){
			mergeAdapter.addView(getTitleView("Featured"), false);
			mergeAdapter.addAdapter(new PagesListAdapter(searchFeatured,false));
			//makeDialog(featured.size()+"","FEATURE COUNTING",null);
		}
		if(searchGames.size()>0){
			mergeAdapter.addView(getTitleView("Nearby Games"), false);
			mergeAdapter.addAdapter(new PagesListAdapter(searchGames,false));
		}
		if(searchPlaces.size()>0){
			mergeAdapter.addView(getTitleView("Other Places"), false);
			mergeAdapter.addAdapter(new PagesListAdapter(searchPlaces,false));
		}

		if(searchPlaces.size()==0 && searchGames.size()==0 && searchFeatured.size()==0){
			((TextView)findViewById(R.id.errmessage)).setText("Sorry, your search yielded no results.");
			((TextView)findViewById(R.id.errmessage)).setVisibility(View.VISIBLE);
		}else{
			((TextView)findViewById(R.id.errmessage)).setVisibility(View.GONE);
		}
		mergeAdapter.addView(loadMore, true);
		listview.setAdapter(mergeAdapter);
		listview.setOnItemClickListener(this);

		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.search)).getWindowToken(), 0);
	}

	public void setupList(){
		ListView listview = (ListView)findViewById(R.id.ListView01);
		listview.setSelector(R.drawable.listbutton);
		listview.setItemsCanFocus(false);
		listview.setDivider(getResources().getDrawable(R.drawable.pxdividinglinewhite));
		MergeAdapter mergeAdapter = new MergeAdapter();
		if(featured.size()>0){
			mergeAdapter.addView(getTitleView("Featured"), false);
			mergeAdapter.addAdapter(new PagesListAdapter(featured,false));
			//makeDialog(featured.size()+"","FEATURE COUNTING",null);
		}
		if(games.size()>0){
			mergeAdapter.addView(getTitleView("Nearby Games"), false);
			mergeAdapter.addAdapter(new PagesListAdapter(games,false));
		}
		if(otherPlaces.size()>0){
			mergeAdapter.addView(getTitleView("Other Places"), false);
			mergeAdapter.addAdapter(new PagesListAdapter(otherPlaces,false));
		}

		if(otherPlaces.size()==0 && games.size()==0 && featured.size()==0){
			((TextView)findViewById(R.id.errmessage)).setText("Sorry, your search yielded no results.");
			((TextView)findViewById(R.id.errmessage)).setVisibility(View.VISIBLE);
		}else{
			((TextView)findViewById(R.id.errmessage)).setVisibility(View.GONE);
		}
		
		
		mergeAdapter.addView(loadMore, true);
		listview.setAdapter(mergeAdapter);
		listview.setOnItemClickListener(this);

		
		if(nextURL != null){
			loadMore.setVisibility(View.VISIBLE);
		}else{
			loadMore.setVisibility(View.GONE);
		}
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.search)).getWindowToken(), 0);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent(R.layout.searchlistview);
		
		
		//setHeader(R.layout.detailheader);
		((EditText)findViewById(R.id.search)).setSingleLine(true);
		((EditText)findViewById(R.id.search)).setOnEditorActionListener(this);

		((EditText)findViewById(R.id.search)).setImeOptions(EditorInfo.IME_ACTION_SEARCH);

		//SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		//lat = mprefs.getString("clat", "0.00");
		//lon = mprefs.getString("clon", "0.00");
		lat = "0.00";
		lon = "0.00";
		
		if(loadMore==null){
			loadMore = new PrizeView(this);
		}
	}

	boolean SENDING = false;
	public void getGames(){
		if(!SENDING){
			SENDING = true;
			
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			String tempLat = mprefs.getString("clat", "0.00");
			String tempLon = mprefs.getString("clon", "0.00");
			lat = tempLat;
			lon = tempLon;
		ListView listview = (ListView)findViewById(R.id.ListView01);
		MergeAdapter mergeAdapter = new MergeAdapter();
		listview.setAdapter(mergeAdapter);
		featured.clear();
		games.clear();
		nextURL = "";
		loaded = false;
		otherPlaces.clear();
		currentPage = 0;
		progressRunnable(new Runnable(){
			public void run(){
				EntryPointObject entry = new EntryPointObject("1");
				entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(NearByGamesBozukoActivity.this));
				sendRequest(entry);
			}
		},"Loading...",NOT_CANCELABLE);
		
		}
	}

	boolean firstLoad = true;
	LocationReceiver mLocationReceiver = new LocationReceiver();
	public void onResume(){
		super.onResume();
		registerReceiver(mLocationReceiver, new IntentFilter("LOCATIONSUPDATED"));
		
		if(firstLoad){
			progressMessage = "Getting Location...";
			showDialog(NOT_CANCELABLE);
			getApp().startLocation();
		}else{
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			String tempLat = mprefs.getString("clat", "0.00");
			String tempLon = mprefs.getString("clon", "0.00");
			if(mprefs.getBoolean("ReloadNearby", false)){
				SharedPreferences.Editor edit = mprefs.edit();
				edit.putBoolean("ReloadNearby", false);
				edit.commit();
				getGames();
			}else if(featured.size()==0 && games.size()==0 && otherPlaces.size()==0){
				getGames();
			}else if(lat.compareTo(tempLat)!=0 && lon.compareTo(tempLon)!=0){
				Double distance = BozukoDataBaseHelper.distanceAsDouble(Double.valueOf(lat), Double.valueOf(tempLat), Double.valueOf(lon), Double.valueOf(tempLon));
				if(distance > 0.5){
					getGames();
				}
			}

			
		}
		if(((EditText)findViewById(R.id.search)).getText().toString().compareTo(((BozukoApplication)getApp()).searchTerm)!=0){
			doQuery(((BozukoApplication)getApp()).searchTerm);
		}
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.search)).getWindowToken(), 0);
	
	}

	public void onPause(){
		super.onPause();
		unregisterReceiver(mLocationReceiver);
	}
	
	public void sendRequest(EntryPointObject entry){
		if(!DataBaseHelper.isOnline(this,0)){
			errorMessage = "Unable to connect to the internet";
			errorTitle = "No Connection";
			RUNNABLE_STATE = RUNNABLE_FAILED;
			return;
		}
		try {
			ArrayList<PageObject> feature = new ArrayList<PageObject>();
			ArrayList<PageObject> game = new ArrayList<PageObject>();
			ArrayList<PageObject> other = new ArrayList<PageObject>();
			String url = GlobalConstants.BASE_URL;
			if(nextURL.compareTo("")==0){
				url += entry.requestInfo("linkspages");
				SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
				lat = mprefs.getString("clat","0.00");
				lon = mprefs.getString("clon","0.00");
				url += String.format("?ll=%s,%s&limit=25&offset=%d",mprefs.getString("clat","0.00"),mprefs.getString("clon","0.00"),currentPage);
				
				url += "&accuracy=" + mprefs.getString("acc", "0");
				url += "&token=" + mprefs.getString("token", "") + "&mobile_version="+GlobalConstants.MOBILE_VERSION;
			}else{
				url += nextURL;
				nextURL = "";
			}
			
			HttpRequest req = new HttpRequest(new URL(url));
			req.setMethodType("GET");
			JsonParser jp = req.AutoStreamJSONError();
			JsonToken start = jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
			if(start == JsonToken.START_OBJECT){
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String fieldname = jp.getCurrentName();
				JsonToken token = jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
				if(token == JsonToken.NOT_AVAILABLE){
					throw new Exception("Parser failed");
				}
				if ("pages".equals(fieldname)) { 
					//DO parse json
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						PageObject page = new PageObject(jp);
						if(page.requestInfo("featured").compareTo("true")==0){
							feature.add(page);
						}else if(page.requestInfo("registered").compareTo("true")==0){
							game.add(page);
						}else{
							other.add(page);
						}
					}
				
					RUNNABLE_STATE = RUNNABLE_SUCCESS;
				}else if ("title".equals(fieldname)) { 
					RUNNABLE_STATE = RUNNABLE_FAILED;
					errorTitle = jp.getText();
				}else if ("message".equals(fieldname)) {
					RUNNABLE_STATE = RUNNABLE_FAILED;
					errorMessage = jp.getText();
				}else if ("name".equals(fieldname)) { 
					RUNNABLE_STATE = RUNNABLE_FAILED;
					errorType = jp.getText();
				}else if ("next".equals(fieldname)) { 
					nextURL = jp.getText();
				}
			}
			}else{
				errorMessage = "Failed to get places from server.";
				errorTitle = "Request Error";
				RUNNABLE_STATE = RUNNABLE_FAILED;
			}
			jp.close();
			mHandler.post(new AddAllRunnable(feature,featured));
			mHandler.post(new AddAllRunnable(game,games));
			mHandler.post(new AddAllRunnable(other,otherPlaces));
			
			//TODO
			
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
			ArrayList<PageObject> feature = new ArrayList<PageObject>();
			ArrayList<PageObject> game = new ArrayList<PageObject>();
			ArrayList<PageObject> other = new ArrayList<PageObject>();
			String url = GlobalConstants.BASE_URL;
			if(nextSearchURL.compareTo("")==0){
				url += entry.requestInfo("linkspages");
				SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
				url += String.format("?ll=%s,%s&limit=25&offset=%d",mprefs.getString("clat","0.00"),mprefs.getString("clon","0.00"),searchCurrentPage);
				url += "&query=" +  URLEncoder.encode(((BozukoApplication)getApp()).searchTerm) +"&mobile_version="+GlobalConstants.MOBILE_VERSION;
				url += "&token=" + mprefs.getString("token", "");
				url += "&accuracy=" + mprefs.getString("acc", "0");
			}else{
				url += nextSearchURL;
				nextSearchURL = "";
			}
			HttpRequest req = new HttpRequest(new URL(url));
			req.setMethodType("GET");
			JsonParser jp = req.AutoStreamJSONError();
			JsonToken start = jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
			if(start == JsonToken.START_OBJECT){
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String fieldname = jp.getCurrentName();
				JsonToken token = jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
				if(token == JsonToken.NOT_AVAILABLE){
					throw new Exception("Parser failed");
				}
				if ("pages".equals(fieldname)) { 
					//DO parse json
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						PageObject page = new PageObject(jp);
						if(page.requestInfo("featured").compareTo("true")==0){
							feature.add(page);
						}else if(page.requestInfo("registered").compareTo("true")==0){
							game.add(page);
						}else{
							other.add(page);
						}
					}
					RUNNABLE_STATE = RUNNABLE_SUCCESS;
				}else if ("title".equals(fieldname)) { 
					RUNNABLE_STATE = RUNNABLE_FAILED;
					errorTitle = jp.getText();
				}else if ("message".equals(fieldname)) {
					RUNNABLE_STATE = RUNNABLE_FAILED;
					errorMessage = jp.getText();
				}else if ("name".equals(fieldname)) { 
					RUNNABLE_STATE = RUNNABLE_FAILED;
					errorType = jp.getText();
				}else if ("next".equals(fieldname)) { 
					nextSearchURL = jp.getText();
				}
			}
			}else{
				errorMessage = "Failed to get places from server.";
				errorTitle = "Request Error";
				RUNNABLE_STATE = RUNNABLE_FAILED;
			}
			jp.close();
			mHandler.post(new AddAllRunnable(feature,searchFeatured));
			mHandler.post(new AddAllRunnable(game,searchGames));
			mHandler.post(new AddAllRunnable(other,searchPlaces));
		} catch (Throwable e) {
			mHandler.post(new DisplayThrowable(e));
			//e.printStackTrace();
			errorMessage = "Unable to connect to the internet";
			errorTitle = "No Connection";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}

	private class PagesListAdapter extends BaseAdapter{
		ArrayList<PageObject> pages;
		//boolean type;

		public PagesListAdapter(ArrayList<PageObject> inArray,boolean inType){
			pages = inArray;
			//type = inType;
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
				groupView = new CheckView(NearByGamesBozukoActivity.this);
			}

			PageView movieView = null;
			if (convertView == null) {
				movieView = new PageView(NearByGamesBozukoActivity.this);
				groupView.setContentView(movieView);
			}
			else {
				movieView = (PageView) convertView;
			}
			movieView.display((PageObject)getItem(position));
			if(getItem(position)==null){
				groupView.showArrow(false);
			}else{
				groupView.showArrow(true);
			}
			
			return groupView;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Object obj = arg0.getItemAtPosition(arg2);
		
		if(obj.getClass() == PageObject.class){
			PageObject page = (PageObject)obj;
			((BozukoApplication)getApp()).currentPageObject = page;
			Intent intent = new Intent(this,PageBozukoActivity.class);
			//intent.putExtra("Package", page);
			startActivity(intent);
		}else{
			loadMore();
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
			searchFeatured.clear();
			searchPlaces.clear();
			searchGames.clear();
			nextSearchURL = "";
			searchCurrentPage=0;
		}else{
			ListView listview = (ListView)findViewById(R.id.ListView01);
			MergeAdapter mergeAdapter = new MergeAdapter();
			listview.setAdapter(mergeAdapter);
			searchFeatured.clear();
			searchPlaces.clear();
			searchGames.clear();
			nextSearchURL = "";
			loadedSearch = false;
			searchCurrentPage=0;
			progressRunnable(new Runnable(){
				public void run(){
					EntryPointObject entry = new EntryPointObject("1");
					entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(NearByGamesBozukoActivity.this));
					sendSearchRequest(entry);
				}
			},"Searching...",NOT_CANCELABLE);
		}
	}

	public void loadMore(){
		if(((BozukoApplication)getApp()).searchTerm.trim().compareTo("")==0){
			currentPage++;
			unProgressRunnable(new Runnable(){
				public void run(){
					EntryPointObject entry = new EntryPointObject("1");
					entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(NearByGamesBozukoActivity.this));
					sendRequest(entry);
				}
			});
		}else{
			searchCurrentPage++;
			unProgressRunnable(new Runnable(){
				public void run(){
					EntryPointObject entry = new EntryPointObject("1");
					entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(NearByGamesBozukoActivity.this));
					sendSearchRequest(entry);
				}
			});
		}
	}

	public void refresh(){
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);

		String tempLat = mprefs.getString("clat", "0.00");
		String tempLon = mprefs.getString("clon", "0.00");
		lat = tempLat;
		lon = tempLon;
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
		if(((BozukoApplication)getApp()).searchTerm.compareTo("")!=0){
			((BozukoApplication)getApp()).searchTerm = "";
			setupList();
		}else{
			finish();
		}
	}

	private class AddAllRunnable implements Runnable{
		ArrayList<PageObject> srcArray;
		ArrayList<PageObject> dstArray;
		public AddAllRunnable(ArrayList<PageObject> tmpArray, ArrayList<PageObject> inArray){
			dstArray = inArray;
			srcArray = tmpArray;
		}

		public void run(){
			dstArray.addAll(srcArray);

			try{
				ListView listview = (ListView)findViewById(R.id.ListView01);
				((BaseAdapter)listview.getAdapter()).notifyDataSetChanged();
			}catch(Throwable t){

			}
			
			if(dstArray==featured){
			if(nextURL.compareTo("")==0){
				loadMore.setVisibility(View.GONE);
			}else{
				loadMore.setVisibility(View.VISIBLE);
			}
			}
			if(dstArray==searchFeatured){
				if(nextSearchURL.compareTo("")==0){
					loadMore.setVisibility(View.GONE);
				}else{
					loadMore.setVisibility(View.VISIBLE);
				}
				}
		}
	}


	protected class LocationReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			if(firstLoad){
				firstLoad = false;
				//dismissDialog(NOT_CANCELABLE);
				getGames();
			}else{
				SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(arg0);
				String tempLat = mprefs.getString("clat", "0.00");
				String tempLon = mprefs.getString("clon", "0.00");
				Double distance = BozukoDataBaseHelper.distanceAsDouble(Double.valueOf(lat), Double.valueOf(tempLat), Double.valueOf(lon), Double.valueOf(tempLon));
				if(distance > 0.5){
					getGames();
				}
			}
			
		}
	}
}
