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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class NearByGamesBozukoActivity extends BozukoControllerActivity implements OnItemClickListener {
	ArrayList<PageObject> featured = new ArrayList<PageObject>();
	ArrayList<PageObject> games = new ArrayList<PageObject>();
	ArrayList<PageObject> otherPlaces = new ArrayList<PageObject>();
	private int currentPage = 0;
	
	public void progressRunnableComplete(){
		ListView listview = (ListView)findViewById(R.id.ListView01);
		listview.setSelector(R.drawable.listbutton);
		
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
		
		listview.setAdapter(mergeAdapter);
		listview.setOnItemClickListener(this);
	}
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.listview);
        //setHeader(R.layout.detailheader);
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
    }
    
    public void sendRequest(EntryPointObject entry){
    	if(!DataBaseHelper.isOnline(this)){
    		RUNNABLE_STATE = RUNNABLE_FAILED;
		}
		try {
			games.clear();
			otherPlaces.clear();
			featured.clear();
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linkspages");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			url += String.format("?ll=%s,%s&limit=25&offset=%d",mprefs.getString("lat","0.00"),mprefs.getString("lon","0.00"),currentPage);
			Log.v("URL",url);
			HttpRequest req = new HttpRequest(new URL(url + "&token=" + mprefs.getString("token", "")));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSON();
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
		} catch (Throwable e) {
			e.printStackTrace();
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
		Log.v("Page",page.toString());
		Intent intent = new Intent(this,PageBozukoActivity.class);
		intent.putExtra("Package", page);
		startActivity(intent);
	}
}
