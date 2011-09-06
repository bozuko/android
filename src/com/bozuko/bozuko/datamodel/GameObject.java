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

public class GameObject extends DataObject {

	public ArrayList<String> icons;
	public ArrayList<String> iconsImages;
	public GameState gameState;
	public ArrayList<PrizeObject>prizes;
	public ArrayList<PrizeObject>consoldationPrizes;

	public GameObject(JSONObject json){
		super(json);
		queryid = "id";
		tablename = "games";
	}

	public GameObject(String string) {
		// TODO Auto-generated constructor stub
		super();
		map.put("id", string);
		queryid = "id";
		tablename = "games";
	}

	@SuppressWarnings("unchecked")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public GameObject createFromParcel(Parcel in) {
			return new GameObject(in);
		}

		public GameObject[] newArray(int size) {
			return new GameObject[size];
		}
	};

	protected GameObject(Parcel in) {
		super(in);
		queryid = "id";
		tablename = "games";

		int count = in.readInt();
		prizes = new ArrayList<PrizeObject>();
		for (int i = 0; i < count; i++) {
			prizes.add((PrizeObject) in.readParcelable(PrizeObject.class.getClassLoader()));
		}

		count = in.readInt();
		consoldationPrizes = new ArrayList<PrizeObject>();
		for (int i = 0; i < count; i++) {
			consoldationPrizes.add((PrizeObject) in.readParcelable(PrizeObject.class.getClassLoader()));
		}
	}
	
	public GameObject(JsonParser jp) {
		// TODO Auto-generated constructor stub
		super(jp);
		queryid = "id";
		tablename = "games";
	}
	

	public GameObject(Cursor r) {
		// TODO Auto-generated constructor stub
		super(r);
		queryid = "id";
		tablename = "games";
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		if(prizes == null){
			out.writeInt(0);
		}else{
			out.writeInt(prizes.size());
			for(PrizeObject prize : prizes){
				out.writeParcelable(prize, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
			}
		}
		
		if(consoldationPrizes == null){
			out.writeInt(0);
		}else{
			out.writeInt(consoldationPrizes.size());
			for(PrizeObject prize : consoldationPrizes){
				out.writeParcelable(prize, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void processIcons(JSONObject object){
		iconsImages = new ArrayList<String>();
		icons = new ArrayList<String>();
		for(Iterator<String> it = object.keys(); it.hasNext();){
			String key = it.next();
			try {
				iconsImages.add(object.getString(key));
				icons.add(key);
			}catch(Throwable t){
				
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void processJson(JSONObject object,String masterKey){
		//Log.v("JSON",object.toString());
		for(Iterator<String> it = object.keys(); it.hasNext();){
			String key = it.next();
			try {
				if(object.get(key).getClass() == JSONObject.class){
					if(key.compareTo("game_state")==0){
						gameState = new GameState((JSONObject)object.get(key));
					}else if(key.compareTo("icons")==0){
						processIcons((JSONObject)object.get(key));
					}else{
						processJson((JSONObject)object.get(key),masterKey+key);
					}
				}else if(object.get(key).getClass() == JSONArray.class){
					JSONArray array = object.getJSONArray(key);
					if(key.compareTo("prizes")==0){
						prizes = new ArrayList<PrizeObject>();
						for(int i=0; i<array.length(); i++){
							PrizeObject game = new PrizeObject(array.getJSONObject(i));
							prizes.add(game);
							//Log.v("Prize",game.toString());
						}
					}
					if(key.compareTo("consolation_prizes")==0){
						consoldationPrizes = new ArrayList<PrizeObject>();
						for(int i=0; i<array.length(); i++){
							PrizeObject game = new PrizeObject(array.getJSONObject(i));
							consoldationPrizes.add(game);
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
	
	public void processJsonParser(JsonParser jp,String masterKey){
		try {
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String key = jp.getCurrentName();
				JsonToken token = jp.nextToken();
				if(token == JsonToken.START_OBJECT){
					if(key.compareTo("game_state")==0){
						gameState = new GameState(jp);
					}else if(key.compareTo("icons")==0){
						processIcons(jp);
					}else{
						processJsonParser(jp,masterKey+key);
					}
				}else if(token == JsonToken.START_ARRAY){
					if(key.compareTo("prizes")==0){
						prizes = new ArrayList<PrizeObject>();
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							PrizeObject game = new PrizeObject(jp);
							prizes.add(game);
						}
					}else if(key.compareTo("consolation_prizes")==0){
						consoldationPrizes = new ArrayList<PrizeObject>();
						while (jp.nextToken() != JsonToken.END_ARRAY) {
							PrizeObject game = new PrizeObject(jp);
							consoldationPrizes.add(game);
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
			//e.printStackTrace();
		}
	}
	
	private void processIcons(JsonParser jp) {
		// TODO Auto-generated method stub
		iconsImages = new ArrayList<String>();
		icons = new ArrayList<String>();
		try{
		while (jp.nextToken() != JsonToken.END_OBJECT) {
			String key = jp.getCurrentName();
			jp.nextToken();
			iconsImages.add(jp.getText());
			icons.add(key);
		}
		}catch(Throwable t){
			
		}
	}

	public String toString(){
		String ret = super.toString();
		for(PrizeObject prize : prizes){
			ret += prize.toString();
		}
		
		return ret;
	}

	public void saveToDb(String id,DataBaseHelper dbh){
		super.saveToDb(id, dbh);
		
		gameState.saveToDb(id, dbh);
		
		for(int i=0; i<prizes.size(); i++){
			prizes.get(i).add("gameid", id);
			prizes.get(i).add("prizetype", 1+"");
			prizes.get(i).tablename = "listprizes";
			prizes.get(i).saveToDb(id, dbh);
		}
		
		for(int i=0; i<consoldationPrizes.size(); i++){
			consoldationPrizes.get(i).add("gameid", id);
			consoldationPrizes.get(i).add("prizetype", 2+"");
			consoldationPrizes.get(i).tablename = "listprizes";
			consoldationPrizes.get(i).saveToDb(id, dbh);
		}
		
		if(icons!=null){
			for(int i=0; i<icons.size(); i++){
				DataObject object = new DataObject();
				object.queryid = "id";
				object.tablename = "gameicons";
				object.add("gameid", id);
				object.add("icon", icons.get(i));
				object.saveToDb(null, dbh);
				//Log.v("ICON",object.toString());
			}
		}
		
		if(iconsImages!=null){
			for(int i=0; i<iconsImages.size(); i++){
				DataObject object = new DataObject();
				object.queryid = "id";
				object.tablename = "gameiconimages";
				object.add("gameid", id);
				object.add("icon", iconsImages.get(i));
				object.saveToDb(null, dbh);
				
				//Log.v("ICONIMAGE",object.toString());
			}
		}
	}
	
	public void getObject(String id,DataBaseHelper dbh){
		super.getObject(id, dbh);
		
		gameState = new GameState(id);
		gameState.getObject(id, dbh);
		
		String selection[] = {id,"2"};
		Cursor r = dbh.getDB().query("listprizes", null, "gameid=? and prizetype=?", selection, null, null, null);
		consoldationPrizes = new ArrayList<PrizeObject>();
		while(r.moveToNext()){
			PrizeObject object = new PrizeObject(r);
			consoldationPrizes.add(object);
		}
		r.close();
		
		String selection2[] = {id,"1"};
		Cursor r2 = dbh.getDB().query("listprizes", null, "gameid=? and prizetype=?", selection2, null, null, null);
		prizes = new ArrayList<PrizeObject>();
		while(r2.moveToNext()){
			PrizeObject object = new PrizeObject(r2);
			prizes.add(object);
		}
		r2.close();
		
		try{
		String selection3[] = {id};
		Cursor r3 = dbh.getDB().query("gameicons", null, "gameid=?", selection3, null, null, null);
		
		while(r3.moveToNext()){
			DataObject object = new DataObject(r3);
			//Log.v("ICON",object.toString());
			if(icons==null){
				icons = new ArrayList<String>();
			}
			icons.add(object.requestInfo("icon"));
		}
		r3.close();
		}catch(Throwable t){
		}
		
		try{
		String selection4[] = {id};
		Cursor r4 = dbh.getDB().query("gameiconimages", null, "gameid=?", selection4, null, null, null);
		
		while(r4.moveToNext()){
			DataObject object = new DataObject(r4);
			//Log.v("ICONIMAGE",object.toString());
			if(iconsImages==null){
				iconsImages = new ArrayList<String>();
			}
			iconsImages.add(object.requestInfo("icon"));
		}
		r4.close();
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
