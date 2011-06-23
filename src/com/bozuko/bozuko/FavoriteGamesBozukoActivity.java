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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class FavoriteGamesBozukoActivity extends BozukoControllerActivity {
	ArrayList<PageObject> games = new ArrayList<PageObject>();
	private int currentPage = 0;

	public void progressRunnableComplete(){
		ListView listview = (ListView)findViewById(R.id.ListView01);
		if(games.size()==0){
			setContent(R.layout.no_favorites);
			return;
		}
		
		if(listview == null){
			setContent(R.layout.listview);
			listview = (ListView)findViewById(R.id.ListView01);
		}
		
		listview.setSelector(R.drawable.listbutton);

		MergeAdapter mergeAdapter = new MergeAdapter();
		if(games.size()>0){
			mergeAdapter.addView(getTitleView("Favorites"), false);
			mergeAdapter.addAdapter(new PagesListAdapter(games));
		}
		listview.setAdapter(mergeAdapter);
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
				getGames();
			}else if(games.size() == 0){
				getGames();
			}else if(mprefs.getBoolean("ReloadFavorites", false)){
				SharedPreferences.Editor edit = mprefs.edit();
				edit.putBoolean("ReloadFavorites", false);
				edit.commit();
				getGames();
			}
		}else{
			//REMOVE THEM
			games.clear();
			setContent(R.layout.no_favorites);
		}
		super.onResume();
	}

	public void sendRequest(EntryPointObject entry){
		if(!DataBaseHelper.isOnline(this)){
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
		try {
			games.clear();
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linkspages");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			url += String.format("?ll=%s,%s&limit=25&favorites=true&offset=%d&token=%s",mprefs.getString("lat","0.00"),mprefs.getString("lon","0.00"),currentPage,mprefs.getString("token", ""));
			Log.v("URL",url);
			HttpRequest req = new HttpRequest(new URL(url));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSON();
			JSONArray objects = json.getJSONArray("pages");
			for(int i=0; i<objects.length(); i++){
				PageObject page = new PageObject(objects.getJSONObject(i));

				Log.v("Page",page.toString());
				if(page.requestInfo("registered").compareTo("true")==0){
					games.add(page);
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
}
