package com.fuzz.android.datahandler;

import java.util.HashMap;
import java.util.Iterator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

public class DataObject implements Parcelable{

	protected HashMap<String,String> map = new HashMap<String,String>();
	public String tablename = "";
	public String queryid = "id";

	public void add(String key, String value) {
		// TODO Auto-generated method stub
		map.put(key.trim(),value);
		
	}

	public DataObject(){

	}

	public DataObject(Cursor r){
		for(int i=0; i<r.getColumnNames().length; i++){
			if(r.getString(i) != null)
				map.put(r.getColumnName(i).replace("&QUOTE;", "'").trim(), r.getString(i).replace("&QUOTE;", "'"));
		}
	}

	@SuppressWarnings("unchecked")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public DataObject createFromParcel(Parcel in) {
			return new DataObject(in);
		}

		public DataObject[] newArray(int size) {
			return new DataObject[size];
		}
	};

	protected DataObject(Parcel in) {
		map = new HashMap<String,String>();
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			map.put(in.readString(), in.readString());
		}
	}

	public DataObject(Node item) {
		// TODO Auto-generated constructor stub
		processNode(item);
	}
	
	public DataObject(JSONObject json) {
		// TODO Auto-generated constructor stub
		processJson(json,"");
	}
	
	public DataObject(JsonParser jp) {
		// TODO Auto-generated constructor stub
		processJsonParser(jp,"");
	}
	
	public void processJsonParser(JsonParser jp,String masterKey){
		try {
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String key = jp.getCurrentName();
				JsonToken token = jp.nextToken();
				if(token == JsonToken.START_OBJECT){
					processJsonParser(jp,masterKey+key);
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
	

	@SuppressWarnings("unchecked")
	public void processJson(JSONObject object,String masterKey){
		for(Iterator<String> it = object.keys(); it.hasNext();){
			String key = it.next();
			try {
				if(object.get(key).getClass() == JSONObject.class){
					processJson((JSONObject)object.get(key),masterKey+key);
				}else{
					map.put(masterKey+key, object.getString(key));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	}

	public void processNode(Node item){
		NodeList children = item.getChildNodes();
		for(int i=0; i<children.getLength(); i++){
			Node n = children.item(i);
			if(n != null){
				if(n.hasChildNodes()){
					if(n.getFirstChild().getNodeType() == Node.TEXT_NODE || n.getFirstChild().getNodeType() == Node.CDATA_SECTION_NODE){
						String value = "";
						for(int j=0; j<n.getChildNodes().getLength(); j++){
							if (n.getChildNodes().item(j).getNodeType() == Node.CDATA_SECTION_NODE)
							{
								CDATASection section = (CDATASection)n.getChildNodes().item(j);
								value = value + section.getNodeValue();
							}else{
								value = value + n.getChildNodes().item(j).getNodeValue();
							}
						}
						map.put(n.getNodeName(), value);
					}
				}
			}
		}
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		// TODO Auto-generated method stub
		//out.writeBundle(map);

		out.writeInt(map.size());
		for (String s: map.keySet()) {
			out.writeString(s);
			out.writeString(map.get(s));
		}
	}

	public boolean openDataBase(DataBaseHelper dbh){
		if(!dbh.isOpen()){
			dbh.openDataBase();
			return true;
		}
		return false;
	}

	public void closeDataBase(DataBaseHelper dbh,boolean opened){
		if(opened){
			dbh.close();
		}
	}

	public void getObject(String id,DataBaseHelper dbh){
		boolean ret = openDataBase(dbh);

		//String sql = "Select * from " + tablename + " where " + queryid + "=" + id;
		SQLiteDatabase db = dbh.getDB();
		//Cursor r = db.rawQuery(sql, null);
		String selection[] = {id};
		Cursor r = db.query(tablename, null, queryid + "=?",  selection, null, null, null);
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
	}
	
	public void getObject(String id,SQLiteDatabase db){
		//String sql = "Select * from " + tablename + " where " + queryid + "=" + id;
		//Cursor r = db.rawQuery(sql, null);
		String selection[] = {id};
		Cursor r = db.query(tablename, null, queryid + "=?",  selection, null, null, null);
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
	}
	


	public void delete(String id, DataBaseHelper dbh){
		boolean ret = openDataBase(dbh);
		SQLiteDatabase db = dbh.getDB();
		//String sql = "delete from " + tablename + " where " + queryid + "='" + id + "'";
		try{
			//db.execSQL(sql);
			String selection[] = {id};
			db.delete(tablename, queryid+"=?", selection);
		}catch(Throwable t){
			//t.printStackTrace();
		}
		closeDataBase(dbh,ret);
	}
	
	public void saveToDb(String id,DataBaseHelper dbh){
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
				r = getObjectCursor(db,id);
				if(r.moveToNext()){
					r.close();
					updateStatement(db,values,id);
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
	}
	
	public void saveToDb(String id, SQLiteDatabase db) {
		// TODO Auto-generated method stub
		ContentValues values = new ContentValues();
		for (String s: map.keySet()) {
			if(values.get(s) == null){
				values.put(s.replace("'", "&QUOTE;"), map.get(s).replace("'", "&QUOTE;"));
			}
		}
		Cursor r = null;
		if(id != null && tablename.compareTo("") != 0){
			try{ 
				r = getObjectCursor(db,id);
				
				if(r.moveToNext()){
					r.close();
					updateStatement(db,values,id);
				}else{
					r.close();
					insertStatement(db,values);
				}
			}catch(SQLException e){
				insertStatement(db,values);
			}
		}else if(tablename.compareTo("") != 0){
			insertStatement(db,values);
		}
	}
	
	public Cursor getObjectCursor(SQLiteDatabase db, String id){
		String columns[] = {queryid};
		String selection[] = {id};
		return db.query(tablename, columns , queryid+"=?", selection, null, null, null);
	}

	public void alterDB(SQLiteDatabase db){
		for (String s: map.keySet()) {
			String sql = "ALTER TABLE " + tablename + " ADD " + s + " text";
			try{
				db.execSQL(sql);
			}catch(SQLException e5){
				//e5.printStackTrace();
			}
		}
	}

	public void createTable(SQLiteDatabase db) throws SQLException{
		String sql = "create table " + tablename + " (id integer primary key autoincrement, ";
		for (String s: map.keySet()) {
				sql = sql + s + " text, ";
		}
		sql = sql.substring(0, sql.length()-2);
		sql = sql + ")";
		db.execSQL(sql);
	}

	public void insertStatement(SQLiteDatabase db, ContentValues values){
		long rowid = -1;
		try{
			
			rowid = db.insertOrThrow(tablename, null, values);
		}catch(SQLException e){
			try{
				createTable(db);
				rowid = db.insertOrThrow(tablename, null, values);
			}catch(SQLException e2){
				//e2.printStackTrace();
				alterDB(db);
				try{
					rowid = db.insertOrThrow(tablename, null, values);
				}catch(SQLException e3){
					//e3.printStackTrace();
				}
			}
		}
		
		if(rowid != -1){
			if(!map.containsKey(queryid)){
				map.put(queryid, String.valueOf(rowid));
			}
		}
	}
	
	public void updateStatement(SQLiteDatabase db, ContentValues values ,String id){
		String selection[] = {id};
		try{
			
			db.update(tablename, values, queryid + "=?", selection);
		}catch(SQLException e){
			alterDB(db);
			try{
				db.update(tablename, values, queryid + "=?", selection);
			}catch(SQLException e3){
			}
		}
	}
	
	public String toString(){
		String ret = "";
		for(String s : map.keySet()){
			ret += "Key: " + s + " Value: " + map.get(s) + "\n";
		}
		return ret;
	}

	public String requestInfo(String str){
		if(!map.containsKey(str)){
			return "";
		}
		return map.get(str);
	}

	public boolean checkInfo(String string) {
		// TODO Auto-generated method stub
		return map.containsKey(string);
	}

	public void remove(String key){
		map.remove(key);
	}
}
