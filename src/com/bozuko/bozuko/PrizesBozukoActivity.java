package com.bozuko.bozuko;

import java.net.URL;
import java.util.ArrayList;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.EntryPointObject;
import com.bozuko.bozuko.datamodel.PrizeObject;
import com.bozuko.bozuko.views.PrizeView;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
import com.fuzz.android.ui.CheckView;
import com.fuzz.android.ui.MergeAdapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PrizesBozukoActivity extends BozukoControllerActivity implements OnItemClickListener, OnClickListener {
	ArrayList<PrizeObject> activePrizes = new ArrayList<PrizeObject>();
	ArrayList<PrizeObject> pastPrizes = new ArrayList<PrizeObject>();
	
	ArrayList<PrizeObject> tmpActivePrizes = new ArrayList<PrizeObject>();
	ArrayList<PrizeObject> tmpPastPrizes = new ArrayList<PrizeObject>();
	
	public void progressRunnableComplete(){
		if(isFinishing()){
			return;
		}
		ListView listview = (ListView)findViewById(R.id.ListView01);
		
		pastPrizes.clear();
		activePrizes.clear();
		pastPrizes.addAll(tmpPastPrizes);
		activePrizes.addAll(tmpActivePrizes);
		
		if(activePrizes.size()==0 && pastPrizes.size()==0){
			setContent(R.layout.no_prizes);
			return;
		}
		
		if(listview == null){
			setContent(R.layout.listview);
			listview = (ListView)findViewById(R.id.ListView01);
		}
		listview.setDivider(getResources().getDrawable(R.drawable.pxdividinglinewhite));
		listview.setSelector(R.drawable.listbutton);

		
		
		MergeAdapter mergeAdapter = new MergeAdapter();
		if(activePrizes.size()>0){
			mergeAdapter.addView(getTitleView("Active Prizes"), false);
			mergeAdapter.addAdapter(new PrizeListAdapter(activePrizes));
		}
		if(pastPrizes.size()>0){
			mergeAdapter.addView(getTitleView("Past Prizes"), false);
			mergeAdapter.addAdapter(new PrizeListAdapter(pastPrizes));
		}
		listview.setAdapter(mergeAdapter);
		listview.setOnItemClickListener(this);
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent(R.layout.no_prizes);
		setHeader(R.layout.detailheader);
		
		
	}
	
	public void getPrizes(){
		progressRunnable(new Runnable(){
			public void run(){
				EntryPointObject entry = new EntryPointObject("1");
				entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(getBaseContext()));
				sendRequest(entry);
			}
		},"Loading...",NOT_CANCELABLE);
	}
	
	public void onResume(){
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(mprefs.getBoolean("facebook_login", false)){
			getPrizes();
		}else{
			activePrizes.clear();
			pastPrizes.clear();
			setContent(R.layout.no_prizes);
		}
		super.onResume();
		
		
		try{
			findViewById(R.id.learnhow).setOnClickListener(this);
		}catch(Throwable t){
			
		}
	}
	
	public void sendRequest(EntryPointObject entry){
		if(!DataBaseHelper.isOnline(this,0)){
			errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
    		RUNNABLE_STATE = RUNNABLE_FAILED;
			RUNNABLE_STATE = RUNNABLE_FAILED;
			return;
		}
		try {
			tmpActivePrizes.clear();
			tmpPastPrizes.clear();
			String url = GlobalConstants.BASE_URL + entry.requestInfo("linksprizes");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			url += String.format("?token=%s",mprefs.getString("token", ""));
			//Log.v("URL",url);
			HttpRequest req = new HttpRequest(new URL(url+"&mobile_version="+GlobalConstants.MOBILE_VERSION));
			req.setMethodType("GET");
			JsonParser jp = req.AutoStreamJSONError();
			jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String fieldname = jp.getCurrentName();
				jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
				if ("prizes".equals(fieldname)) { 
					//DO parse json
					while (jp.nextToken() != JsonToken.END_ARRAY) {
						PrizeObject page = new PrizeObject(jp);
						//Log.v("Page",page.toString());
						//prizes.add(page);
						if(page.requestInfo("state").compareTo("expired") == 0 || page.requestInfo("state").compareTo("redeemed") == 0){
							tmpPastPrizes.add(page);
						}else{
							tmpActivePrizes.add(page);
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
				}
			}
			jp.close();
//			JSONObject json = req.AutoJSONError();
//			try{
//			JSONArray objects = json.getJSONArray("prizes");
//			for(int i=0; i<objects.length(); i++){
//				PrizeObject page = new PrizeObject(objects.getJSONObject(i));
//				//Log.v("Page",page.toString());
//				//prizes.add(page);
//				if(page.requestInfo("state").compareTo("expired") == 0 || page.requestInfo("state").compareTo("redeemed") == 0){
//					tmpPastPrizes.add(page);
//				}else{
//					tmpActivePrizes.add(page);
//				}
//			}
//			RUNNABLE_STATE = RUNNABLE_SUCCESS;
//			}catch(Throwable t){
//				errorTitle = json.getString("title");
//				errorMessage = json.getString("message");
//				errorType = json.getString("name");
//				RUNNABLE_STATE = RUNNABLE_FAILED;
//			}
		} catch (Throwable e) {
			e.printStackTrace();
			mHandler.post(new DisplayThrowable(e));
			errorMessage = "Please try again in a moment.";
    		errorTitle = "Request Error";
    		RUNNABLE_STATE = RUNNABLE_FAILED;
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}
	
	private class PrizeListAdapter extends BaseAdapter{
		ArrayList<PrizeObject> pages;

		public PrizeListAdapter(ArrayList<PrizeObject> inArray){
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

			PrizeView movieView = null;
			if (convertView == null) {
				movieView = new PrizeView(PrizesBozukoActivity.this);
				groupView.setContentView(movieView);
			}
			else {
				movieView = (PrizeView) convertView;
			}
			movieView.display((PrizeObject)getItem(position));
			groupView.showArrow(true);
			return groupView;
		}

	}

	public boolean onCreateOptionsMenu(Menu menu){
		//menu.add(0, R.drawable.icongames, 0, "Games").setIcon(R.drawable.icongames);
		//menu.add(0, R.drawable.iconbozuko, 0, "Bozuko").setIcon(R.drawable.iconbozuko);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.drawable.icongames:
				Intent games = new Intent(this,GamesTabController.class);
				games.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(games);
				break;
			case R.drawable.iconbozuko:
				Intent bozuko = new Intent(this,SettingsBozukoActivity.class);
				bozuko.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(bozuko);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Object object = arg0.getItemAtPosition(arg2);
		if(object.getClass() == PrizeObject.class){
			PrizeObject prize = (PrizeObject)object;
			if(prize.requestInfo("state").compareTo("redeemed") == 0 || prize.requestInfo("state").compareTo("expired") == 0){
				Intent intent = new Intent(this,PrizeBozukoActivity.class);
				intent.putExtra("Package", prize);
				startActivity(intent);
			}else{
				Intent intent = new Intent(this,PrizeRedeemBozukoActivity.class);
				intent.putExtra("Package", prize);
				startActivityForResult(intent,666);
			}
		}
	}
	
	 protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
         if (requestCode == 666) {
             if (resultCode == RESULT_OK) {
            	
             }
         }
     }

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this,AboutBozukoActivity.class);
		startActivity(intent);
	}
}
