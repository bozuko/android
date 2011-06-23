package com.bozuko.bozuko.datamodel;

import org.json.JSONObject;

import com.fuzz.android.datahandler.DataObject;

public class EntryPointObject extends DataObject{

	public EntryPointObject(JSONObject json){
		super(json);
		queryid = "entryid";
		tablename = "entrypoint";
	}

	
	public EntryPointObject(String string) {
		// TODO Auto-generated constructor stub
		super();
		map.put("entryid", string);
		queryid = "entryid";
		tablename = "entrypoint";
	}
}
