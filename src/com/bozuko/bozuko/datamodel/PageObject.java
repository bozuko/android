package com.bozuko.bozuko.datamodel;

import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Parcel;
import android.os.Parcelable;

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
				e.printStackTrace();
			}
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
}