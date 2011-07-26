package com.bozuko.bozuko.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.datahandler.DataObject;

public class GameResult extends DataObject {

	public ArrayList<Object> results;
	
	public GameResult(JSONObject json){
		super(json);
		queryid = "gameid";
		tablename = "gameresult";
	}
	
	public GameResult(String string) {
		// TODO Auto-generated constructor stub
		super();
		map.put("gameid", string);
		queryid = "gameid";
		tablename = "gameresult";
	}
	
	@SuppressWarnings("unchecked")
	public void processJson(JSONObject object,String masterKey){
		
		for(Iterator<String> it = object.keys(); it.hasNext();){
			String key = it.next();
			try {
				if(object.get(key).getClass() == JSONObject.class){
					processJson((JSONObject)object.get(key),masterKey+key);
				}else if(object.get(key).getClass() == JSONArray.class){
					JSONArray array = object.getJSONArray(key);
					if(key.compareTo("result")==0){
						results = new ArrayList<Object>();
						for(int i=0; i<array.length(); i++){
							results.add(array.getString(i));
						}
					}
					if(key.compareTo("numbers")==0){
						results = new ArrayList<Object>();
						for(int i=0; i<array.length(); i++){
							JSONObject json = array.getJSONObject(i);
							HashMap<String,String> temp = new HashMap<String,String>();
							temp.put("number", json.getString("number"));
							temp.put("text", json.getString("text"));
							Log.v("TEXT",json.getString("text"));
							Log.v("NUMBER",json.getString("number"));
							results.add(temp);
						}
					}
				}else{
					map.put(masterKey+key, object.getString(key));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void saveToDb(String id,DataBaseHelper dbh){
		super.saveToDb(id, dbh);
		for(int i=0; i<results.size(); i++){
			ScratchTicket ticket = new ScratchTicket(id+i);
			HashMap<String,String> temp = (HashMap<String, String>) results.get(i);
			for(String key : temp.keySet()){
				ticket.add(key, temp.get(key));
			}
			
			ticket.saveToDb(id+i, dbh);
		}
	}
	
	public void getObject(String id,DataBaseHelper dbh){
		super.getObject(id, dbh);
		results = new ArrayList<Object>();
		for(int i=0; i<6; i++){
			ScratchTicket ticket = new ScratchTicket(id+i);
			ticket.getObject(id+i, dbh);
			HashMap<String,String> temp = new HashMap<String,String>();
			ticket.copyTo(temp);
			
			results.add(temp);
		}
	}
}
