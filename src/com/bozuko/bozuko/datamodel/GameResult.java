package com.bozuko.bozuko.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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
							//Log.v("TEXT",json.getString("text"));
							//Log.v("NUMBER",json.getString("number"));
							results.add(temp);
						}
					}
				}else{
					map.put(masterKey+key, object.getString(key));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void saveToDb(String id,DataBaseHelper dbh, String bozukoid){
		boolean ret = openDataBase(dbh);
		SQLiteDatabase db = dbh.getDB();
		ContentValues values = new ContentValues();
		for (String s: map.keySet()) {
			if(values.get(s) == null){
				values.put(s.replace("'", "&QUOTE;"), map.get(s).replace("'", "&QUOTE;"));
			}
		}
		Cursor r = null;
		if(id != null && tablename.compareTo("") != 0){
			try{ 
				r = getObjectCursor(db,id,bozukoid);
				if(r.moveToNext()){
					r.close();
					updateStatement(db,values,id,bozukoid);
				}else{
					r.close();
					insertStatement(db,values);
				}
			}catch(SQLException e){
				insertStatement(db,values);
			}
			closeDataBase(dbh,ret);
		}else if(tablename.compareTo("") != 0){
			insertStatement(db,values);
		}
		
		
		for(int i=0; i<results.size(); i++){
			ScratchTicket ticket = new ScratchTicket(id+i);
			HashMap<String,String> temp = (HashMap<String, String>) results.get(i);
			for(String key : temp.keySet()){
				ticket.add(key, temp.get(key));
			}
			
			ticket.saveToDb(id+i, dbh);
		}
	} 
	
	public void getObject(String id,DataBaseHelper dbh, String bozukoid){
		boolean ret = openDataBase(dbh);
		SQLiteDatabase db = dbh.getDB();
		String selection[] = {id,bozukoid};
		DatabaseUtils.dumpCursor(db.query(tablename, null, null,null, null, null, null));
		DatabaseUtils.dumpCursor(db.query(tablename, null, queryid + "=? AND bozukoid=?",  selection, null, null, null));
		Cursor r = db.query(tablename, null, queryid + "=? AND bozukoid=?",  selection, null, null, null);
		while(r.moveToNext()){
			for(String key : r.getColumnNames()){
				String value = r.getString(r.getColumnIndex(key));
				if(value != null){
					//Log.v("ObjectData","Key: " + key + " Data: " + value);
					map.put(key.replace("&QUOTE;", "'"), value.replace("&QUOTE;", "'"));
				}
			}
		}
		r.close();
		//dbh.close();
		closeDataBase(dbh,ret);
		
		
		results = new ArrayList<Object>();
		for(int i=0; i<6; i++){
			ScratchTicket ticket = new ScratchTicket(id+i);
			ticket.getObject(id+i, dbh);
			HashMap<String,String> temp = new HashMap<String,String>();
			ticket.copyTo(temp);
			
			results.add(temp);
		}
	}
	
	
	public Cursor getObjectCursor(SQLiteDatabase db, String id,String bozukoid){
		String columns[] = {queryid};
		String selection[] = {id,bozukoid};
		return db.query(tablename, columns , queryid+"=? AND bozukoid=?", selection, null, null, null);
	}
	
	
	public void updateStatement(SQLiteDatabase db, ContentValues values ,String id, String bozukoid){
		String selection[] = {id,bozukoid};
		try{
			
			db.update(tablename, values, queryid + "=? AND bozukoid=?", selection);
		}catch(SQLException e){
			alterDB(db);
			try{
				db.update(tablename, values, queryid + "=? AND bozukoid=?", selection);
			}catch(SQLException e3){
			}
		}
	}
}
