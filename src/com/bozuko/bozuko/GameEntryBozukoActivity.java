package com.bozuko.bozuko;

import java.net.URL;
import java.util.ArrayList;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.bozuko.bozuko.datamodel.GameObject;
import com.bozuko.bozuko.datamodel.PrizeObject;
import com.bozuko.bozuko.views.GameHeaderView;
import com.bozuko.bozuko.views.PrizeCell;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
import com.fuzz.android.ui.GroupView;
import com.fuzz.android.ui.MergeAdapter;

public class GameEntryBozukoActivity extends BozukoControllerActivity implements OnItemClickListener, OnClickListener {

	GameObject game;
	
	public void progressRunnableComplete(){
		setupView();
		setupButton();
	}
	
	public View getRulesCellView(String inString,int resource){
		GroupView groupView = new GroupView(this);
		
		LinearLayout layout = new LinearLayout(getBaseContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		
		TextView textView = new TextView(getBaseContext());
		textView.setPadding(0, 10, 0, 10);
		textView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		textView.setTextColor(Color.BLACK);
		textView.setTypeface(Typeface.DEFAULT_BOLD);
		textView.setTextSize(14);
		textView.setGravity(Gravity.CENTER);
		textView.setText("Official Rules");
		layout.addView(textView);
		
		textView = new TextView(getBaseContext());
		textView.setPadding(0, 10, 0, 10);
		textView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		textView.setTextColor(Color.GRAY);
		textView.setTextSize(14);
	//	textView.setGravity(Gravity.CENTER);
		textView.setText(inString);
		layout.addView(textView);
		
		groupView.setContentView(layout);
		groupView.setImage(resource);
		
		return groupView;
	}

	public void progressRunnableError(){
		if(errorType.compareTo("facebook/auth")==0){
			makeDialog(errorMessage,errorTitle,new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					facebookSignOut();
					finish();
				}
			});
		}else{
			makeDialog(errorMessage,errorTitle,new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			});
			setupView();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent(R.layout.gameentry);
		setHeader(R.layout.detailheader);

		game = ((BozukoApplication)getApp()).currentGameObject;
		
