package com.bozuko.bozuko;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
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
	private int searchCurrentPage = 0;

	boolean loadMore = false;
	boolean loadMoreSearch = false;

	public void progressRunnableComplete(){
		if(isFinishing()){
			return;
		}
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
		if(search.size()<30){
			MergeAdapter mergeAdapter = new MergeAdapter();
			if(search.size()>0){
				mergeAdapter.addView(getTitleView("Search"), false);
				mergeAdapter.addAdapter(new PagesListAdapter(search,true));
				((TextView)findViewById(R.id.errmessage)).setVisibility(View.GONE);
			}else{
				((TextView)findViewById(R.id.errmessage)).setText("Sorry, your search yielded no results.");
				((TextView)findViewById(R.id.errmessage)).setVisibility(View.VISIBLE);
			}
			
			listview.setAdapter(mergeAdapter);
		}else{
			
		}
		
		
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
			mergeAdapter.addAdapter(new PagesListAdapter(games,false));
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
			try{
				ListView listview = (ListView)findViewById(R.id.ListView01);
				MergeAdapter mergeAdapter = new MergeAdapter();
				listview.setAdapter(mergeAdapter);
			}catch(Throwable t){

			}
			search.clear();
			searchCurrentPage = 0;
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
		try{
			ListView listview = (ListView)findViewById(R.id.ListView01);
			MergeAdapter mergeAdapter = new MergeAdapter();
			listview.setAdapter(mergeAdapter);
		}catch(Throwable t){

		}
		games.clear();
		currentPage = 0;
		progressRunnable(new Runnable(){
			public void run(){
				EntryPointObject entry = new EntryPointObject("1");
				entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(getBaseContext()));
				sendRequest(entry);
			}
		},"Loading...",CANCELABLE);
	}

	public void loadMore(){
		if(((BozukoApplication)getApp()).searchTerm.trim().compareTo("")==0){
			currentPage++;
			progressRunnable(new Runnable(){
				public void run(){
					EntryPointObject entry = new EntryPointObject("1");
					entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(getBaseContext()));
					sendRequest(entry);
				}
			},"Loading...",CANCELABLE);
		}else{
			searchCurrentPage++;
			progressRunnable(new Runnable(){
				public void run(){
					EntryPointObject entry = new EntryPointObject("1");
					entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(getBaseContext()));
					sendSearchRequest(entry);
				}
			},"Loading...",CANCELABLE);
		}
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
			ArrayList<PageObject> pages = new ArrayList<PageObject>();
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linkspages");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			url += String.format("?ll=%s,%s&limit=25&favorites=true&offset=%d&token=%s",mprefs.getString("clat","0.00"),mprefs.getString("clon","0.00"),currentPage,mprefs.getString("token", ""));
			//Log.v("URL",url);
			HttpRequest req = new HttpRequest(new URL(url+"&mobile_version="+GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			JsonParser jp = req.AutoStreamJSONError();
			jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String fieldname = jp.getCurrentName();
				jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
				if ("pages".equals(fieldname)) { 
					//DO parse json
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						PageObject page = new PageObject(jp);
						//Log.v("Page",page.toString());
						if(page.requestInfo("registered").compareTo("true")==0){
							pages.add(page);
						}
					}
					

					mHandler.post(new AddAllRunnable(pages,games));

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
				}
			}
			jp.close();
		} catch (Throwable e) {
			mHandler.post(new DisplayThrowable(e));
			//e.printStackTrace();
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

			ArrayList<PageObject> pages = new ArrayList<PageObject>();
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linkspages");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			url += String.format("?ll=%s,%s&limit=25&favorites=true&offset=%d&token=%s",mprefs.getString("clat","0.00"),mprefs.getString("clon","0.00"),searchCurrentPage,mprefs.getString("token", ""));
			//Log.v("URL",url);
			HttpRequest req = new HttpRequest(new URL(url+ "&query=" +  URLEncoder.encode(((BozukoApplication)getApp()).searchTerm) +"&mobile_version="+GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSONError();
			try{
				JSONArray objects = json.getJSONArray("pages");
				for(int i=0; i<objects.length(); i++){
					PageObject page = new PageObject(objects.getJSONObject(i));

					//Log.v("Page",page.toString());
					if(page.requestInfo("registered").compareTo("true")==0){
						pages.add(page);
					}
				}

				

				mHandler.post(new AddAllRunnable(pages,search));

				RUNNABLE_STATE = RUNNABLE_SUCCESS;
			}catch(Throwable t){
				errorTitle = json.getString("title");
				errorMessage = json.getString("message");
				errorType = json.getString("name");
				RUNNABLE_STATE = RUNNABLE_FAILED;
			}
		} catch (Throwable e) {
			//e.printStackTrace();
			mHandler.post(new DisplayThrowable(e));
			errorMessage = "Unable to connect to the internet";
			errorTitle = "No Connection";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}

	private class PagesListAdapter extends BaseAdapter{
		ArrayList<PageObject> pages;
		boolean type = false;

		public PagesListAdapter(ArrayList<PageObject> inArray,boolean inType){
			pages = inArray;
			type = inType;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if(type){
				if(loadMoreSearch){
					return pages.size()+1;
				}else{
					return pages.size();
				}
			}else{
				if(loadMore){
					return pages.size()+1;
				}else{
					return pages.size();
				}
			}


		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			if(position < pages.size()){
				return pages.get(position);
			}else{
				return null;
			}
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
			if(getItem(position)==null){
				groupView.showArrow(false);
			}else{
				groupView.showArrow(true);
			}
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
		Object obj = arg0.getItemAtPosition(arg2);
		if(obj != null){
			PageObject page = (PageObject)obj;
			((BozukoApplication)getApp()).currentPageObject = page;
			Intent intent = new Intent(this,PageBozukoActivity.class);
			//intent.putExtra("Package", page);
			startActivity(intent);
		}else{
			loadMore();
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
		}
	}
}
