package com.bozuko.bozuko.datamodel;

import org.json.JSONObject;

import com.fuzz.android.datahandler.DataObject;

public class Bozuko extends DataObject {

	public Bozuko(JSONObject json){
		super(json);
		queryid = "bozukoid";
		tablename = "bozuko";
	}

	
	public Bozuko(String string) {
		// TODO Auto-generated constructor stub
		super();
		map.put("bozukoid", string);
		queryid = "bozukoid";
		tablename = "bozuko";
	}
}
