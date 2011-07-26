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
import android.widget.EditText;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

public class NearByGamesBozukoActivity extends BozukoControllerActivity implements OnItemClickListener, OnEditorActionListener {
	ArrayList<PageObject> featured = new ArrayList<PageObject>();
	ArrayList<PageObject> games = new ArrayList<PageObject>();
	ArrayList<PageObject> otherPlaces = new ArrayList<PageObject>();
	ArrayList<PageObject> search = new ArrayList<PageObject>();
	String lat = "0.0";
	String lon = "0.0";
	private int currentPage = 0;
	
	public void progressRunnableComplete(){
		if(((BozukoApplication)getApp()).searchTerm.trim().compareTo("")==0){
			setupList();
		}else{
			setupSearch();
		}	
	}
	
	public void progressRunnableError(){
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
		if(search.size()>0){
			mergeAdapter.addView(getTitleView("Search"), false);
			mergeAdapter.addAdapter(new PagesListAdapter(search));
			((TextView)findViewById(R.id.errmessage)).setVisibility(View.GONE);
		}else{
			((TextView)findViewById(R.id.errmessage)).setText("Sorry, your search yielded no results.");
			((TextView)findViewById(R.id.errmessage)).setVisibility(View.VISIBLE);
		}
		
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
			mergeAdapter.addAdapter(new PagesListAdapter(featured));
		}
		if(games.size()>0){
			mergeAdapter.addView(getTitleView("Nearby Games"), false);
			mergeAdapter.addAdapter(new PagesListAdapter(games));
		}
		if(otherPlaces.size()>0){
			mergeAdapter.addView(getTitleView("Other Places"), false);
			mergeAdapter.addAdapter(new PagesListAdapter(otherPlaces));
		}
		
		if(otherPlaces.size()==0 && games.size()==0 && featured.size()==0){
			((TextView)findViewById(R.id.errmessage)).setText("Sorry, your search yielded no results.");
			((TextView)findViewById(R.id.errmessage)).setVisibility(View.VISIBLE);
		}else{
			((TextView)findViewById(R.id.errmessage)).setVisibility(View.GONE);
		}
		
		listview.setAdapter(mergeAdapter);
		listview.setOnItemClickListener(this);
		
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
        
        SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
        lat = mprefs.getString("clat", "0.00");
        lon = mprefs.getString("clon", "0.00");
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
    	super.onResume();
    	SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
    	if(mprefs.getBoolean("ReloadNearby", false)){
			SharedPreferences.Editor edit = mprefs.edit();
			edit.putBoolean("ReloadNearby", false);
			edit.commit();
			getGames();
		}
    	if(featured.size()==0 && games.size()==0 && otherPlaces.size()==0){
    		 getGames();
    	}
    	if(((EditText)findViewById(R.id.search)).getText().toString().compareTo(((BozukoApplication)getApp()).searchTerm)!=0){
			doQuery(((BozukoApplication)getApp()).searchTerm);
		}
    	
    	String tempLat = mprefs.getString("clat", "0.00");
    	String tempLon = mprefs.getString("clon", "0.00");
    	if(lat.compareTo(tempLat)!=0 && lon.compareTo(tempLon)!=0){
    		Double distance = BozukoDataBaseHelper.distanceAsDouble(Double.valueOf(lat), Double.valueOf(tempLat), Double.valueOf(lon), Double.valueOf(tempLon));
    		if(distance > 5){
    			lat = tempLat;
    			lon = tempLon;
    			 getGames();
    		}
    	}
    		
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.search)).getWindowToken(), 0);
    }
    
    public void sendRequest(EntryPointObject entry){
    	if(!DataBaseHelper.isOnline(this)){
    		errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
    		RUNNABLE_STATE = RUNNABLE_FAILED;
		}
		try {
			games.clear();
			otherPlaces.clear();
			featured.clear();
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linkspages");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			lat = mprefs.getString("clat","0.00");
			lon = mprefs.getString("clon","0.00");
			url += String.format("?ll=%s,%s&limit=25&offset=%d",mprefs.getString("clat","0.00"),mprefs.getString("clon","0.00"),currentPage);
			//Log.v("URL",url);
			HttpRequest req = new HttpRequest(new URL(url + "&token=" + mprefs.getString("token", "") + "&mobile_version="+GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSONError();
			try{
			JSONArray objects = json.getJSONArray("pages");
			for(int i=0; i<objects.length(); i++){
				PageObject page = new PageObject(objects.getJSONObject(i));
				//Log.v("Page",page.toString());
				if(page.requestInfo("featured").compareTo("true")==0){
					featured.add(page);
				}else if(page.requestInfo("registered").compareTo("true")==0){
					games.add(page);
				}else{
					otherPlaces.add(page);
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
			errorMessage = "Failed to get places from server.";
    		errorTitle = "Request Error";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
    }
    
    public void sendSearchRequest(EntryPointObject entry){
    	if(!DataBaseHelper.isOnline(this)){
    		errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
    		RUNNABLE_STATE = RUNNABLE_FAILED;
		}
		try {
			search.clear();
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linkspages");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			url += String.format("?ll=%s,%s&limit=25&offset=%d",mprefs.getString("clat","0.00"),mprefs.getString("clon","0.00"),currentPage);
			//Log.v("URL",url);
			HttpRequest req = new HttpRequest(new URL(url + "&query=" + ((BozukoApplication)getApp()).searchTerm + "&token=" + mprefs.getString("token", "") + "&mobile_version="+GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSONError();
			try{
			JSONArray objects = json.getJSONArray("pages");
			for(int i=0; i<objects.length(); i++){
				PageObject page = new PageObject(objects.getJSONObject(i));
				search.add(page);
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
				movieView = new PageView(getBaseContext());
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		PageObject page = (PageObject)arg0.getItemAtPosition(arg2);
		((BozukoApplication)getApp()).currentPageObject = page;
		//Log.v("Page",page.toString());
		Intent intent = new Intent(this,PageBozukoActivity.class);
		//intent.putExtra("Package", page);
		startActivity(intent);
		
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
		if(((BozukoApplication)getApp()).searchTerm.compareTo("")!=0){
			((BozukoApplication)getApp()).searchTerm = "";
			setupList();
		}else{
			finish();
		}
	}
}
