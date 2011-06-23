package com.bozuko.bozuko.datamodel;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.fuzz.android.datahandler.DataObject;

public class GameObject extends DataObject {

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
	}
	
}
