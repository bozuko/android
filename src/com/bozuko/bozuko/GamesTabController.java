package com.bozuko.bozuko;

import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;

public class GamesTabController extends TabActivity implements OnClickListener {

	
	public void onCreate(Bundle savedInstanceState) {
		 getWindow().setFormat(PixelFormat.RGBA_8888);
	    super.onCreate(savedInstanceState);
	    showtabs();
	    
	    findViewById(R.id.nearby).setOnClickListener(this);
	    findViewById(R.id.favorites).setOnClickListener(this);
	    findViewById(R.id.map).setOnClickListener(this);
	}
	
	public void showtabs(){
		setContentView(R.layout.tabactivity);
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    
	    intent = new Intent().setClass(this, NearByGamesBozukoActivity.class);
	    spec = tabHost.newTabSpec("Nearby").setIndicator("Nearby").setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, FavoriteGamesBozukoActivity.class);
	    spec = tabHost.newTabSpec("Favorites").setIndicator("Favorites").setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, GamesMapController.class);
	    spec = tabHost.newTabSpec("Map").setIndicator("Map").setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	    
	    Button nearby = (Button)findViewById(R.id.nearby);
		Button favorites = (Button)findViewById(R.id.favorites);
		Button map = (Button)findViewById(R.id.map);
	    nearby.setSelected(true);
		favorites.setSelected(false);
		map.setSelected(false);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		TabHost tabHost = getTabHost();
		Button nearby = (Button)findViewById(R.id.nearby);
		Button favorites = (Button)findViewById(R.id.favorites);
		Button map = (Button)findViewById(R.id.map);
		switch(v.getId()){
			case R.id.nearby:
				tabHost.setCurrentTab(0);
				nearby.setSelected(true);
				favorites.setSelected(false);
				map.setSelected(false);
				break;
			case R.id.favorites:
				tabHost.setCurrentTab(1);
				nearby.setSelected(false);
				favorites.setSelected(true);
				map.setSelected(false);
				break;
			case R.id.map:
				tabHost.setCurrentTab(2);
				nearby.setSelected(false);
				favorites.setSelected(false);
				map.setSelected(true);
				break;
		}
	}

	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(0, R.drawable.ic_menu_refresh, 0, "Refresh").setIcon(R.drawable.ic_menu_refresh);
		menu.add(0, R.drawable.iconprizes, 0, "Prizes").setIcon(R.drawable.iconprizes);
		menu.add(0, R.drawable.iconbozuko, 0, "Bozuko").setIcon(R.drawable.iconbozuko);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.drawable.ic_menu_refresh:
				LocalActivityManager m = getLocalActivityManager();
				try{
					((BozukoControllerActivity)m.getCurrentActivity()).refresh();
				}catch(Throwable t){
					
				}
				break;
			case R.drawable.iconbozuko:
				Intent bozuko = new Intent(this,SettingsBozukoActivity.class);
				bozuko.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(bozuko);
				break;
			case R.drawable.iconprizes:
				Intent prizes = new Intent(this,PrizesBozukoActivity.class);
				prizes.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(prizes);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
}