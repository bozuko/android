package com.bozuko.bozuko.datamodel;

import org.json.JSONObject;

import com.fuzz.android.datahandler.DataObject;

public class GameState extends DataObject {

	public GameState(JSONObject json){
		super(json);
		queryid = "bozukoid";
		tablename = "bozuko";
	}

	
	public GameState(String string) {
		// TODO Auto-generated constructor stub
		super();
		map.put("bozukoid", string);
		queryid = "bozukoid";
		tablename = "bozuko";
	}
}
