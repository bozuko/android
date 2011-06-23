package com.bozuko.bozuko.datamodel;

import org.json.JSONObject;

import com.fuzz.android.datahandler.DataObject;

public class User extends DataObject {

	public User(JSONObject json){
		super(json);
		queryid = "userid";
		tablename = "user";
	}

	
	public User(String string) {
		// TODO Auto-generated constructor stub
		super();
		map.put("userid", string);
		queryid = "userid";
		tablename = "user";
	}
}
