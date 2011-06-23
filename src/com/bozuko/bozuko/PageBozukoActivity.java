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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
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

		groupView.setImage(R.drawable.cellbutton);

		return groupView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent(R.layout.listview);
		setHeader(R.layout.detailheader);

		page = (PageObject)getIntent().getParcelableExtra("Package");

		if(page == null){
			pageLink = getIntent().getStringExtra("Page");
			progressRunnable(new Runnable(){
				public void run(){
					sendRequest();
				}
			},"Loading...",CANCELABLE);
		}
	}

	public void setupView(){
		ListView listview = (ListView)findViewById(R.id.ListView01);
		listview.setBackgroundColor(Color.argb(255, 205, 205, 205));
		listview.setCacheColorHint(Color.argb(255, 205, 205, 205));
		listview.setSelector(R.drawable.blank);
		MergeAdapter mergeAdapter = new MergeAdapter();

		mergeAdapter.addView(getSpacer(), false);
		mergeAdapter.addView(getHeaderView(),true);

		if(page.games.size()>0){
			mergeAdapter.addView(getSpacer(), false);
			mergeAdapter.addAdapter(new GameListAdapter());
		}

		if(page.checkInfo("announcement") && page.requestInfo("announcement").compareTo("") != 0){
			mergeAdapter.addView(getSpacer(), false);
			mergeAdapter.addView(getGreyCellView(page.requestInfo("announcement")),false);
		}

		mergeAdapter.addView(getSpacer(), false);
		mergeAdapter.addView(getCellView("Feedback",R.drawable.tableviewtopbutton), true);
		mergeAdapter.addView(getCellView("Share",R.drawable.tableviewbtmbutton), true);
		mergeAdapter.addView(getSpacer(), false);

		try{
			MenuOption option = new MenuOption(R.drawable.facebook_icon, "Facebook Check In", "", false, this.getClass().getMethod("checkIn", (Class<?>[])null));
			mergeAdapter.addView(getOptionView(option,R.drawable.tableviewtopbutton),true);

			option = new MenuOption(R.drawable.facebook_icon, "Like Us on Facebook", "", false, this.getClass().getMethod("likeUs", (Class<?>[])null));
			mergeAdapter.addView(getOptionView(option,R.drawable.tableviewbtmbutton),true);
		}catch(Throwable t){

		}
		mergeAdapter.addView(getSpacer(), false);
		listview.setAdapter(mergeAdapter);
		listview.setOnItemClickListener(this);
	}

	public void onResume(){
		if(page != null){
			setupView();
		}else{
			//LOAD DATA
		}
		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

	}

	public void likeUs(){

	}

	public void checkIn(){

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
    		RUNNABLE_STATE = RUNNABLE_FAILED;
		}
		try {
			String url = GlobalConstants.BASE_URL + pageLink;
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			HttpRequest req = new HttpRequest(new URL(url + "&token=" + mprefs.getString("token", "")));
			req.setMethodType("GET");
			JSONObject json = req.AutoJSON();
			page = new PageObject(json);
			RUNNABLE_STATE = RUNNABLE_SUCCESS;
		} catch (Throwable e) {
			e.printStackTrace();
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
    }
}