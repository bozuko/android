package com.bozuko.bozuko;

import com.bozuko.bozuko.datamodel.GameObject;

import android.os.Bundle;
import android.widget.TextView;

public class OfficialBozukoActivity extends BozukoControllerActivity {

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		GameObject game = ((BozukoApplication)getApp()).currentGameObject;
		
		
		setContent(R.layout.officialrules);
		setHeader(R.layout.detailheader);
		
		((TextView)findViewById(R.id.rules)).setText(game.requestInfo("rules"));
	}
}
