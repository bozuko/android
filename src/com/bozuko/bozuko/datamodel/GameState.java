package com.bozuko.bozuko.datamodel;

import org.codehaus.jackson.JsonParser;
import org.json.JSONObject;
import com.fuzz.android.datahandler.DataObject;

public class GameState extends DataObject {

	public GameState(JSONObject json){
		super(json);
		queryid = "game_id";
		tablename = "gameState";
	}

	
	public GameState(String string) {
		// TODO Auto-generated constructor stub
		super();
		map.put("game_id", string);
		queryid = "game_id";
		tablename = "gameState";
	}


	public GameState(JsonParser jp) {
		// TODO Auto-generated constructor stub
		super(jp);
		queryid = "game_id";
		tablename = "gameState";
	}

}
