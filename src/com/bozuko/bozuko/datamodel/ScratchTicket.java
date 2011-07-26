package com.bozuko.bozuko.datamodel;

import java.util.HashMap;

import org.json.JSONObject;

import com.fuzz.android.datahandler.DataObject;

public class ScratchTicket extends DataObject {

	public ScratchTicket(JSONObject json){
		super(json);
		queryid = "gameid";
		tablename = "scratchresult";
	}
	
	public ScratchTicket(String string) {
		// TODO Auto-generated constructor stub
		super();
		map.put("gameid", string);
		queryid = "gameid";
		tablename = "scratchresult";
	}

	public void copyTo(HashMap<String, String> temp) {
		// TODO Auto-generated method stub
		for(String key : map.keySet()){
			temp.put(key, map.get(key));
		}
	}
}
