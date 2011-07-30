package com.bozuko.bozuko.datamodel;

import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Parcel;
import android.os.Parcelable;

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
}
