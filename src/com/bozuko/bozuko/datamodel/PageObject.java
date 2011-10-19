package com.bozuko.bozuko.datamodel;

import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.datahandler.DataObject;

public class PageObject extends DataObject {

	public ArrayList<GameObject> games;

	public PageObject(JSONObject json){
		super(json);
		queryid = "id";
		tablename = "pages";
	}

	public PageObject(String string) {
		// TODO Auto-generated constructor stub
		super();
		map.put("id", string);
		queryid = "id";
		tablename = "pages";
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
					if(key.compareTo("games")==0){
						games = new ArrayList<GameObject>();
						for(int i=0; i<array.length(); i++){
							GameObject game = new GameObject(array.getJSONObject(i));
							games.add(game);
						}
					}
				}else{
					map.put(masterKey+key, object.getString(key));
				}
			} catch (JSONException e) {
				
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
		}
	}

	public void processJsonParser(JsonParser jp,String masterKey){
		try {
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String key = jp.getCurrentName();
				JsonToken token = jp.nextToken();
				if(token == JsonToken.START_OBJECT){
					processJsonParser(jp,masterKey+key);
				}else if(token == JsonToken.START_ARRAY){
					if(key.compareTo("games")==0){
						games = new ArrayList<GameObject>();
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							GameObject game = new GameObject(jp);
							games.add(game);
						}
					}else{
						while (jp.nextToken() != JsonToken.END_ARRAY) {
						}
					}
				}else if(token == JsonToken.NOT_AVAILABLE){
					throw new Exception("Parser failed");
				}else{
					map.put(masterKey+key, jp.getText());
				}
			}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public PageObject createFromParcel(Parcel in) {
			return new PageObject(in);
		}

		public PageObject[] newArray(int size) {
			return new PageObject[size];
		}
	};

	protected PageObject(Parcel in) {
		super(in);
		queryid = "id";
		tablename = "pages";

		int count = in.readInt();
		games = new ArrayList<GameObject>();
		for (int i = 0; i < count; i++) {
			games.add((GameObject) in.readParcelable(GameObject.class.getClassLoader()));
		}
	}

	public PageObject(JsonParser jp) {
		// TODO Auto-generated constructor stub
		super(jp);
		queryid = "id";
		tablename = "pages";
	}
	

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		if(games == null){
			out.writeInt(0);
		}else{
			out.writeInt(games.size());
			for(GameObject game : games){
				out.writeParcelable(game, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
			}
		}
	}

	public void saveToDb(String id,DataBaseHelper dbh){
		
		dbh.eraseTable("pages");
		dbh.eraseTable("games");
		dbh.eraseTable("gameiconimages");
		dbh.eraseTable("gameicons");
		dbh.eraseTable("gameState");
		dbh.eraseTable("listprizes");
		super.saveToDb(id, dbh);
		
		
		for(int i=0; i<games.size(); i++){
			games.get(i).add("pageid", requestInfo("id"));
			games.get(i).saveToDb(games.get(i).requestInfo("id"), dbh);
		}
	}
	
	public void getObject(String id,DataBaseHelper dbh){
		super.getObject(id, dbh);

		try{
		String selection[] = {requestInfo("id")};
		Cursor r = dbh.getDB().query("games", null, "pageid=?", selection, null, null, null);
		games = new ArrayList<GameObject>();
		while(r.moveToNext()){
			GameObject object = new GameObject(r);
			object.getObject(object.requestInfo("id"), dbh);
			games.add(object);
		}
		r.close();
		}catch(Throwable t){
		}
	}
	
	public void createTable(SQLiteDatabase db) throws SQLException{
		String sql = "create table " + tablename + " (_id integer primary key autoincrement, ";
		for (String s: map.keySet()) {
				sql = sql + s + " text, ";
		}
		sql = sql.substring(0, sql.length()-2);
		sql = sql + ")";
		db.execSQL(sql);
	}
}