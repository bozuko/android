package com.bozuko.bozuko;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabWidget;

public class TabController extends TabActivity {
	
	public void onCreate(Bundle savedInstanceState) {
		 getWindow().setFormat(PixelFormat.RGBA_8888);
	    super.onCreate(savedInstanceState);
	    
	    showtabs();
	}
	
	public void showtabs(){
		setContentView(R.layout.mastertabactivity);
	    
	    TabHost tabHost = getTabHost();
	    TabWidget tabWidget = tabHost.getTabWidget();
	    TabHost.TabSpec spec;
	    Intent intent;
	    try{
	    	tabWidget.setStripEnabled(false);
	    	//tabWidget.setRightStripDrawable(R.drawable.menuline);
	    	//tabWidget.setLeftStripDrawable(R.drawable.menuline);
	    }catch(Throwable t){
	    	
	    }
	    
	    intent = new Intent().setClass(this, GamesTabController.class);
	    spec = tabHost.newTabSpec("Games").setIndicator("Games").setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, PrizesBozukoActivity.class);
	    spec = tabHost.newTabSpec("Prizes").setIndicator("Prizes").setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, SettingsBozukoActivity.class);
	    spec = tabHost.newTabSpec("Bozuko").setIndicator("Bozuko").setContent(intent);
	    tabHost.addTab(spec);
	    
	    tabHost.setCurrentTab(0);
	    setTabColor(tabHost);
	}
	
	int r[] = {R.drawable.gamesbutton,R.drawable.prizesbutton,R.drawable.bozukobutton};
	
	
	public void setTabColor(TabHost tabhost) {
        for(int i=0;i<tabhost.getTabWidget().getChildCount();i++)
        {
        	tabhost.getTabWidget().getChildAt(i).setBackgroundResource(r[i]);
        }
    }


}