		setupView();
	}
	
	public View getHeaderView(){
		GroupView groupView = new GroupView(this);


		GameHeaderView pageView = new GameHeaderView(this);
		pageView.display(game,((BozukoApplication)getApp()).currentPageObject);
		groupView.setContentView(pageView);

		groupView.setImage(R.drawable.cellbutton);

		return groupView;
	}
	
	public void setupView(){
		ListView listview = (ListView)findViewById(R.id.ListView01);
		if(listview.getAdapter() == null){
		listview.setBackgroundColor(Color.argb(255, 205, 205, 205));
		listview.setCacheColorHint(Color.argb(255, 205, 205, 205));
		listview.setSelector(R.drawable.blank);
		listview.setDividerHeight(0);
		
		MergeAdapter mergeAdapter = new MergeAdapter();

		mergeAdapter.addView(getSpacer(), false);
		mergeAdapter.addView(getHeaderView(),false);
		
		
		if(game.prizes.size() > 0 && game.consoldationPrizes.size() > 0){
			mergeAdapter.addView(getSpacer(), false);
			mergeAdapter.addView(getGroupTitleView("Prizes"),false);
			
			ArrayList<PrizeObject> prizes = new ArrayList<PrizeObject>();
			prizes.addAll(game.prizes);
			prizes.addAll(game.consoldationPrizes);
			mergeAdapter.addAdapter(new PrizeListAdapter(prizes));
			
			
		}else if(game.prizes.size() > 0){
			mergeAdapter.addView(getSpacer(), false);
			mergeAdapter.addView(getGroupTitleView("Prizes"),false);
			
			mergeAdapter.addAdapter(new PrizeListAdapter(game.prizes));
		}else if(game.consoldationPrizes.size() > 0){
			mergeAdapter.addView(getSpacer(), false);
			mergeAdapter.addView(getGroupTitleView("Prizes"),false);
			
			mergeAdapter.addAdapter(new PrizeListAdapter(game.consoldationPrizes));
		}
		
		mergeAdapter.addView(getSpacer(), false);
		mergeAdapter.addView(getRulesCellView(game.requestInfo("rules"), R.drawable.cellbtn), false);
	
		
		mergeAdapter.addView(getSpacer(), false);
		listview.setAdapter(mergeAdapter);
		listview.setOnItemClickListener(this);
		}
		//Log.v("GameState",game.gameState.toString());	
		
		findViewById(R.id.entergame).setVisibility(View.INVISIBLE);
		findViewById(R.id.agree).setVisibility(View.INVISIBLE);
		findViewById(R.id.nextgame).setVisibility(View.INVISIBLE);
		
		((TextView)findViewById(R.id.nextgame)).setText("Loading...");
		findViewById(R.id.nextgame).setVisibility(View.VISIBLE);
	}
	
	public void setupButton(){
		((Button)findViewById(R.id.entergame)).setText(game.gameState.requestInfo("button_text"));
		((Button)findViewById(R.id.entergame)).setOnClickListener(this);
		
		if(game.gameState.requestInfo("button_enabled").compareTo("true")!=0){
			findViewById(R.id.entergame).setVisibility(View.INVISIBLE);
			findViewById(R.id.agree).setVisibility(View.INVISIBLE);
			findViewById(R.id.nextgame).setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.nextgame)).setText(game.gameState.requestInfo("button_text"));
		}else{
			findViewById(R.id.entergame).setVisibility(View.VISIBLE);
			findViewById(R.id.agree).setVisibility(View.VISIBLE);
			findViewById(R.id.nextgame).setVisibility(View.INVISIBLE);
		}
	}

	UpdateReceiver mReceiver = new UpdateReceiver();
	public void onPause(){
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	public void onResume(){
		registerReceiver(mReceiver, new IntentFilter("LIKECHANGED"));
		super.onResume();
		setupView();
		unProgressRunnable(new Runnable(){
			public void run(){
				getGameState();
			}
		});
	}
	
	public void getGameState(){
		if(!DataBaseHelper.isOnline(this)){
			errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
    		RUNNABLE_STATE = RUNNABLE_FAILED;
		}
		try {
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			String url = GlobalConstants.BASE_URL + game.gameState.requestInfo("linksgame_state");
			url += "?token=" + mprefs.getString("token", "") + "&mobile_version="+GlobalConstants.MOBILE_VERSION;
			HttpRequest req = new HttpRequest(new URL(url + String.format("&ll=%s,%s", mprefs.getString("clat", "0.00"),mprefs.getString("clon", "0.00"))));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSONError();
			try{
				errorTitle = json.getString("title");
				errorMessage = json.getString("message");
				errorType = json.getString("name");
				RUNNABLE_STATE = RUNNABLE_FAILED;
		}catch(Throwable t){
			
			Log.v("JSONSTATE",json.toString());
			game.gameState.processJson(json, "");
			RUNNABLE_STATE = RUNNABLE_SUCCESS;
		}
		}catch(Throwable t){
			errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Object object = arg0.getItemAtPosition(arg2);
		if(object.getClass() == PrizeObject.class){
			Intent intent = new Intent(this,PrizeDetailBozukoActivity.class);
			intent.putExtra("Package", (PrizeObject)object);
			startActivity(intent);
		}
	}
	
	private class PrizeListAdapter extends BaseAdapter{
		ArrayList<PrizeObject> prizes;
		
		public PrizeListAdapter(ArrayList<PrizeObject> inArray){
			prizes = inArray;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return prizes.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return prizes.get(position);
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

			PrizeCell movieView = null;
			if (convertView == null) {
				movieView = new PrizeCell(getBaseContext());
				groupView.setContentView(movieView);
			}
			else {
				movieView = (PrizeCell) convertView;
			}
			movieView.display((PrizeObject)getItem(position));
			int resource = R.drawable.tableviewtopbutton;
			if(position > 0 && position < game.prizes.size()-1){
				resource = R.drawable.tableviewmidbutton;
			}else if(position > 0){
				resource = R.drawable.tableviewbtmbutton;
			}
			if(getCount()==1){
				resource = R.drawable.cellbutton;
			}

			groupView.setImage(resource);
			groupView.showArrow(false);
			return groupView;
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		//Log.v("Game",game.toString());
		System.gc();
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
		//Log.v("GAME",game.toString());
		//Log.v("GAMESTATE",game.gameState.toString());
		if(mprefs.getBoolean("facebook_login", false)){
//			if(game.gameState.requestInfo("button_action").compareTo("enter") == 0){
//				if(game.requestInfo("entry_methodtype").compareTo("facebook/like")==0 && ((BozukoApplication)getApp()).currentPageObject.requestInfo("liked").compareTo("true")!=0){
//					//Intent intent = new Intent(this,SocialMediaWebViewActivity.class);
//					//intent.setData(Uri.parse(((BozukoApplication)getApp()).currentGameObject.requestInfo("like_url")));
//					//startActivity(intent);
//					makeDialog("Facebook like required.","Game Error",null);
//				}else{
//					if(game.requestInfo("type").compareTo("scratch")==0){
//						Intent intent = new Intent(this,ScratchGameBozukoActivity.class);
//						startActivity(intent);
//					}else if(game.requestInfo("type").compareTo("slots") == 0){
//						Intent intent = new Intent(this,SlotsGameBozukoActivity.class);
//						startActivity(intent);
//					}
//				}
//			}else{
//				if(game.requestInfo("type").compareTo("scratch")==0){
//					Intent intent = new Intent(this,ScratchGameBozukoActivity.class);
//					startActivity(intent);
//				}else if(game.requestInfo("type").compareTo("slots") == 0){
//					Intent intent = new Intent(this,SlotsGameBozukoActivity.class);
//					startActivity(intent);
//				}
//			}
			if(game.requestInfo("type").compareTo("scratch")==0){
				Intent intent = new Intent(this,ScratchGameBozukoActivity.class);
				startActivity(intent);
			}else if(game.requestInfo("type").compareTo("slots") == 0){
				Intent intent = new Intent(this,SlotsGameBozukoActivity.class);
				startActivity(intent);
			}
		}else{
			notLoggedIn();
		}
		
		
	}
	
	protected class UpdateReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			progressRunnable(new Runnable(){
				public void run(){
					getGameState();
				}
			},"Loading...",CANCELABLE);
		}
	}
}
